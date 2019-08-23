package com.ibagroup.wf.intelia.core;

import java.util.Map;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.codejargon.feather.Provides;
import org.slf4j.Logger;
import org.webharvest.utils.SystemUtilities;
import com.freedomoss.crowdcontrol.webharvest.WebHarvestConstants;
import com.ibagroup.wf.intelia.core.config.DataStoreConfiguration;
import com.ibagroup.wf.intelia.core.exceptions.DefaultExceptionHandler;
import com.ibagroup.wf.intelia.core.metadata.MetadataListManager;
import com.ibagroup.wf.intelia.core.metadata.MetadataManager;
import com.ibagroup.wf.intelia.core.metadata.storage.MetadataStorage;
import com.ibagroup.wf.intelia.core.mis.RobotLogger;
import com.ibagroup.wf.intelia.core.security.SecurityUtils;
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

    private final Binding binding;
    private Map<String, String> params;
    private Provider<Injector> injectorProvider;

    public CoreModule(Binding context, final Map<String, String> botConfigParams, Provider<Injector> injectorProvider) {
        this.binding = context;
        this.params = botConfigParams;
        this.injectorProvider = injectorProvider;
    }

    @Provides
    public Binding binding() {
        return binding;
    }

    @Provides
    @Singleton
    public FlowContext flowContext() {
        return new FlowContext(binding);
    }

    @Provides
    @Singleton
    public Logger logger() {
        return (Logger) binding.getVariable(WebHarvestConstants.LOG);
    }

    @Provides
    @Named(BOT_CONFIG_PARAMS_PARAM_NAME)
    public Map<String, String> params() {
        return params;
    }

    @Provides
    @Singleton
    public SystemUtilities sys() {
        return (SystemUtilities) binding.getVariable(WebHarvestConstants.SYS);
    }

    @Provides
    @Singleton
    public MetadataManager metadataManager(MetadataListManager metadataListManager) {
        return metadataListManager;
    }

    @Provides
    @Singleton
    public SecurityUtils securityUtils() {
        return new SecurityUtils(binding);
    }

    /**
     * !Provides Injector to anyone interested!
     */
    @Provides
    @Singleton
    public Injector injector() {
        return injectorProvider.get();
    }

}
