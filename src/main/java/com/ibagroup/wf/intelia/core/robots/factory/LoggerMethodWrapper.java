/**
 * 
 */
package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;
import java.util.Date;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibagroup.wf.intelia.core.mis.IRobotLogger;
import com.ibagroup.wf.intelia.core.mis.LoggableMethod;
import com.ibagroup.wf.intelia.core.mis.LoggablePerform;
import com.ibagroup.wf.intelia.core.mis.TaskAction;
import com.ibagroup.wf.intelia.core.mis.TaskAction.Result;

/**
 * Method Wrapper that adds method run stats to the RobotLogger log
 * <ul>
 * <li>triggered by {@link LoggableMethod} annotation</li>
 * <li>triggered by {@link LoggablePerform} annotation</li>
 * </ul>
 * 
 * @author LEIZERONOK_EB
 *
 */
public class LoggerMethodWrapper extends ChainMethodWrapper {

    private static final Logger logger = LoggerFactory.getLogger(LoggerMethodWrapper.class);

    private static final String PERFORM = "perform";

    private IRobotLogger robotLogger;

    public LoggerMethodWrapper(IRobotLogger robotLogger) {
        this.robotLogger = robotLogger;
    }

    @Override
    public boolean isHandled(Method m) {
        return m.isAnnotationPresent(LoggableMethod.class) || m.isAnnotationPresent(LoggablePerform.class);
    }

    @Override
    public Object wrap(Invocation invocation) throws Throwable {
        Long startTime = new Date().getTime();
        String module = null;
        String operation = null;
        boolean transactional = false;
        if (invocation.getMethod().isAnnotationPresent(LoggablePerform.class)) {
            operation = PERFORM;
            module = invocation.getMethod().getAnnotation(LoggablePerform.class).module();
        } else if (invocation.getMethod().isAnnotationPresent(LoggableMethod.class)) {
            operation = invocation.getMethod().getAnnotation(LoggableMethod.class).operation();
            module = invocation.getMethod().getAnnotation(LoggableMethod.class).module();
            transactional = invocation.getMethod().getAnnotation(LoggableMethod.class).transactional();
        }

        try {
            RunnerContext.cleanActionData();

            // invoke next in chain
            Object result = invokeInner(invocation);

            if (CollectionUtils.isNotEmpty(RunnerContext.getLastDescriptions())) {
                String des1 = "";
                try {
                    des1 = RunnerContext.getLastDescriptions().get(0);
                } catch (Exception e) {
                    logger.debug("empty description for TaskAction in RunnerContext");
                }
                String des2 = "";
                try {
                    des2 = RunnerContext.getLastDescriptions().get(1);
                } catch (Exception e) {
                    logger.debug("empty description for TaskAction in RunnerContext");
                }
                String des3 = "";
                try {
                    des3 = RunnerContext.getLastDescriptions().get(2);
                } catch (Exception e) {
                    logger.debug("empty description for TaskAction in RunnerContext");
                }

                TaskAction action = new TaskAction(module, operation, RunnerContext.getLastResult(), startTime, des1, des2, des3, transactional);
                robotLogger.addAction(action);
            } else {
                TaskAction action = new TaskAction(module, operation, RunnerContext.getLastResult(), startTime, transactional);
                robotLogger.addAction(action);
            }
            RunnerContext.cleanActionData();
            return result;
        } catch (Throwable e) {
            TaskAction action =
                    new TaskAction(module, operation, Result.EXCEPTION, startTime, ExceptionUtils.getRootCauseMessage(e), ExceptionUtils.getStackTrace(e), null, transactional);
            robotLogger.addAction(action);
            throw e;
        }

    };



}
