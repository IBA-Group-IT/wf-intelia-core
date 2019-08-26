package com.ibagroup.wf.intelia.core;

import static com.ibagroup.wf.intelia.core.CoreModule.BOT_CONFIG_PARAMS_PARAM_NAME;
import java.util.Map;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.codejargon.feather.Provides;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager.Formatter;
import com.ibagroup.wf.intelia.core.exceptions.ExceptionHandler;
import com.ibagroup.wf.intelia.core.metadata.MetadataManager;
import com.ibagroup.wf.intelia.core.metadata.storage.MetadataPermanentStorage;
import com.ibagroup.wf.intelia.core.mis.IRobotLogger;
import com.ibagroup.wf.intelia.core.robots.factory.ChainMethodWrapper;
import com.ibagroup.wf.intelia.core.robots.factory.LoggerDetailsWrapper;
import com.ibagroup.wf.intelia.core.robots.factory.LoggerMethodWrapper;
import com.ibagroup.wf.intelia.core.robots.factory.PerformMethodWrapper;
import com.ibagroup.wf.intelia.core.robots.factory.SecurityMethodWrapper;
import com.ibagroup.wf.intelia.core.robots.factory.StoreLogsAtExitMethodWrapper;
import com.ibagroup.wf.intelia.core.security.SecurityUtils;
import com.workfusion.intake.core.Module;

public class PerformMethodWrapperModule implements Module {
    public final static String DO_NOT_RETHROW_EXCEPTION_PARAM_NAME = "doNotReThrowException";

    @Provides
    @Named("uploadAfterEachPerform")
    public boolean uploadAfterEachPerform(ConfigurationManager cfg) {
        boolean debugMode = cfg.getConfigItem(CommonConstants.DEBUG_MODE_ON, false, Formatter.BOOLEAN);
        return debugMode;
    }

    @Provides
    @Named("uploadAfterFailure")
    public boolean uploadAfterFailure(ConfigurationManager cfg) {
        boolean debugMode = cfg.getConfigItem(CommonConstants.DEBUG_MODE_ON, false, Formatter.BOOLEAN);
        // if debugMode is ON then upload metadata to S3 in any case
        boolean uploadAfterFailure = true;
        if (!debugMode) {
            uploadAfterFailure = cfg.getConfigItem(CommonConstants.UPLOAD_AFTER_FAILURE, true, Formatter.BOOLEAN);
        }
        return uploadAfterFailure;
    }

    @Provides
    @Named(DO_NOT_RETHROW_EXCEPTION_PARAM_NAME)
    public boolean doNotReThrowException(ConfigurationManager cfg, @Named(BOT_CONFIG_PARAMS_PARAM_NAME) Map<String, String> params) {
        return params.containsKey(DO_NOT_RETHROW_EXCEPTION_PARAM_NAME) ? Boolean.parseBoolean(params.get(DO_NOT_RETHROW_EXCEPTION_PARAM_NAME)) : false;
    }

    @Provides
    @Singleton
    public ChainMethodWrapper chainMethodWrapper(SecurityUtils securityUtils, MetadataManager metadataManager,
            @Named("uploadAfterEachPerform") boolean uploadAfterEachPerform, @Named("uploadAfterFailure") boolean uploadAfterFailure,
            @Named(DO_NOT_RETHROW_EXCEPTION_PARAM_NAME) boolean doNotReThrowException, @Named("IRobotLogger") Optional<IRobotLogger> robotLogger,
            @Named("ExceptionHandler") Optional<ExceptionHandler> exceptionHandler,
            @Named("MetadataPermanentStorage") Optional<MetadataPermanentStorage> metadataPermanentStorage) {

        // set MATRYOSHKA chain of method wrappers
        // ORDER IS IMPORTANT !!!
        // OUTERMOST starts first and ends last

        ChainMethodWrapper chainMethodWrapper = null;
        if (robotLogger.isPresent()) {
            // will call robotLogger.storeLogs() last at the perform processing
            chainMethodWrapper =
                    new StoreLogsAtExitMethodWrapper(robotLogger.get()).setInner(new LoggerDetailsWrapper(robotLogger.get())).setInner(new LoggerMethodWrapper(robotLogger.get()));
        }

        chainMethodWrapper = setInner(chainMethodWrapper,
                new PerformMethodWrapper(uploadAfterEachPerform, uploadAfterFailure, doNotReThrowException, metadataPermanentStorage.orElse(null), exceptionHandler.orElse(null),
                        metadataManager));

        // TODO decide if that to be configurable
        chainMethodWrapper = setInner(chainMethodWrapper, new SecurityMethodWrapper(securityUtils));

        return chainMethodWrapper.getOuterMost();
    }

    private ChainMethodWrapper setInner(ChainMethodWrapper chainMethodWrapper, ChainMethodWrapper inner) {
        if (chainMethodWrapper == null) {
            return inner;
        } else {
            return chainMethodWrapper.setInner(inner);
        }
    }
}
