package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class PerformMethodWrapper extends MethodWrapper {

    private static final List<String> PERFORM = Arrays.asList("perform");

    public static boolean isHandled(Method m) {
        return PERFORM.stream().anyMatch(name -> m.getName().matches(name));
    }

    @Override
    void before(Method thisMethod) {}

    @Override
    void after(Method thisMethod) {}

    @Override
    void exceptionHandling(Throwable exception) {}

}
