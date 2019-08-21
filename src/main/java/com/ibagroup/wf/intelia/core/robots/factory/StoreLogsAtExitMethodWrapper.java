/**
 * 
 */
package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;
import com.ibagroup.wf.intelia.core.mis.IRobotLogger;

/**
 * Method Wrapper to finally store logs at the completion of robot entry method (perform)
 *
 */
public class StoreLogsAtExitMethodWrapper extends ChainMethodWrapper {

    private IRobotLogger robotLogger;

    public StoreLogsAtExitMethodWrapper(IRobotLogger robotLogger) {
        this.robotLogger = robotLogger;
    }

    @Override
    public boolean isHandled(Method m) {
        return PerformMethodWrapper.isPerformMethod.test(m);
    }

    @Override
    public Object wrap(Invocation invocation) throws Throwable {
        try {
            return invokeInner(invocation);
        } finally {
            robotLogger.storeLogs();
        }
    };


}
