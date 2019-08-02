package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;
import javassist.util.proxy.MethodFilter;

/**
 * MATRYOSHKA chain of method wrappers <br>
 * ORDER IS IMPORTANT !!! <br>
 * OUTERMOST starts first and ends last
 * 
 * @author dmitriev
 *
 */
public abstract class ChainMethodWrapper implements MethodFilter {

    private ChainMethodWrapper outer;
    private ChainMethodWrapper inner;

    public Object verifyAndWrap(Invocation invocation) throws Throwable {
        if (isHandled(invocation.getMethod())) {
            return wrap(invocation);
        }
        return invokeNext(invocation);
    }

    abstract Object wrap(Invocation invocation) throws Throwable;

    protected Object invokeNext(Invocation invocation) throws Throwable {
        if(getInner() == null){
            return invocation.getProceed().invoke(invocation.getSelf(), invocation.getArgs());
        }
        return getInner().verifyAndWrap(invocation);
    }

    public ChainMethodWrapper getInner() {
        return inner;
    }

    public ChainMethodWrapper getOuter() {
        return outer;
    }

    public ChainMethodWrapper getInnerMost() {
        if (getInner() == null) {
            return this;
        }
        return getInner().getInnerMost();
    }

    public ChainMethodWrapper getOuterMost() {
        if (getOuter() == null) {
            return this;
        }
        return getOuter().getOuterMost();
    }

    public ChainMethodWrapper setInner(ChainMethodWrapper next) {
        next.setOuter(this);
        this.inner = next;
        return next;
    }

    public ChainMethodWrapper setOuter(ChainMethodWrapper outer) {
        outer.setInner(this);
        this.outer = outer;
        return outer;
    }

    public boolean isHandledByChain(Method m) {
        if (isHandled(m)) {
            return true;
        }
        if (getInner() != null) {
            return inner.isHandledByChain(m);
        }
        return false;
    }


}

