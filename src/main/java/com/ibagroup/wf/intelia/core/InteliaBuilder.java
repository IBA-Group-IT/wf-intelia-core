package com.ibagroup.wf.intelia.core;

import static com.ibagroup.wf.intelia.core.PerformMethodWrapperModule.DO_NOT_RETHROW_EXCEPTION_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.config.DataStoreConfiguration.RPA_CONFIG_DS;
import static com.ibagroup.wf.intelia.core.datastore.BaseDS.DEFAULT_DATASTORE_PARAM_NAME;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableMap;
import com.ibagroup.wf.intelia.core.config.DataStoreConfiguration;
import com.ibagroup.wf.intelia.core.config.MapConfiguration;
import com.ibagroup.wf.intelia.core.exceptions.DefaultExceptionHandler;
import com.ibagroup.wf.intelia.core.metadata.storage.MetadataStorage;
import com.ibagroup.wf.intelia.core.mis.RobotLogger;
import com.ibagroup.wf.intelia.core.robots.factory.PerformMethodWrapper;
import com.workfusion.intake.core.Module;
import groovy.lang.Binding;

/**
 * Intelia APP builder.
 * <p>
 * Default use:
 * 
 * <pre>
 * Intelia intelia = new InteliaBuilder(binding).defaultSetup().get();
 * </pre>
 * </p>
 * 
 * @see Intelia#init(Binding)
 * @see Intelia#defaultInteliaSetup(Binding)
 * @see Intelia#defaultInteliaSetup(Binding, boolean)
 * @see Intelia#defaultInteliaSetup(Binding, String)
 * @see Intelia#miniInteliaSetup(Binding)
 * @see Intelia#microInteliaSetup(Binding)
 * @see Intelia#nanoInteliaSetup(Binding)
 * 
 * @author dmitriev
 *
 */
public class InteliaBuilder<T extends InteliaBuilder<T>> {
    private Binding context;
    private Map<String, String> params = new HashMap<>();
    private Set<Module> overrideModules = new HashSet<>();
    private Set<Module> additionalModules = new HashSet<>();
    private Object injectContext;

    public InteliaBuilder(Binding context) {
        this.context = context;
    }

    public T params(Map<String, String> params) {
        this.params.putAll(params);
        return (T) this;
    }

    /**
     * Adds additional module(s) instances to the injection context
     */
    public T additional(Module... modules) {
        additionalModules.addAll(Arrays.asList(modules));
        return (T) this;
    }

    /**
     * Adds additional module(s) classes to the injection context.
     * <p>
     * <b>Module class must have no-args constructor for auto-instantiation</b>
     * </p>
     */
    public T additional(Class<? extends Module>... modules) {
        additionalModules.addAll(Arrays.stream(modules).map(c -> {
            try {
                return c.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Failed to instantiate additional module " + c.getSimpleName(), e);
            }
        }).map(Module.class::cast).collect(Collectors.toSet()));
        return (T) this;
    }

    /**
     * Adds override module(s) instances to the injection context
     */
    public T override(Module... modules) {
        overrideModules.addAll(Arrays.asList(modules));
        return (T) this;
    }

    public T injectFields(Object context) {
        this.injectContext = context;
        return (T) this;
    }

    /**
     * Does following default setup
     * <ul>
     * <li>cfgManager - {@link DataStoreConfiguration}, from <b>rpa_config_ds</b> param</li>
     * <li>metadataPermamntStorage - S3 {@link MetadataStorage}</li>
     * <li>robotLogger - {@link RobotLogger}</li>
     * <li>exceptionHandler - {@link DefaultExceptionHandler}</li>
     * <li>method wrappers - all included</li>
     * <li>doNotRethrowException - from <b>doNotRethrowException</b> param
     * </ul>
     */
    public T defaultSetup() {
        return miniSetup().additional(RobotLoggerModule.class, S3MetadataPermanentStorageModule.class, DefaultExceptionHandlerModule.class);
    }



    /**
     * Shortcut for {@code builder.params(ImmutableMap.of(RPA_CONFIG_DS, configDsName))}
     */
    public T defaultDatastore(String dsName) {
        return additional(DefaultDataStoreModule.class).params(ImmutableMap.of(DEFAULT_DATASTORE_PARAM_NAME, dsName));
    }


    /**
     * Shortcut for {@code builder.params(ImmutableMap.of(RPA_CONFIG_DS, configDsName))}
     */
    public T configFromDatastore(String configDsName) {
        return params(ImmutableMap.of(RPA_CONFIG_DS, configDsName));
    }

    /**
     * Shortcut for
     * {@code builder.params(ImmutableMap.of(DO_NOT_RETHROW_EXCEPTION_PARAM_NAME, String.valueOf(doNotRethrowException)))}
     */
    public T doNotRethrowException(boolean doNotRethrowException) {
        return params(ImmutableMap.of(DO_NOT_RETHROW_EXCEPTION_PARAM_NAME, String.valueOf(doNotRethrowException)));
    }

    /**
     * Does following mini setup
     * <ul>
     * <li>cfgManager - {@link DataStoreConfiguration}, from <b>rpa_config_ds</b> config param</li>
     * <li>metadataPermamntStorage - none</li>
     * <li>robotLogger - none</li>
     * <li>exceptionHandler - none</li>
     * <li>method wrappers - {@link PerformMethodWrapper}</li>
     * </ul>
     */
    public T miniSetup() {
        return additional(DataStoreConfigurationManagerModule.class, PerformMethodWrapperModule.class);
    }

    /**
     * Does following micro setup
     * <ul>
     * <li>cfgManager - {@link MapConfiguration}, from {@link #params(Map)}</li>
     * <li>metadataPermamntStorage - none</li>
     * <li>robotLogger - none</li>
     * <li>exceptionHandler - none</li>
     * <li>method wrappers - {@link PerformMethodWrapper}</li>
     * </ul>
     */
    public T microSetup() {
        return nanoSetup().additional(PerformMethodWrapperModule.class);
    }

    /**
     * Does following nano setup
     * <ul>
     * <li>cfgManager - {@link MapConfiguration}, from {@link #params(Map)}</li>
     * <li>metadataPermamntStorage - none</li>
     * <li>robotLogger - none</li>
     * <li>exceptionHandler - none</li>
     * <li>method wrappers - none</li>
     * </ul>
     */
    public T nanoSetup() {
        return additional(ParamsConfigurationManagerModule.class);
    }

    public Intelia get() {
        return new Intelia(context, params, additionalModules, overrideModules, injectContext);
    }

    public Binding getContext() {
        return context;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Set<Module> getOverrideModules() {
        return overrideModules;
    }

    public Set<Module> getAdditionalModules() {
        return additionalModules;
    }

    public Object getInjectContext() {
        return injectContext;
    }

}
