package com.ibagroup.wf.intelia.core.config;

import com.ibagroup.wf.intelia.core.BindingUtils;

import groovy.lang.Binding;

public class RuntimeConfiguration implements ConfigurationManager {

    private Binding binding;

    public RuntimeConfiguration(Binding binding) {
        this.binding = binding;
    }

    @Override
    public String getConfigItem(String keyParameter) {
        return BindingUtils.getPropertyValue(binding, keyParameter);
    }

    @Override
    public boolean isLocal() {
        return false;
    }
}
