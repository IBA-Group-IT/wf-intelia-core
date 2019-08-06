package com.ibagroup.wf.intelia.core.robots.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.ibagroup.wf.intelia.core.annotations.AfterInit;
import com.ibagroup.wf.intelia.core.annotations.Wire;
import com.ibagroup.wf.intelia.core.mis.TaskAction.Result;
import com.ibagroup.wf.intelia.core.robots.RobotProtocol;
import groovy.lang.Binding;

@RunWith(MockitoJUnitRunner.class)
public class RobotsFactoryTest {

    public static class TestChainMethodWrapper extends ChainMethodWrapper {
        Predicate<Method> shouldWrapMehod;
        Function<Invocation, Object> wrapBody;

        @Override
        public boolean isHandled(Method m) {
            return shouldWrapMehod.test(m);
        }

        @Override
        Object wrap(Invocation invocation) throws Throwable {
            return wrapBody.apply(invocation);
        }
    }

    public static class TestRobot implements RobotProtocol {
        @Wire
        private Binding binding;

        public Object perform() {
            return RobotsFactoryTest.performBody.get();
        }

        @AfterInit
        public void afterInit() {
            RobotsFactoryTest.afterInitBody.get();
        }

        @Override
        public boolean storeCurrentMetadata() {
            return false;
        }

        @Override
        public void storeCurrentActionResult(Result result, String... description) {
        }
    }

    @Mock
    Predicate<Method> shouldWrapMethod;

    @Mock
    Function<Invocation, Object> wrapBody;

    @InjectMocks
    TestChainMethodWrapper wrapper;

    @Mock
    static Supplier<Object> performBody;

    @Mock
    static Supplier<Void> afterInitBody;

    @Mock
    Binding binding;

    @Test
    public void testNewRobotInstance_wiring() throws Throwable {
        Map<Class<?>, Object> wiringObjects = new HashMap<>();
        wiringObjects.put(Binding.class, binding);
        TestRobot robotInstance = new RobotsFactory(wiringObjects, null).newRobotInstance(TestRobot.class);
        assertThat(robotInstance).isNotNull();
        assertThat(robotInstance.binding).isEqualTo(binding);
    }

    @Test
    public void testNewRobotInstance_afterInit() throws Throwable {
        TestRobot robotInstance = new RobotsFactory(null, null).newRobotInstance(TestRobot.class);
        assertThat(robotInstance).isNotNull();
        verify(afterInitBody).get();
    }

    @Test
    public void testNewRobotInstance_afterInitFailed() throws Throwable {
        RuntimeException runtime = new RuntimeException("AfterInit failed");
        doThrow(runtime).when(afterInitBody).get();
        assertThatThrownBy(() -> {
            new RobotsFactory(null, null).newRobotInstance(TestRobot.class);
        }).isEqualTo(runtime);
    }

    @Test
    public void testPerform_noWrappers() throws Throwable {
        Object theResult = new Object();
        when(performBody.get()).thenReturn(theResult);
        TestRobot robotInstance = new RobotsFactory(null, null).newRobotInstance(TestRobot.class);
        assertThat(robotInstance).isNotNull();
        Object result = robotInstance.perform();
        verify(performBody).get();
        assertThat(result).isEqualTo(theResult);
    }

    @Test
    public void testPerform_noWrappers_performFailed() throws Throwable {
        RuntimeException runtime = new RuntimeException("Perform failed");
        doThrow(runtime).when(performBody).get();
        TestRobot robotInstance = new RobotsFactory(null, null).newRobotInstance(TestRobot.class);
        assertThat(robotInstance).isNotNull();
        assertThatThrownBy(() -> {
            robotInstance.perform();
        }).isEqualTo(runtime);
    }

    @Test
    public void testPerform_withWrapper_wrapperNotApplied() throws Throwable {
        Object theResult = new Object();
        when(performBody.get()).thenReturn(theResult);
        TestRobot robotInstance = new RobotsFactory(null, wrapper).newRobotInstance(TestRobot.class);
        assertThat(robotInstance).isNotNull();
        // wrapper's isHandled called for each robot method (9 times actually)
        // but for the test none applies
        verify(shouldWrapMethod, atLeastOnce()).test(any());
        reset(shouldWrapMethod);

        // run perform
        Object result = robotInstance.perform();
        // since wrapper does not apply - wrapper should not be called
        verify(shouldWrapMethod, never()).test(any());
        verify(wrapBody, never()).apply(any());
        verify(performBody).get();
        assertThat(result).isEqualTo(theResult);
    }

    @Test
    public void testPerform_withWrapper_wrapperNotApplied_performFailed() throws Throwable {
        RuntimeException runtime = new RuntimeException("Perform failed");
        doThrow(runtime).when(performBody).get();
        TestRobot robotInstance = new RobotsFactory(null, wrapper).newRobotInstance(TestRobot.class);
        assertThat(robotInstance).isNotNull();
        assertThatThrownBy(() -> {
            robotInstance.perform();
        }).isEqualTo(runtime);
    }

    @Test
    public void testPerform_withWrapper_wrapperApplied() throws Throwable {
        Object theResult = new Object();
        when(performBody.get()).thenReturn(theResult);
        // for the test wrapper applies to all robot methods
        when(shouldWrapMethod.test(any())).thenReturn(true);
        TestRobot robotInstance = new RobotsFactory(null, wrapper).newRobotInstance(TestRobot.class);
        assertThat(robotInstance).isNotNull();
        // wrapper's isHandled called for each robot method (9 times actually)
        verify(shouldWrapMethod, atLeastOnce()).test(any());

        // mock wrapper wrap method to call perform inside
        when(wrapBody.apply(any())).thenAnswer(invocation -> {
            return wrapper.invokeInner((Invocation) invocation.getArguments()[0]);
        });
        // run perform
        Object result = robotInstance.perform();
        // since wrapper does not apply - wrapper should not be called
        verify(wrapBody).apply(any());
        verify(performBody).get();
        assertThat(result).isEqualTo(theResult);
    }

    @Test
    public void testPerform_withWrapper_wrapperApplied_performFailed() throws Throwable {
        RuntimeException runtime = new RuntimeException("Perform failed");
        doThrow(runtime).when(performBody).get();
        // for the test wrapper applies to all robot methods
        when(shouldWrapMethod.test(any())).thenReturn(true);
        TestRobot robotInstance = new RobotsFactory(null, wrapper).newRobotInstance(TestRobot.class);
        assertThat(robotInstance).isNotNull();

        // mock wrapper wrap method to call perform inside
        when(wrapBody.apply(any())).thenAnswer(invocation -> {
            return wrapper.invokeInner((Invocation) invocation.getArguments()[0]);
        });
        // run perform
        assertThatThrownBy(() -> {
            robotInstance.perform();
        }).isEqualTo(runtime);
    }


}
