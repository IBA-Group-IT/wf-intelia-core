package com.ibagroup.wf.intelia.core.robots.factory;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.ibagroup.wf.intelia.core.CommonConstants;
import com.ibagroup.wf.intelia.core.FlowContext;
import com.ibagroup.wf.intelia.core.InteliaBuilder;
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
import com.ibagroup.wf.intelia.core.security.SecurityUtils;
import com.ibagroup.wf.intelia.core.storage.S3Manager;
import com.ibagroup.wf.intelia.core.storage.StorageManager;
import groovy.lang.Binding;

/**
 * Default use:
 * 
 * <pre>
 * RobotsFactory robotsFactory = new RobotsFactoryBuilder(binding).defaultSetup().build();
 * </pre>
 * 
 * @deprecated {@link InteliaBuilder} instead.
 */
@Deprecated
public class RobotsFactoryBuilder {

    private boolean handlersInsideBinding = false;

    private Binding binding;
    private FlowContext flowContext;
    private ExceptionHandler exceptionHandler;
    private ConfigurationManager configurationManager;
    private MetadataManager metadataManager;
    private MetadataPermanentStorage metadataPermanentStorage;
    private IRobotLogger robotLogger;
    private SecurityUtils securityUtils;

    private boolean doNotReThrowException = false;

    // method wrappers options
    private boolean logMethodCallStats = false;
    private boolean logRobotDetailsAtPerform = false;
    private boolean tweakPerform = false;
    private boolean tweakSecurity = false;

    public RobotsFactoryBuilder(Binding binding) {
        // binding is mandatory for builder
        this.binding = binding;
        this.flowContext = new FlowContext(binding);
        this.securityUtils = new SecurityUtils(binding);
    }

    /**
     * Does following default setup
     * <ul>
     * <li>cfgManager - {@link DataStoreConfiguration}, from <b>rpa_config_ds</b></li>
     * <li>activityManager - {@link MetadataListManager}</li>
     * <li>activitiesStorage - {@link MetadataStorage}</li>
     * <li>robotLogger - {@link RobotLogger}</li>
     * <li>exceptionHandler - {@link DefaultExceptionHandler}</li>
     * <li>re-throw exception - <b>false</b></li>
     * <li>method wrappers - all included</li>
     * </ul>
     */
    public RobotsFactoryBuilder defaultSetup() {
        return defaultSetup(false, null);
    }

    /**
     * Does following default setup
     * <ul>
     * <li>cfgManager - {@link DataStoreConfiguration}</li>
     * <li>activityManager - {@link MetadataListManager}</li>
     * <li>activitiesStorage - {@link MetadataStorage}</li>
     * <li>robotLogger - {@link RobotLogger}</li>
     * <li>exceptionHandler - {@link DefaultExceptionHandler}</li>
     * <li>re-throw exception - <b>false</b></li>
     * <li>method wrappers - all included</li>
     * </ul>
     */
    public RobotsFactoryBuilder defaultSetup(String dsName) {
        return defaultSetup(false, dsName);
    }

    /**
     * Does following default setup
     * <ul>
     * <li>cfgManager - {@link DataStoreConfiguration}, from <b>rpa_config_ds</b></li>
     * <li>activityManager - {@link MetadataListManager}</li>
     * <li>activitiesStorage - {@link MetadataStorage}</li>
     * <li>robotLogger - {@link RobotLogger}</li>
     * <li>exceptionHandler - {@link DefaultExceptionHandler}</li>
     * <li>method wrappers - all included</li>
     * </ul>
     */
    public RobotsFactoryBuilder defaultSetup(boolean throwException) {
        return defaultSetup(throwException, null);
    }

    /**
     * Does following default setup
     * <ul>
     * <li>cfgManager - {@link DataStoreConfiguration}, from <b>rpa_config_ds</b></li>
     * <li>activityManager - {@link MetadataListManager}</li>
     * <li>activitiesStorage - {@link MetadataStorage}</li>
     * <li>robotLogger - {@link RobotLogger}</li>
     * <li>exceptionHandler - {@link DefaultExceptionHandler}</li>
     * <li>method wrappers - all included</li>
     * </ul>
     */
    public RobotsFactoryBuilder defaultSetup(boolean throwException, String dsName) {
        return defaultCfg(dsName).defaultActivityMgr().defaultActivitiesStorage(configurationManager).defaultRobotLogger(configurationManager).defaultExHandler()
                .setDoNotReThrowException(!throwException).addAllMethodWrappers();

    }

    /**
     * Does following default setup
     * <ul>
     * <li>cfgManager - {@link DataStoreConfiguration}</li>
     * <li>activityManager - {@link MetadataListManager}</li>
     * <li>activitiesStorage - {@link MetadataStorage}</li>
     * <li>method wrappers - none included</li>
     * </ul>
     */
    public RobotsFactoryBuilder miniSetup(boolean throwException, String dsName) {
        return defaultCfg(dsName).defaultActivityMgr().defaultActivitiesStorage(configurationManager);
    }

    @SuppressWarnings("unchecked")
    public RobotsFactory build() {
        boolean debugMode = configurationManager.getConfigItem(CommonConstants.DEBUG_MODE_ON, false, Formatter.BOOLEAN);
        boolean uploadAfterEachPerform = debugMode;
        // if debugMode is ON then upload metadata to S3 in any case
        boolean uploadAfterFailure = true;
        if (!debugMode) {
            uploadAfterFailure = configurationManager.getConfigItem(CommonConstants.UPLOAD_AFTER_FAILURE, true, Formatter.BOOLEAN);
        }

        // try set robotic env from binding if not set directly
        if (handlersInsideBinding && null != binding) {
            binding.getVariables().forEach((key, value) -> {
                if (null == exceptionHandler && value instanceof ExceptionHandler) {
                    exceptionHandler = (ExceptionHandler) value;
                }
                if (null == configurationManager && value instanceof ConfigurationManager) {
                    configurationManager = (ConfigurationManager) value;
                }
                if (null == metadataManager && value instanceof MetadataManager) {
                    metadataManager = (MetadataManager) value;
                }
            });
        }


        // set MATRYOSHKA chain of method wrappers
        // ORDER IS IMPORTANT !!!
        // OUTERMOST starts first and ends last
        ChainMethodWrapper chainMethodWrapper = null;
        if (logMethodCallStats || logRobotDetailsAtPerform) {
            // will call robotLogger.storeLogs() last at the perform processing
            chainMethodWrapper = new StoreLogsAtExitMethodWrapper(robotLogger);
        }

        if (logRobotDetailsAtPerform) {
            chainMethodWrapper = setInner(chainMethodWrapper, new LoggerDetailsWrapper(robotLogger));
        }

        if (logMethodCallStats) {
            chainMethodWrapper = setInner(chainMethodWrapper, new LoggerMethodWrapper(robotLogger));
        }

        if (tweakPerform) {
            chainMethodWrapper = setInner(chainMethodWrapper,
                    new PerformMethodWrapper(uploadAfterEachPerform, uploadAfterFailure, doNotReThrowException, metadataPermanentStorage, exceptionHandler, metadataManager));
        }

        if (tweakSecurity) {
            chainMethodWrapper = setInner(chainMethodWrapper, new SecurityMethodWrapper(securityUtils));
        }

        return new RobotsFactory(getMapOfWiredObjects(), chainMethodWrapper != null ? chainMethodWrapper.getOuterMost() : null);
    }

    private ChainMethodWrapper setInner(ChainMethodWrapper chainMethodWrapper, ChainMethodWrapper inner) {
        if (chainMethodWrapper == null) {
            return inner;
        } else {
            return chainMethodWrapper.setInner(inner);
        }
    }

    private Map<Class<?>, Object> getMapOfWiredObjects() {
        return Arrays.asList(exceptionHandler, configurationManager, metadataManager, robotLogger, binding, flowContext, securityUtils).stream().filter(item -> item != null)
                .filter(item -> null != item)
                .collect(Collectors.toMap(value -> value.getClass(), value -> value, (value1, value2) -> value1));
    }

    public RobotsFactoryBuilder useBingingToFetchHandlers() {
        this.handlersInsideBinding = true;
        return this;
    }

    public RobotsFactoryBuilder setDoNotReThrowException(boolean doNotReThrowException) {
        this.doNotReThrowException = doNotReThrowException;
        return this;
    }

    public RobotsFactoryBuilder doNotReThrowException() {
        return setDoNotReThrowException(true);
    }

    public RobotsFactoryBuilder setExHandler(ExceptionHandler exHandler) {
        this.exceptionHandler = exHandler;
        return this;
    }

    public RobotsFactoryBuilder defaultExHandler() {
        return setExHandler(new DefaultExceptionHandler(flowContext));
    }

    public RobotsFactoryBuilder setCfg(ConfigurationManager cfg) {
        this.configurationManager = cfg;
        return this;
    }

    public RobotsFactoryBuilder defaultCfg(String dsName) {
        return setCfg(new DataStoreConfiguration(binding, dsName));
    }

    public RobotsFactoryBuilder setActivityMgr(MetadataManager activityMgr) {
        this.metadataManager = activityMgr;
        return this;
    }

    public RobotsFactoryBuilder defaultActivityMgr() {
        return setActivityMgr(new MetadataListManager());
    }

    public RobotsFactoryBuilder setActivitiesStorage(MetadataPermanentStorage activitiesStorage) {
        this.metadataPermanentStorage = activitiesStorage;
        return this;
    }

    public RobotsFactoryBuilder defaultActivitiesStorage(ConfigurationManager cfg) {
        StorageManager storageMgr = new S3Manager(binding, cfg.getConfigItem("screenshots_bucket"), cfg.getConfigItem("screenshots_folder"));
        return setActivitiesStorage(new MetadataStorage(binding, storageMgr, metadataManager, () -> {
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
        }));
    }

    public RobotsFactoryBuilder setRobotLogger(IRobotLogger robotLogger) {
        this.robotLogger = robotLogger;
        return this;
    }
    
    public RobotsFactoryBuilder defaultRobotLogger(ConfigurationManager cfg) {
       return setRobotLogger(new RobotLogger(binding, cfg.getConfigItem("bp_actions"), cfg.getConfigItem("bp_details")));
    }

    public RobotsFactoryBuilder logMethodCallStats() {
        logMethodCallStats = true;
        return this;
    }

    public RobotsFactoryBuilder logRobotDetailsAtPerform() {
        logRobotDetailsAtPerform = true;
        return this;
    }

    public RobotsFactoryBuilder tweakPerform() {
        tweakPerform = true;
        return this;
    }

    public RobotsFactoryBuilder tweakSecurity() {
        tweakSecurity = true;
        return this;
    }

    public RobotsFactoryBuilder addAllMethodWrappers() {
        return logMethodCallStats().logRobotDetailsAtPerform().tweakPerform().tweakSecurity();
    }

    public RobotsFactoryBuilder addOnlyMethodAndDetailsLogging() {
        return logMethodCallStats().logRobotDetailsAtPerform();
    }

    public RobotsFactoryBuilder onlyTweakPerform() {
        return tweakPerform();
    }

    public RobotsFactoryBuilder onlyTweakPerformAndSecurity() {
        return tweakPerform().tweakSecurity();
    }

}
