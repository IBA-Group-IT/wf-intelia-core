package com.ibagroup.wf.intelia.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Named;
import org.codejargon.feather.Provides;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.ibagroup.wf.intelia.core.annotations.Wire;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.metadata.MetadataListManager;
import com.ibagroup.wf.intelia.core.metadata.MetadataManager;
import com.ibagroup.wf.intelia.core.robots.factory.ChainMethodWrapper;
import com.ibagroup.wf.intelia.core.robots.factory.Invocation;
import com.workfusion.intake.core.Module;
import groovy.lang.Binding;

@RunWith(MockitoJUnitRunner.class)
public class InteliaTest {

    public static class TestRobot_inject {

        MetadataManager metadataManager;

        @Inject
        @Named("testParamName")
        String testParam;

        @Inject
        public TestRobot_inject(MetadataManager metadataManager) {
            this.metadataManager = metadataManager;
        }
    }

    public static class TestRobot_wire {
        @Wire
        String testParam;
    }

    public static class TestRobot_perform {
        public Object perform() {
            return performBody.get();
        }
    }

    public static class TestModule_injectParam implements Module {
        @Provides
        @Named("testParamName")
        public String testParam() {
            return "testParamValue";
        }
    }

    public static class TestModule_injectAll extends TestModule_injectParam {
        @Provides
        public MetadataManager metadataManager(MetadataListManager metadataListManager) {
            return metadataListManager;
        }
    }

    public static class TestModule_wireFromBinding implements Module {
        @Provides
        public Binding binding() {
            return binding;
        }
    }


    public static class TestModule_wireFromCfg extends TestModule_wireFromBinding {
        @Provides
        public ConfigurationManager cfg() {
            return cfg;
        }
    }

    public class TestModule_proxy extends TestModule_injectAll {
        @Provides
        public ChainMethodWrapper chainMethodWrapper(TestChainMethodWrapper wrapper) {
            return wrapper;
        }
    }

    public static class TestChainMethodWrapper extends ChainMethodWrapper {

        @Override
        public boolean isHandled(Method m) {
            return isTargetMethod.test(m);
        }

        @Override
        public Object wrap(Invocation invocation) throws Throwable {
            return wrapBody.apply(invocation);
        }

    }


    @Mock
    static Binding binding;

    @Mock
    static ConfigurationManager cfg;

    @Mock
    static Predicate<Method> isTargetMethod;

    @Mock
    static Function<Invocation, Object> wrapBody;

    @Mock
    static Supplier<Object> performBody;

    @Test(expected = RuntimeException.class)
    public void testGetInstance_injectConstructorAndField_failed() throws Throwable {
        Intelia intelia = new Intelia(null, null, null, Collections.singleton(new TestModule_injectParam()), null);
        intelia.getInstance(TestRobot_inject.class);
    }

    @Test
    public void testGetInstance_injectConstructorAndField() throws Throwable {
        Intelia intelia = new Intelia(null, null, null, Collections.singleton(new TestModule_injectAll()), null);
        TestRobot_inject robot = intelia.getInstance(TestRobot_inject.class);
        assertThat(robot).isNotNull();
        assertThat(robot.metadataManager).isNotNull();
        assertThat(robot.metadataManager).isInstanceOf(MetadataListManager.class);
        assertThat(robot.testParam).isEqualTo("testParamValue");
    }


    @Test
    public void testGetInstance_wireFromBinding() throws Throwable {
        Intelia intelia = new Intelia(null, null, null, Collections.singleton(new TestModule_wireFromBinding()), null);
        when(binding.hasVariable("testParam")).thenReturn(true);
        when(binding.getVariable("testParam")).thenReturn("testParamValue");
        TestRobot_wire robot = intelia.getInstance(TestRobot_wire.class);
        assertThat(robot).isNotNull();
        assertThat(robot.testParam).isEqualTo("testParamValue");
        verify(binding).hasVariable("testParam");
        verify(binding).getVariable("testParam");
    }

    @Test
    public void testGetInstance_wireFromCfg() throws Throwable {
        Intelia intelia = new Intelia(null, null, null, Collections.singleton(new TestModule_wireFromCfg()), null);
        when(cfg.getConfigItem("testParam")).thenReturn("testParamValue");
        TestRobot_wire robot = intelia.getInstance(TestRobot_wire.class);
        assertThat(robot).isNotNull();
        assertThat(robot.testParam).isEqualTo("testParamValue");
        verify(cfg).getConfigItem("testParam");
    }

    @Test
    public void testGetInstance_proxy_failed() throws Throwable {
        Intelia intelia = new Intelia(null, null, null, Collections.singleton(new TestModule_proxy()), null);
        TestRobot_inject robot = intelia.getInstance(TestRobot_inject.class);
        assertThat(robot).isNotNull();
        assertThat(robot.metadataManager).isNotNull();
        assertThat(robot.metadataManager).isInstanceOf(MetadataListManager.class);
        assertThat(robot.testParam).isEqualTo("testParamValue");
    }

    @Test
    public void testGetInstance_proxy() throws Throwable {
        Intelia intelia = new Intelia(null, null, null, Collections.singleton(new TestModule_proxy()), null);
        when(isTargetMethod.test(argThat(m -> "perform".equals(m.getName())))).thenReturn(true);
        TestRobot_perform robot = intelia.getInstance(TestRobot_perform.class);
        assertThat(robot).isNotNull();
        robot.perform();
        verify(isTargetMethod, times(2)).test(argThat(m -> "perform".equals(m.getName())));
        verify(wrapBody).apply(any());
    }

    @Test
    public void testGetInstance_proxy_performFailed() throws Throwable {
        Intelia intelia = new Intelia(null, null, null, Collections.singleton(new TestModule_proxy()), null);
        RuntimeException runtime = new RuntimeException("Perform failed");
        doThrow(runtime).when(performBody).get();
        TestRobot_perform robot = intelia.getInstance(TestRobot_perform.class);
        assertThat(robot).isNotNull();
        // run perform
        assertThatThrownBy(() -> {
            robot.perform();
        }).isEqualTo(runtime);
    }
}
