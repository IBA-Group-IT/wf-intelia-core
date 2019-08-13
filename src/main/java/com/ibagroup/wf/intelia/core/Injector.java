package com.ibagroup.wf.intelia.core;

import org.codejargon.feather.Feather;

public class Injector {

    private Feather injector;

    /**
     * Provides instance of requested class with all its dependencies.
     *
     * @param clazz object class to be provided
     * @param <T> type of requested object
     * @return instance with all dependencies
     */
    public final <T> T getInstance(Class<T> clazz) {
        T newInstance = injector.instance(clazz);
        injector.injectFields(newInstance);
        if (Injector.class.isInstance(newInstance)) {
            ((Injector) newInstance).setInjector(injector);
        }
        return newInstance;
    }

    public void setInjector(Feather injector) {
        this.injector = injector;
    }

    public Feather getInjector() {
        return injector;
    }

}
