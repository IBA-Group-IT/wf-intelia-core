package com.ibagroup.wf.intelia.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Provider;
import org.apache.commons.collections.CollectionUtils;
import org.codejargon.feather.Feather;
import groovy.lang.Binding;

public class Intelia implements Injector {
    protected final Binding context;
    private final Feather injector;

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
        List<Module> modules = new ArrayList<>();

        if (CollectionUtils.isEmpty(overrideModules)) {
            modules.add(new CoreModule(context, params, new Provider<Injector>() {
                @Override
                public Injector get() {
                    return Intelia.this;
                };
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
     * Provides instance of requested class with all its dependencies.
     *
     * @param clazz object class to be provided
     * @param <T> type of requested object
     * @return instance with all dependencies
     */
    public <T> T getInstance(Class<T> clazz) {
        T newInstance = injector.instance(clazz);
        injector.injectFields(newInstance);
        return newInstance;
    }

}
