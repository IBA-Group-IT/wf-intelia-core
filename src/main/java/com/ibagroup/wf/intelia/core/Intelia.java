package com.ibagroup.wf.intelia.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.codejargon.feather.Feather;
import groovy.lang.Binding;

public class Intelia extends Injector {
    protected final Binding context;

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
            modules.add(new CoreModule(context, params));
            if (CollectionUtils.isNotEmpty(additionalModules)) {
                modules.addAll(additionalModules);
            }
        } else {
            modules.addAll(overrideModules);
        }

        setInjector(Feather.with(modules));
        if (injectContext != null) {
            getInjector().injectFields(injectContext);
        }
    }

    public static InteliaBuilder init(Binding binding) {
        return new InteliaBuilder(binding);
    }

}
