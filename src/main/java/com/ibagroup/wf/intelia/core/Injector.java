package com.ibagroup.wf.intelia.core;

public interface Injector {
    public <T> T getInstance(Class<T> clazz);
}
