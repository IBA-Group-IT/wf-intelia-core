package com.ibagroup.wf.intelia.core;

import static com.ibagroup.wf.intelia.core.config.DataStoreConfiguration.RPA_CONFIG_DS;
import static com.ibagroup.wf.intelia.core.mis.RobotLogger.BP_ACTIONS_DS_NAME_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.mis.RobotLogger.BP_DETAILS_DS_NAME_PARAM_NAME;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.codejargon.feather.Provides;
import org.slf4j.Logger;
import org.webharvest.utils.SystemUtilities;
import com.freedomoss.crowdcontrol.webharvest.WebHarvestConstants;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager.Formatter;
import com.ibagroup.wf.intelia.core.config.DataStoreConfiguration;
import com.ibagroup.wf.intelia.core.exceptions.DefaultExceptionHandler;
import com.ibagroup.wf.intelia.core.exceptions.ExceptionHandler;
import com.ibagroup.wf.intelia.core.metadata.MetadataListManager;
import com.ibagroup.wf.intelia.core.metadata.MetadataManager;
import com.ibagroup.wf.intelia.core.metadata.storage.MetadataPermanentStorage;
import com.ibagroup.wf.intelia.core.metadata.storage.MetadataStorage;
import com.ibagroup.wf.intelia.core.mis.IRobotLogger;
import com.ibagroup.wf.intelia.core.mis.RobotLogger;
import com.ibagroup.wf.intelia.core.robots.factory.ChainMethodWrapper;
import com.ibagroup.wf.intelia.core.robots.factory.LoggerDetailsWrapper;
import com.ibagroup.wf.intelia.core.robots.factory.LoggerMethodWrapper;
import com.ibagroup.wf.intelia.core.robots.factory.PerformMethodWrapper;
import com.ibagroup.wf.intelia.core.robots.factory.SecurityMethodWrapper;
import com.ibagroup.wf.intelia.core.robots.factory.StoreLogsAtExitMethodWrapper;
import com.ibagroup.wf.intelia.core.storage.S3Manager;
import com.ibagroup.wf.intelia.core.storage.StorageManager;
import groovy.lang.Binding;

/**
 * Feather Injectable Context Initialization Module. Adds default set of Core beans into context:
 * <ul>
 * <li>cfgManager - {@link DataStoreConfiguration}, from <b>rpa_config_ds</b> param</li>
 * <li>metadataManager - {@link MetadataListManager}</li>
 * <li>metadataStorage - S3 {@link MetadataStorage}</li>
 * <li>robotLogger - {@link RobotLogger}</li>
 * <li>exceptionHandler - {@link DefaultExceptionHandler}</li>
 * <li>re-throw exception - from <b>doNotReThrowException</b> param</li>
 * <li>method wrappers - all included</li>
 * </ul>
 * 
 * @author dmitriev
 *
 */
public class CoreModule implements Module {
    public final static String BOT_CONFIG_PARAMS_PARAM_NAME = "botConfigParams";
    public final static String DO_NOT_RETHROW_EXCEPTION_PARAM_NAME = "doNotReThrowException";

    private final Binding context;
    private Map<String, String> params;
    private Provider<Injector> injectorProvider;

    public CoreModule(Binding context, final Map<String, String> botConfigParams, Provider<Injector> injectorProvider) {
        this.context = context;
        this.params = botConfigParams;
        this.injectorProvider = injectorProvider;
    }

    @Provides
    public Binding binding() {
        return context;
    }

    @Provides
    @Singleton
    public FlowContext flowContext() {
        return new FlowContext(context);
    }

    @Provides
    @Singleton
    public Logger logger() {
        return (Logger) context.getVariable(WebHarvestConstants.LOG);
    }

    @Provides
    @Named(BOT_CONFIG_PARAMS_PARAM_NAME)
    public Map<String, String> params() {
        return params;
    }

    @Provides
    @Singleton
    public SystemUtilities sys() {
        return (SystemUtilities) context.getVariable(WebHarvestConstants.SYS);
    }

    @Provides
    @Singleton
    public MetadataManager metadataManager(MetadataListManager metadataListManager) {
        return metadataListManager;
    }

    @Provides
    @Named(RPA_CONFIG_DS)
    public String configDSName() {
        return params.get(RPA_CONFIG_DS);
    }

    @Provides
    @Singleton
    public ConfigurationManager configurationManager(DataStoreConfiguration dataStoreConfigManager) {
        return dataStoreConfigManager;
    }

    @Provides
    @Singleton
    public MetadataPermanentStorage metadataPermanentStorage(MetadataManager metadataManager, ConfigurationManager cfg, FlowContext flowContext) {
        StorageManager storageMgr = new S3Manager(context, cfg.getConfigItem("screenshots_bucket"), cfg.getConfigItem("screenshots_folder"));
        return new MetadataStorage(context, storageMgr, metadataManager, () -> {
            String taskName = flowContext.getTaskName().equals("WWI TODO: Config name should be here") ? "local-task" : flowContext.getTaskName();

            String submissionId = flowContext.getSubmissionId();
            // String processGuid = BindingUtils.getPropertyValue(binding, PROCESS_UUID);
            String processGuid = flowContext.getProcessGuid();
            String taskPath = "";

            if (StringUtils.isNotBlank(processGuid)) {
                taskPath += processGuid + "/";
            }

            if (StringUtils.isNotBlank(submissionId)) {
                taskPath += submissionId + "/";
            }

            taskPath += taskName;
            return taskPath;
        });
    }

    @Provides
    @Named(BP_ACTIONS_DS_NAME_PARAM_NAME)
    public String bpActionsDSName(ConfigurationManager cfg) {
        return params.containsKey(BP_ACTIONS_DS_NAME_PARAM_NAME) ? params.get(BP_ACTIONS_DS_NAME_PARAM_NAME) : cfg.getConfigItem(BP_ACTIONS_DS_NAME_PARAM_NAME);
    }

    @Provides
    @Named(BP_DETAILS_DS_NAME_PARAM_NAME)
    public String bpDetailsDSName(ConfigurationManager cfg) {
        return params.containsKey(BP_DETAILS_DS_NAME_PARAM_NAME) ? params.get(BP_DETAILS_DS_NAME_PARAM_NAME) : cfg.getConfigItem(BP_DETAILS_DS_NAME_PARAM_NAME);
    }

    @Provides
    @Singleton
    public IRobotLogger robotLogger(RobotLogger robotLogger) {
        return robotLogger;
    }

    @Provides
    @Singleton
    public ExceptionHandler exceptionHandler(DefaultExceptionHandler defaultExceptionHandler) {
        return defaultExceptionHandler;
    }

    /**
     * !Provides Injector to anyone interested!
     */
    @Provides
    @Singleton
    public Injector injector() {
        return injectorProvider.get();
    }

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
    public boolean doNotReThrowException(ConfigurationManager cfg) {
        return params.containsKey(DO_NOT_RETHROW_EXCEPTION_PARAM_NAME) ? Boolean.parseBoolean(params.get(DO_NOT_RETHROW_EXCEPTION_PARAM_NAME)) : false;
    }

    @Provides
    @Singleton
    public ChainMethodWrapper chainMethodWrapper(IRobotLogger robotLogger, MetadataManager metadataManager, ExceptionHandler exceptionHandler,
            MetadataPermanentStorage metadataPermanentStorage, @Named("uploadAfterEachPerform") boolean uploadAfterEachPerform,
            @Named("uploadAfterFailure") boolean uploadAfterFailure, @Named(DO_NOT_RETHROW_EXCEPTION_PARAM_NAME) boolean doNotReThrowException) {
        // set MATRYOSHKA chain of method wrappers
        // ORDER IS IMPORTANT !!!
        // OUTERMOST starts first and ends last
        return new StoreLogsAtExitMethodWrapper(robotLogger).setInner(new LoggerDetailsWrapper(robotLogger)).setInner(new LoggerMethodWrapper(robotLogger))
                .setInner(new PerformMethodWrapper(uploadAfterEachPerform, uploadAfterFailure, doNotReThrowException, metadataPermanentStorage, exceptionHandler, metadataManager))
                .setInner(new SecurityMethodWrapper(context)).getOuterMost();

    }
}