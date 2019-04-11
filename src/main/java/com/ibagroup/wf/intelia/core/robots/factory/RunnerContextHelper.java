package com.ibagroup.wf.intelia.core.robots.factory;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibagroup.wf.intelia.core.CommonConstants;
import com.ibagroup.wf.intelia.core.mis.RecordUuid;

public class RunnerContextHelper {

    private static final Logger logger = LoggerFactory.getLogger(RunnerContextHelper.class);
    private static String uuid = CommonConstants.DUMMY_UUID;

    public static String extractRecordUuid(Object robot) {

        FieldUtils.getFieldsListWithAnnotation(robot.getClass(), RecordUuid.class).stream().forEach(field -> {
            if (field.isAnnotationPresent(RecordUuid.class)) {
                field.setAccessible(true);

                try {
                    uuid = (String) field.get(robot);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    logger.error(ExceptionUtils.getMessage(e), e);
                }
            }
        });

        return uuid;
    }

    private RunnerContextHelper() {}
}
