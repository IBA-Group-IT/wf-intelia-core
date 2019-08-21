package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.InvocationTargetException;
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

    /**
     * Entry method - checks if wrapper applies for the invocation method and wraps if yes
     * 
     * @param invocation
     * @return
     * @throws Throwable
     */
    public Object verifyAndWrap(Invocation invocation) throws Throwable {
        if (isHandled(invocation.getMethod())) {
            return wrap(invocation);
        }
        return invokeInner(invocation);
    }

    /**
     * Will be executed if wrapper applies for the invocation
     * 
     * @param invocation
     * @return
     * @throws Throwable
     */
    public abstract Object wrap(Invocation invocation) throws Throwable;

    /**
     * Passes control to inner wrapper or finally to the proxied object
     * 
     * @param invocation
     * @return
     * @throws Throwable
     */
    protected Object invokeInner(Invocation invocation) throws Throwable {
        // for call stack optimization
        // find the inner wrapper which handles invocation and skip which dont
        ChainMethodWrapper inner = getInner();
        while (inner != null && !inner.isHandled(invocation.getMethod())) {
            inner = inner.getInner();
        }
        if (inner == null) {
            try {
                return invocation.getProceed().invoke(invocation.getSelf(), invocation.getArgs());
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
        return inner.wrap(invocation);
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
        this.inner = next;
        if (next.getOuter() != this) {
            next.setOuter(this);
        }
        return next;
    }

    public ChainMethodWrapper setOuter(ChainMethodWrapper outer) {
        this.outer = outer;
        if (outer.getInner() != this) {
            outer.setInner(this);
        }
        return outer;
    }

    /**
     * returns true if any wrapper in the chain can handle the method
     * 
     * @param m
     * @return
     */
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

