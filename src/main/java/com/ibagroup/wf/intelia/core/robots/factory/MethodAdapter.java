package com.ibagroup.wf.intelia.core.robots.factory;

import static com.ibagroup.wf.intelia.core.CommonConstants.IS_HANDLED;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibagroup.wf.intelia.core.annotations.OnError;

import javassist.util.proxy.MethodHandler;

public abstract class MethodAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MethodAdapter.class);

    protected List<Class<? extends MethodWrapper>> methodWrappers = new ArrayList<>();
    private MethodWrapperBuilder methodWrapperBuilder;
    private MethodWrapper methodWrapper;

    static List<Class<?>> getIfs() {
        return null;
    }

    public MethodAdapter(MethodWrapperBuilder methodWrapperBuilder) {
        this.methodWrapperBuilder = methodWrapperBuilder;
    }

    /**
     *
     * @see MethodFilter#isHandled(Method)
     *
     * @param m - method to check from base robot
     * @return true if handled an no further process required
     */

    /**
     *
     * @see MethodHandler#invoke(Object, Method, Method, Object[])
     *
     * @param self - robot
     * @param thisMethod - overloaded method
     * @param proceed - robots method
     * @param args - arguments
     * @return Return result with true if no further process required
     * @throws Throwable - throws any exception that can occur
     */
    abstract ReturnResult invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable;

    public ReturnResult process(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        return this.invoke(self, thisMethod, proceed, args);
    }

    public ReturnResult exceptionHandling(Object self, Method thisMethod, Method proceed, Object[] args, Throwable throwable) throws Throwable {
        executeExceptionHandling(self, proceed, throwable);
        return handlerError(self, thisMethod, proceed, args, throwable);
    }

    public ReturnResult handlerError(Object self, Method thisMethod, Method proceed, Object[] args, Throwable throwable) {
        // invoking GenerateUuid method
        try {
            Optional<Method> any = MethodUtils.getMethodsListWithAnnotation(self.getClass(), OnError.class).stream().findAny();
            if (any.isPresent()) {
                Method method = any.get();

                Object invoke;
                if (method.getParameterCount() > 1) {
                    Object[] newArgs = ArrayUtils.add(new Object[] {self, thisMethod, proceed, throwable}, args);
                    invoke = method.invoke(self, newArgs);
                } else if (method.getParameterCount() == 1) {
                    invoke = method.invoke(self, throwable);
                } else {
                    invoke = method.invoke(self);
                }

                return new ReturnResult(true, invoke);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return new ReturnResult(false, null);
    }

    protected void executeBeforePhase(Method thisMethod) {
        for (Class<? extends MethodWrapper> wrapperClass : methodWrappers) {
            try {
                if (isMethodHandled(wrapperClass, thisMethod)) {
                    methodWrapper = methodWrapperBuilder.build(wrapperClass);
                    methodWrapper.before(thisMethod);
                    break;
                }
            } catch (Exception e) {
                logger.error("[MethodAdapter | executeBeforePhase] error occured ", e);
            }
        }
    }

    protected void executeExceptionHandling(Object self, Method thisMethod, Throwable throwable) {
        methodWrapper.exceptionHandling(throwable);
    }

    protected void executeAfterPhase(Method thisMethod) {
        for (Class<? extends MethodWrapper> wrapperClass : methodWrappers) {
            try {
                if (isMethodHandled(wrapperClass, thisMethod)) {
                    methodWrapper.after(thisMethod);
                    break;
                }
            } catch (Exception e) {
                logger.error("[MethodAdapter | executeAfterPhase] error occured ", e);
            }
        }
    }

    private boolean isMethodHandled(Class<? extends MethodWrapper> wrapperClass, Method thisMethod) throws Exception {
        return (boolean) wrapperClass.getMethod(IS_HANDLED, Method.class).invoke(null, thisMethod);
    }

    public void setMethodWrappers(List<Class<? extends MethodWrapper>> methodWrappers) {
        this.methodWrappers = methodWrappers;
    }

    static class ReturnResult {
        private final boolean handled;
        private final Object result;

        public ReturnResult(boolean handled, Object result) {
            super();
            this.handled = handled;
            this.result = result;
        }

        public boolean isHandled() {
            return handled;
        }

        public Object getResult() {
            return result;
        }

    }
}
