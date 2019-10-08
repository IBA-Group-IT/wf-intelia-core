package com.ibagroup.wf.intelia.core;

import static com.ibagroup.wf.intelia.core.robots.factory.RobotsFactoryHelper.getFieldValue;
import static com.ibagroup.wf.intelia.core.robots.factory.RobotsFactoryHelper.wireField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.codejargon.feather.Feather;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibagroup.wf.intelia.core.annotations.Wire;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.robots.factory.ChainMethodWrapper;
import com.ibagroup.wf.intelia.core.robots.factory.Invocation;
import com.ibagroup.wf.intelia.core.utils.BindingUtils;
import com.workfusion.intake.core.Module;
import groovy.lang.Binding;
import javassist.util.proxy.ProxyFactory;

/**
 * Possible ways of creating a robot with the factory:
 * 
 * <pre>
 *  #1 Robot robot = <Intelia.>init(binding).defaultSetup().get().getInstance(Robot.class);
 *  #2 Robot robot = <Intelia.>defaultFactorySetup(binding).getInstance(Robot.class);
 * </pre>
 * 
 * @see Intelia#init(Binding)
 * @see Intelia#defaultInteliaSetup(Binding)
 * @see Intelia#defaultInteliaSetup(Binding, boolean)
 * @see Intelia#defaultInteliaSetup(Binding, String)
 * @see Intelia#miniInteliaSetup(Binding)
 * @see Intelia#microInteliaSetup(Binding)
 * @see Intelia#nanoInteliaSetup(Binding)
 */
public class Intelia implements Injector {
    private static final Logger logger = LoggerFactory.getLogger(Intelia.class);
    protected final Binding context;
    protected final Map<String, String> params;
    protected final Feather injector;

    /**
     * Initializes the Intelia Engine.
     *
     * @param context Bot Config Context.
     * @param additionalModules Additional modules to be included as dependency providers.
     * @param overrideModules modules which override default Intelia Module and additional modules.
     * @param injectContext Object in which all dependency are injected. Mostly is used for testing.
     */
    protected Intelia(Binding context, Map<String, String> params, Collection<Module> additionalModules, Collection<Module> overrideModules, Object injectContext) {
        this.context = context;
        this.params = params;
        List<Module> modules = new ArrayList<>();

        if (CollectionUtils.isEmpty(overrideModules)) {
            modules.add(new CoreModule(context, params, () -> {
                return Intelia.this;
            }));
            if (CollectionUtils.isNotEmpty(additionalModules)) {
                modules.addAll(additionalModules);
            }
        } else {
            modules.addAll(overrideModules);
        }

        injector = Feather.with(modules);
        if (injectContext != null) {
            injector.injectFields(injectContext);
        }
    }

    public static InteliaBuilder init(Binding binding) {
        return new InteliaBuilder(binding);
    }

    /**
     * Shortcut for {@code Intelia.init(binding).defaultSetup().get()}
     * 
     * @see InteliaBuilder#defaultSetup()
     */
    public static Intelia defaultInteliaSetup(Binding binding) {
        return init(binding).defaultSetup().get();
    }

    /**
     * Shortcut for
     * {@code Intelia.init(binding).defaultSetup().configFromDatastore(configDsName).get()}
     * 
     * @see InteliaBuilder#defaultSetup()
     * @see InteliaBuilder#configFromDatastore(String)
     */
    public static Intelia defaultInteliaSetup(Binding binding, String configDsName) {
        return init(binding).defaultSetup().configFromDatastore(configDsName).get();
    }

    /**
     * Shortcut for
     * {@code Intelia.init(binding).defaultSetup().doNotRethrowException(doNotRethrowException).get()}
     * 
     * @see InteliaBuilder#defaultSetup()
     */
    public static Intelia defaultInteliaSetup(Binding binding, boolean doNotRethrowException) {
        return init(binding).defaultSetup().doNotRethrowException(doNotRethrowException).get();
    }

    /**
     * Shortcut for {@code Intelia.init(binding).nanoSetup().get()}
     * 
     * @see InteliaBuilder#nanoSetup()
     */
    public static Intelia nanoInteliaSetup(Binding binding) {
        return init(binding).nanoSetup().get();
    }

    /**
     * Shortcut for {@code Intelia.init(binding).microSetup().get()}
     * 
     * @see InteliaBuilder#microSetup()
     */
    public static Intelia microInteliaSetup(Binding binding) {
        return init(binding).microSetup().get();
    }

    /**
     * Shortcut for {@code Intelia.init(binding).miniSetup().get()}
     * 
     * @see InteliaBuilder#miniSetup()
     */
    public static Intelia miniInteliaSetup(Binding binding) {
        return init(binding).miniSetup().get();
    }

    /**
     * Provides instance of requested class with all its dependencies.
     *
     * @param clazz object class to be provided
     * @param <T> type of requested object
     * @return instance with all dependencies
     */
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        T newInstance = null;
        // EXPERIMENTAL FEATURE
        // try create a no-args proxy first
        try {
            ChainMethodWrapper chainMethodWrapper = injector.instance(ChainMethodWrapper.class);
            if (chainMethodWrapper != null) {
                // proxy needed to wrap methods
                ProxyFactory factory = new ProxyFactory();
                factory.setSuperclass(clazz);
                factory.setFilter(m -> chainMethodWrapper.isHandledByChain(m));
                newInstance = (T) factory.create(new Class<?>[0], new Object[0], (self, thisMethod, proceed, args) -> {
                    try {
                        return chainMethodWrapper.verifyAndWrap(new Invocation(self, thisMethod, proceed, args));
                    } catch (Throwable cause) {
                        logger.error("Failed to invoke method " + thisMethod.getName() + "on" + self.getClass().getName(), cause);
                        throw cause;
                    }
                });
            }
        } catch (Throwable e) {
            logger.warn("Failed to proxy new instance of " + clazz.getName() + " - no method wrapping will be applied");
        }
        if (newInstance == null) {
            // If proxy failed (e.g. no wrappers chain or no-args constructor missing)
            // Feather will create instance and do constructor args injection
            newInstance = injector.instance(clazz);
        }
        // do fields injection as the last step
        injector.injectFields(newInstance);
        // EXPERIMENTAL - resolve @Wire against params, Binding and configuration
        ConfigurationManager cfg = null;
        try {
            cfg = injector.instance(ConfigurationManager.class);
        } catch (Exception e) {
            logger.warn("Can't resolve @Wire against ConfigurationManager for " + clazz.getName() + " - no ConfigurationManager found in dependency context");
        }
        final T finalNewInstance = newInstance;
        final ConfigurationManager finalCfg = cfg;
        FieldUtils.getFieldsListWithAnnotation(clazz, Wire.class).stream().forEach(field -> {
            Object fieldValue = getFieldValue(finalNewInstance, field);
            if (null == fieldValue) {
                // if not set by Feather -
                // trying to wire from params, Binding and configuration
                Wire wireanno = field.getAnnotation(Wire.class);
                String name = wireanno.name().trim().isEmpty() ? field.getName() : wireanno.name();
                Object value = null;
                if (MapUtils.isNotEmpty(params)) {
                    value = params.get(name);
                }
                if (isEmpty(value)) {
                    value = BindingUtils.getTypedPropertyValue(context, name);
                }
                if (isEmpty(value) && finalCfg != null) {
                    // try resolve from cfg
                    value = finalCfg.getConfigItem(name);
                }
                // if default value set - use it
                String defaultValue = wireanno.defaultValue();
                if (isEmpty(value) && !StringUtils.isBlank(defaultValue)) {
                    value = defaultValue;
                }
                if (!isEmpty(value)) {
                    wireField(field, name, finalNewInstance, value);
                    return;
                }
                if (wireanno.required()) {
                    // if no match found and required then throw unable to
                    // wire Exception
                    throw new IllegalArgumentException(
                            "Can' wire required [" + field.getName() + "], unable to find bean type [" + field.getType() + "] or item with name [" + wireanno.name() + "]");
                }
            }
        });


        return newInstance;
    }

    private boolean isEmpty(Object value) {
        if (null != value) {
            if (value instanceof String) {
                return StringUtils.isBlank((String) value);
            }
            return false;
        }
        return true;
    }

}
