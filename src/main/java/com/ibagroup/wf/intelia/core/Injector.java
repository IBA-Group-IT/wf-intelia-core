package com.ibagroup.wf.intelia.core;

import org.codejargon.feather.Feather;

public class Injector {

    private Feather injector;

    public final <T extends Injector> T instance(Class<T> clazz) {
        T newInstance = injector.instance(clazz);
        injector.injectFields(newInstance);
        newInstance.setInjector(injector);
        return newInstance;
    }

    public void setInjector(Feather injector) {
        this.injector = injector;
    }

}
