package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibagroup.wf.intelia.core.annotations.OnError;
import com.ibagroup.wf.intelia.core.annotations.PrePerform;
import com.ibagroup.wf.intelia.core.exceptions.ExceptionHandler;
import com.ibagroup.wf.intelia.core.metadata.MetadataManager;
import com.ibagroup.wf.intelia.core.metadata.storage.MetadataPermanentStorage;
import com.ibagroup.wf.intelia.core.metadata.types.LoggingMetadata;
import com.ibagroup.wf.intelia.core.robots.RobotProtocol;
import com.ibagroup.wf.intelia.core.utils.MethodUtils;

public class PerformMethodWrapper extends ChainMethodWrapper {

    private static final Logger logger = LoggerFactory.getLogger(PerformMethodWrapper.class);
    public final static Predicate<Method> isPerformMethod = m -> "perform".equalsIgnoreCase(m.getName());

    private final boolean uploadAfterEachPerform;
    private final boolean uploadAfterFailure;
    private final boolean doNotReThrowException;
    private final MetadataPermanentStorage metadataPermanentStorage;
    private final ExceptionHandler exceptionHandler;
    private final MetadataManager metadataManager;


    public PerformMethodWrapper(boolean uploadAfterEachPerform, boolean uploadAfterFailure, boolean doNotReThrowException, MetadataPermanentStorage metadataPermanentStorage,
            ExceptionHandler exceptionHandler, MetadataManager metadataManager) {
        this.uploadAfterEachPerform = uploadAfterEachPerform;
        this.uploadAfterFailure = uploadAfterFailure;

        this.metadataPermanentStorage = metadataPermanentStorage;
        this.exceptionHandler = exceptionHandler;
        this.metadataManager = metadataManager;
        this.doNotReThrowException = doNotReThrowException;

    }

    public boolean isHandled(Method m) {
        return isPerformMethod.test(m);
    }

    @Override
    public Object wrap(Invocation invocation) throws Throwable {

        try {
            MethodUtils.findAndInvokeAllMethodsWithAnnotation(invocation.getSelf(), PrePerform.class);
        } catch (Throwable e) {
            logger.error("A @PrePerform method failed", e);
        }

        try {
            // invoke next in chain
            Object result = invokeInner(invocation);

            RunnerContext.setRecordUuid(RunnerContextHelper.extractRecordUuid(invocation.getSelf()));

            // === Workaround solution for BULK mechanism ===
            // All records exceptions are catch by Robot itself and add metadata with name
            // "stacktrace".
            // If such metadata exists once all records were processed then we need to upload
            // methadata to S3.
            // ==============================================
            boolean isFailureFound = false;
            if (metadataManager != null) {
                isFailureFound = metadataManager.getMetadataList().stream().anyMatch(metadata -> {
                    return metadata.getName().contains("stacktrace");
                });
            }
            if (metadataPermanentStorage != null && (uploadAfterEachPerform || (uploadAfterFailure && isFailureFound))) {
                metadataPermanentStorage.storeAllMetadata(null);
            }
            return result;

        } catch (Throwable throwable) {

            String errMessage = ExceptionUtils.getMessage(throwable) + ": " + ExceptionUtils.getStackTrace(throwable);

            logger.error("Perform failed", throwable);
            if (null != exceptionHandler) {
                exceptionHandler.logCurrentException(throwable);
            }

            if (null != metadataManager) {
                metadataManager.addMetadata(new LoggingMetadata("stacktrace", errMessage));
            }

            try {
                RobotProtocol robot = (RobotProtocol) invocation.getSelf();
                robot.storeCurrentMetadata();
                logger.info("Robot stored current metadata after exception: " + throwable.getMessage());
            } catch (Throwable e) {
                logger.error("Robot failed to store current metadata", e);
            }

            // Run @OnError
            Object onErrorResult = null;
            try {
                Optional<Method> any = MethodUtils.findAnyMehodHavingAnnotation(invocation.getSelf().getClass(), OnError.class);
                if (any.isPresent()) {
                    Method method = any.get();

                    if (method.getParameterCount() > 1) {
                        Object[] newArgs =
                                ArrayUtils.add(new Object[] {invocation.getSelf(), invocation.getMethod(), invocation.getProceed(), throwable}, invocation.getArgs());
                        onErrorResult = method.invoke(invocation.getSelf(), newArgs);
                    } else if (method.getParameterCount() == 1) {
                        onErrorResult = method.invoke(invocation.getSelf(), throwable);
                    } else {
                        onErrorResult = method.invoke(invocation.getSelf());
                    }
                }
            } catch (Throwable e) {
                logger.error("@OnError failed", e);
            }


            if (uploadAfterFailure && null != metadataPermanentStorage) {
                metadataPermanentStorage.storeAllMetadata(null);
            }

            if (!doNotReThrowException) {
                throw throwable;
            }
            return onErrorResult != null ? onErrorResult : RobotsFactoryHelper.defaultReturnValue(invocation.getMethod());
        }
    }

}
