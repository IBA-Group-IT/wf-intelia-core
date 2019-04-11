package com.ibagroup.wf.intelia.core.robots.factory;

import com.ibagroup.wf.intelia.core.exceptions.ExceptionHandler;
import com.ibagroup.wf.intelia.core.metadata.MetadataManager;
import com.ibagroup.wf.intelia.core.metadata.storage.MetadataPermanentStorage;
import com.ibagroup.wf.intelia.core.mis.IRobotLogger;

import groovy.lang.Binding;

public class MethodWrapperBuilder {
    private IRobotLogger robotLogger;

    public MethodWrapperBuilder(Binding binding, boolean uploadAfterEachPerform, boolean doNotThrowException, MetadataPermanentStorage metadataPermanentStorage,
            ExceptionHandler exceptionHandler, MetadataManager metadataManager, IRobotLogger robotLogger) {
        this.robotLogger = robotLogger;
    }

    public MethodWrapperBuilder(IRobotLogger robotLogger) {
        this.robotLogger = robotLogger;
    }

    MethodWrapper build(Class<? extends MethodWrapper> wrapperClass) throws Exception {
        //HARDCODED to keep compatibility with the legacy code 
        if (wrapperClass.equals(PerformMethodWrapper.class)) {
            return LoggerMethodWrapper.class.getConstructor(IRobotLogger.class).newInstance(robotLogger);
        }

        if (wrapperClass.equals(LoggerMethodWrapper.class)) {
            return wrapperClass.getConstructor(IRobotLogger.class).newInstance(robotLogger);
        }

        return null;
    }
}
