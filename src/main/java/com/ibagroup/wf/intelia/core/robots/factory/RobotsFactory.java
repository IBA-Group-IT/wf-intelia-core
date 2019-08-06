package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibagroup.wf.intelia.core.annotations.AfterInit;
import com.ibagroup.wf.intelia.core.robots.RobotProtocol;
import com.ibagroup.wf.intelia.core.utils.MethodUtils;
import groovy.lang.Binding;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

/**
 * Possible ways of creating a robot with the factory:
 * 
 * <pre>
 *  #1 Robot robot = new RobotsFactoryBuilder(binding).defaultSetup().build().newRobotInstance(Robot.class);
 *  #2 Robot robot = <RobotsFactory.>defaultFactorySetup(binding).newRobotInstance(Robot.class);
 * </pre>
 * 
 */
public class RobotsFactory {
    private static final Logger logger = LoggerFactory.getLogger(RobotsFactory.class);

    private final ChainMethodWrapper chainMethodWrapper;

    private final Map<Class<?>, Object> wiringObjects;

    public RobotsFactory(Map<Class<?>, Object> objMap, ChainMethodWrapper chainMethodWrapper) {
        this.chainMethodWrapper = chainMethodWrapper;
        this.wiringObjects = objMap;
    }

    /**
     * Shortcut for {@code new RobotsFactoryBuilder(binding).defaultSetup().build()}
     */
    public static RobotsFactory defaultFactorySetup(Binding binding) {
        return new RobotsFactoryBuilder(binding).defaultSetup().build();
    }

    /**
     * Shortcut for {@code new RobotsFactoryBuilder(binding, throwException).defaultSetup().build()}
     */
    public static RobotsFactory defaultFactorySetup(Binding binding, boolean throwException) {
        return new RobotsFactoryBuilder(binding).defaultSetup(throwException).build();
    }

    /**
     * @deprecated use {@link #newRobotInstance(Class) instead}
     */
    @Deprecated
    public <T extends RobotProtocol> T newInstance(Class<T> clazz) throws Throwable {
        return newRobotInstance(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T extends RobotProtocol> T newRobotInstance(Class<T> clazz) throws Throwable {

        try {
            T robot = null;
            if (chainMethodWrapper != null) {
                // proxy needed to wrap methods
                ProxyFactory factory = new ProxyFactory();
                factory.setSuperclass(clazz);
                factory.setFilter(m -> chainMethodWrapper.isHandledByChain(m));
                robot = (T) factory.create(new Class<?>[0], new Object[0], new MethodHandler() {
                    @Override
                    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                        try {
                            return chainMethodWrapper.verifyAndWrap(new Invocation(self, thisMethod, proceed, args));
                        } catch (Throwable cause) {
                            logger.error("Failed to invoke method " + thisMethod.getName() + "on" + self.getClass().getName(), cause);
                            throw cause;
                        }
                    }
                });
            } else {
                // no-args constructor robot instantiation
                robot = clazz.getConstructor(new Class<?>[0]).newInstance();
            }
            if (MapUtils.isNotEmpty(wiringObjects)) {
                Binding binding = (Binding) wiringObjects.get(Binding.class);
                // post wire all robot fields
                RobotsFactoryHelper.wireObject(robot, wiringObjects, binding);
            }
            // invoke all @AfterInit methods
            MethodUtils.findAndInvokeAllMethodsWithAnnotation(robot, AfterInit.class);

            return robot;
        } catch (Throwable cause) {
            logger.error("Failed to crate new robot instance " + clazz.getName(), cause);
            throw cause;
        }
    }


}
