package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibagroup.wf.intelia.core.mis.IRobotLogger;
import com.ibagroup.wf.intelia.core.mis.LoggableDetail;
import com.ibagroup.wf.intelia.core.mis.LoggableField;
import com.ibagroup.wf.intelia.core.mis.LoggingPhase;
import com.ibagroup.wf.intelia.core.mis.TaskDetail;
import com.ibagroup.wf.intelia.core.to.BaseTO;

public class LoggerDetailsWrapper extends ChainMethodWrapper {

    private static final Logger logger = LoggerFactory.getLogger(LoggerDetailsWrapper.class);

    private IRobotLogger robotLogger;

    public LoggerDetailsWrapper(IRobotLogger robotLogger) {
        super();
        this.robotLogger = robotLogger;
    }

    @Override
    public boolean isHandled(Method m) {
        return PerformMethodWrapper.isPerformMethod.test(m);
    }

    @Override
    public Object wrap(Invocation invocation) throws Throwable {

        FieldUtils.getFieldsListWithAnnotation(invocation.getSelf().getClass(), LoggableField.class).stream().forEach(field -> {
            if (field.getAnnotation(LoggableField.class).phase().equals(LoggingPhase.ONSTART)) {
                String module = field.getAnnotation(LoggableField.class).module();
                searchForDetails(invocation.getSelf(), field, module, false);
            }
        });

        Object result = invokeInner(invocation);

        FieldUtils.getFieldsListWithAnnotation(invocation.getSelf().getClass(), LoggableField.class).stream().forEach(field -> {
            if (field.getAnnotation(LoggableField.class).phase().equals(LoggingPhase.ONCOMPLETION)) {
                String module = field.getAnnotation(LoggableField.class).module();
                searchForDetails(invocation.getSelf(), field, module, false);
            }
        });

        return result;

    }

    private void logDetail(Object robot, Field field, String module) {
        try {
            field.setAccessible(true);
            String value = Objects.toString(field.get(robot));
            TaskDetail detail = new TaskDetail(module, field.getAnnotation(LoggableDetail.class).name(), value);
            robotLogger.addDetails(detail);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            logger.error("[LoggerDetailsWrapper]: error occured during TaskDetail logging. Unable to log field: " + field.getName() + " from: " + robot.getClass(), e);
        }

    }

    private void searchForDetails(Object object, Field field, String module, boolean fromTO) {
        if (!fromTO) {
            RunnerContext.setRecordUuid(RunnerContextHelper.extractRecordUuid(object));
        }

        if (List.class.isAssignableFrom(field.getType()) && Class.class.isAssignableFrom((((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]).getClass())
                && (BaseTO.class.isAssignableFrom((Class<?>) (((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0])))) {
            try {
                field.setAccessible(true);
                List<Class<?>> listTO = (List<Class<?>>) field.get(object);
                if (CollectionUtils.isNotEmpty(listTO)) {
                    for (Object to : listTO) {
                        if (to instanceof BaseTO) {
                            setRecordUuid((BaseTO) to);
                            searchFromTOObject(to, module);
                        }
                    }
                }

            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.error("[LoggerDetailsWrapper]: error occured during TaskDetail logging. Unable to get list TO: " + field.getName() + " from: " + object.getClass(), e);
            }
        } else if (BaseTO.class.isAssignableFrom(field.getType())) {
            searchForRecordUuid(object, field);
            List<Field> innerFields = FieldUtils.getFieldsListWithAnnotation(field.getType(), LoggableDetail.class);
            try {
                field.setAccessible(true);
                Object innerObject = field.get(object);
                for (Field f : innerFields) {
                    searchForDetails(innerObject, f, module, true);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.error("[LoggerDetailsWrapper]: error occured during TaskDetail logging. Unable to get inner TO: " + field.getName() + " from: " + object.getClass(), e);
            }
            return;
        } else if (field.isAnnotationPresent(LoggableDetail.class)) {
            logDetail(object, field, module);

        }
    }

    private void searchFromTOObject(Object robot, String module) {
        FieldUtils.getFieldsListWithAnnotation(robot.getClass(), LoggableDetail.class).stream().forEach(field -> {
            searchForDetails(robot, field, module, true);
        });
    }

    private void searchForRecordUuid(Object robot, Field field) {
        try {
            field.setAccessible(true);
            Object innerObject = field.get(robot);
            setRecordUuid((BaseTO) innerObject);
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
            logger.error("[LoggerDetailsWrapper]: error occured during TaskDetail logging. Unable to get recordUuid from TO: " + field.getName() + " from: " + robot.getClass(), e);
        }
    }

    private void setRecordUuid(BaseTO object) {
        try {
            String recortUuid = object.getRecordUuid();
            RunnerContext.setRecordUuid(recortUuid);
        } catch (IllegalArgumentException | SecurityException e) {
            logger.error("[LoggerDetailsWrapper]: error occured during TaskDetail logging. Unable to get recordUuid from TO: " + object.getClass(), e);
        }
    }

}
