package com.ibagroup.wf.intelia.core.robots.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.ibagroup.wf.intelia.core.CommonConstants;
import com.ibagroup.wf.intelia.core.FlowContext;
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
 */
public class RobotsFactoryBuilder {

    private boolean handlersInsideBinding = false;

    private Binding binding;
    private FlowContext flowContext;
    private ExceptionHandler exceptionHandler;
    private ConfigurationManager configurationManager;
    private MetadataManager metadataManager;
    private MetadataPermanentStorage metadataPermanentStorage;
    private IRobotLogger robotLogger;

    private List<Class<? extends MethodWrapper>> methodWrappers = new ArrayList<>();

    private boolean doNotReThrowException = false;
    private boolean doNotUsePerformAdapter = false;

    private MethodAdapterBuilder methodAdapterBuilder;
    private MethodWrapperBuilder methodWrapperBuilder;

    public RobotsFactoryBuilder(Binding binding) {
        // binding is mandatory for builder
        this.binding = binding;
        this.flowContext = new FlowContext(binding);
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
     * </ul>
     */
    public RobotsFactoryBuilder defaultSetup(boolean throwException) {
        return defaultSetup(throwException, null);
    }

    /**
     * Does following default setup
     * <ul>
     * <li>cfgManager - {@link DataStoreConfiguration}</li>
     * <li>activityManager - {@link MetadataListManager}</li>
     * <li>activitiesStorage - {@link MetadataStorage}</li>
     * <li>robotLogger - {@link RobotLogger}</li>
     * <li>exceptionHandler - {@link DefaultExceptionHandler}</li>
     * </ul>
     */
    public RobotsFactoryBuilder defaultSetup(boolean throwException, String dsName) {
        return defaultCfg(dsName).defaultActivityMgr().defaultActivitiesStorage(configurationManager).defaultRobotLogger(configurationManager).defaultExHandler().setDoNotReThrowException(!throwException);
    }

    @SuppressWarnings("unchecked")
    public RobotsFactory build() {
        if (!doNotUsePerformAdapter) {
            // add default performMethodAdapter
            addMethodWrapper(PerformMethodWrapper.class);
            // add default LoggerMethodWrapper
            addMethodWrapper(LoggerMethodWrapper.class);
        }

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

        methodWrapperBuilder = new MethodWrapperBuilder(robotLogger);
        methodAdapterBuilder = new MethodAdapterBuilder(binding, uploadAfterEachPerform, uploadAfterFailure, doNotReThrowException, metadataPermanentStorage, exceptionHandler,
                metadataManager, robotLogger, methodWrapperBuilder);

        return new RobotsFactory(getMapOfWiredObjects(), methodWrappers, doNotReThrowException, methodAdapterBuilder);
    }


    private Map<Class<?>, Object> getMapOfWiredObjects() {
        return Arrays.asList(exceptionHandler, configurationManager, metadataManager, robotLogger, binding, flowContext).stream().filter(item -> item != null).filter(item -> null != item)
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

    public RobotsFactoryBuilder doNotUsePerformAdapter() {
        this.doNotUsePerformAdapter = true;
        return this;
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

    public RobotsFactoryBuilder addMethodWrapper(Class<? extends MethodWrapper> methodWrapperClass) {
        this.methodWrappers.add(methodWrapperClass);
        return this;
    }

    public RobotsFactoryBuilder addMethodWrappers(List<Class<? extends MethodWrapper>> methodWrapperClasses) {
        this.methodWrappers.addAll(methodWrapperClasses);
        return this;
    }

    public RobotsFactoryBuilder setRobotLogger(IRobotLogger robotLogger) {
        this.robotLogger = robotLogger;
        return this;
    }
    
    public RobotsFactoryBuilder defaultRobotLogger(ConfigurationManager cfg) {
       return setRobotLogger(new RobotLogger(binding, cfg.getConfigItem("bp_actions"), cfg.getConfigItem("bp_details")));
    }

}
