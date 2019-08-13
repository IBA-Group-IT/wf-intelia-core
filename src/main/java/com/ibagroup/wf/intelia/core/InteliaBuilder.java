package com.ibagroup.wf.intelia.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import groovy.lang.Binding;

public class InteliaBuilder {
    private Binding context;
    private Map<String, String> params = new HashMap<>();
    private List<Module> overrideModules;
    private List<Module> additionalModules;
    private Object injectContext;

    public InteliaBuilder(Binding context) {
        this.context = context;
    }

    public InteliaBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public InteliaBuilder additional(Module... modules) {
        additionalModules = Arrays.asList(modules);
        return this;
    }

    public InteliaBuilder override(Module... modules) {
        overrideModules = Arrays.asList(modules);
        return this;
    }

    public InteliaBuilder injectFields(Object context) {
        this.injectContext = context;
        return this;
    }

    public Intelia get() {
        return new Intelia(context, params, additionalModules, overrideModules, injectContext);
    }
}
