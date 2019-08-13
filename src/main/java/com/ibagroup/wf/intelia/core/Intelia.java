package com.ibagroup.wf.intelia.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.codejargon.feather.Feather;
import groovy.lang.Binding;

public class Intelia {
    private final Feather injector;
    protected final Binding context;
    private Map<String, String> params;

    /**
     * Initializes the Intelia Engine.
     *
     * @param context Bot Config Context.
     * @param additionalModules Additional modules to be included as dependency providers.
     * @param overrideModules modules which override default Intelia Module and additional modules.
     * @param injectContext Object in which all dependency are injected. Mostly is used for testing.
     */
    protected Intelia(Binding context, Map<String, String> params, List<Module> additionalModules, List<Module> overrideModules, Object injectContext) {
        this.context = context;
        this.params = params;
        List<Module> modules = new ArrayList<>();

        if (CollectionUtils.isEmpty(overrideModules)) {
            modules.add(new CoreModule(context, params));
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
     * Provides instance of requested class with all its dependencies.
     *
     * @param clazz object class to be provided
     * @param <T> type of requested object
     * @return instance with all dependencies
     */
    public final <T> T getInstance(Class<T> clazz) {
        return injector.instance(clazz);
    }

    public Map<String, String> getParams() {
        return params;
    }

}
