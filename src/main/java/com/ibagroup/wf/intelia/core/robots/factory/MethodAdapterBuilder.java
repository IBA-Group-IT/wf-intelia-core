package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;

import com.ibagroup.wf.intelia.core.exceptions.ExceptionHandler;
import com.ibagroup.wf.intelia.core.metadata.MetadataManager;
import com.ibagroup.wf.intelia.core.metadata.storage.MetadataPermanentStorage;
import com.ibagroup.wf.intelia.core.mis.IRobotLogger;

import groovy.lang.Binding;

public class MethodAdapterBuilder {

    private final Binding binding;
    private final boolean uploadAfterEachPerform;
    private final boolean uploadAfterFailure;
    private MetadataPermanentStorage metadataPermanentStorage;
    private ExceptionHandler exceptionHandler;
    private MetadataManager metadataManager;
    private IRobotLogger robotLogger;
    private final boolean doNotThrowException;
    private MethodWrapperBuilder methodWrapperBuilder;

    public MethodAdapterBuilder(Binding binding, boolean uploadAfterEachPerform, boolean uploadAfterFailure, boolean doNotThrowException,
            MetadataPermanentStorage metadataPermanentStorage, ExceptionHandler exceptionHandler, MetadataManager metadataManager, IRobotLogger robotLogger,
            MethodWrapperBuilder methodWrapperBuilder) {
        this.binding = binding;
        this.doNotThrowException = doNotThrowException;
        this.uploadAfterEachPerform = uploadAfterEachPerform;
        this.uploadAfterFailure = uploadAfterFailure;
        this.metadataPermanentStorage = metadataPermanentStorage;
        this.exceptionHandler = exceptionHandler;
        this.metadataManager = metadataManager;
        this.robotLogger = robotLogger;
        this.methodWrapperBuilder = methodWrapperBuilder;
    }

    MethodAdapter build(Method method) throws Exception {
        //hardcoded logic to identify if the method is "perform"
        if (PerformMethodAdapter.isHandled(method)) {
            return PerformMethodAdapter.class.getConstructor(boolean.class, boolean.class, boolean.class, Binding.class, MetadataPermanentStorage.class, ExceptionHandler.class,
                    MetadataManager.class, IRobotLogger.class, MethodWrapperBuilder.class).newInstance(uploadAfterEachPerform, uploadAfterFailure, doNotThrowException, binding,
                            metadataPermanentStorage, exceptionHandler, metadataManager, robotLogger, methodWrapperBuilder);
        } else {
            return GenericMethodAdapter.class.getConstructor(MethodWrapperBuilder.class).newInstance(methodWrapperBuilder);
        }
    }
}
