/**
 * 
 */
package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import com.ibagroup.wf.intelia.core.FlowContext;
import com.ibagroup.wf.intelia.core.annotations.Retry;

/**
 * Method Wrapper to catch exception and retry same call again
 *
 */
public class RetryMethodWrapper extends ChainMethodWrapper {

    private FlowContext flowContext;

    public RetryMethodWrapper(FlowContext flowContext) {
        this.flowContext = flowContext;
    }

    @Override
    public boolean isHandled(Method m) {
        return m.isAnnotationPresent(Retry.class);
    }

    @Override
    public Object wrap(Invocation invocation) throws Throwable {
        Retry retry = invocation.getMethod().getAnnotation(Retry.class);
        int attempts = retry.attempts();
        int delay = retry.delay();
        Class<? extends Throwable>[] breakOn = retry.breakOn();
        int attempt = 0;

        Throwable lastThrowable = null;
        while (attempt < attempts) {
            try {
                return invokeInner(invocation);
            } catch (Throwable t) {
                flowContext.warn("Attempt {} failed with exception", attempt, t);
                lastThrowable = t;
                if (ArrayUtils.isNotEmpty(breakOn)) {
                    if (Arrays.stream(breakOn).anyMatch(bClass -> bClass.isAssignableFrom(t.getClass()))) {
                        throw new RuntimeException("Retry canceled on show-stopper throwable", lastThrowable);
                    }
                }
                attempt++;
                if (attempt >= attempts) {
                    break;
                }
            }
            if (delay > 0) {
                try {
                    // flowContext.debug("Retry sleep {} ms", delay);
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    flowContext.warn("Retry sleep interrupted", e);
                }
            }
            flowContext.warn("Retrying again");
        }
        throw new RuntimeException("Final Retry attempt reached - rethrowing", lastThrowable);
    }

}
