package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;
import java.util.Arrays;

public class Invocation {
    private final Object self;
    private final Method method;
    private final Object[] args;
    private final Method proceed;

    public Invocation(Object obj, Method method, Method proceed, Object[] args) {
        this.self = obj;
        this.method = method;
        this.args = Arrays.copyOf(args, args.length);
        this.proceed = proceed;
    }

    public Method getMethod() {
        return method;
    };

    public Object getSelf() {
        return self;
    };

    public Object[] getArgs() {
        return Arrays.copyOf(args, args.length);
    };

    public Method getProceed() {
        return proceed;
    }

    @Override
    public String toString() {
        return "Invocation of " + method.getName() + " on " + self.getClass().getName();
    }
}
