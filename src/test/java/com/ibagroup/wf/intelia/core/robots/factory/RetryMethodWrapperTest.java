package com.ibagroup.wf.intelia.core.robots.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.ibagroup.wf.intelia.core.FlowContext;
import com.ibagroup.wf.intelia.core.annotations.Retry;

@RunWith(MockitoJUnitRunner.class)
public class RetryMethodWrapperTest {
    public final static int ATTEMPTS = 4;

    public static class TestException extends RuntimeException {
    }

    public static class SubTestException extends TestException {
    }

    public static class TestClass {

        @Retry(attempts = ATTEMPTS, delay = 100, breakOn = {TestException.class})
        public Object retry() {
            return retryBody.get();
        }

        public void dontRetry() {}
    }

    @Mock
    static Supplier<Object> retryBody;

    @Mock
    FlowContext flowContext;

    TestClass testClass;
    Method retry;
    Object[] args = new Object[] {};

    @InjectMocks
    RetryMethodWrapper retryMethodWrapper;

    @Before
    public void before() throws NoSuchMethodException, SecurityException {
        testClass = new TestClass();
        retry = TestClass.class.getMethod("retry");
    }

    @Test
    public void testIsHandled() throws Throwable {
        assertThat(retryMethodWrapper.isHandled(retry)).isTrue();
        assertThat(retryMethodWrapper.isHandled(TestClass.class.getMethod("dontRetry"))).isFalse();
    }

    @Test
    public void testWrap_noException() throws Throwable {
        Object result = new Object();
        when(retryBody.get()).thenReturn(result);
        assertThat(retryMethodWrapper.wrap(new Invocation(testClass, retry, retry, args))).isEqualTo(result);
        verify(retryBody, times(1)).get();
    }

    @Test
    public void testWrap_someException() throws Throwable {
        RuntimeException someException = new RuntimeException();
        doThrow(someException).when(retryBody).get();
        assertThatThrownBy(() -> {
            retryMethodWrapper.wrap(new Invocation(testClass, retry, retry, args));
        }).isEqualTo(someException);
        verify(retryBody, times(ATTEMPTS)).get();
    }

    @Test
    public void testWrap_breakException() throws Throwable {
        RuntimeException breakException = new TestException();
        doThrow(breakException).when(retryBody).get();
        assertThatThrownBy(() -> {
            retryMethodWrapper.wrap(new Invocation(testClass, retry, retry, args));
        }).isEqualTo(breakException);
        verify(retryBody, times(1)).get();
    }

    @Test
    public void testWrap_breakSubException() throws Throwable {
        RuntimeException breakException = new SubTestException();
        doThrow(breakException).when(retryBody).get();
        assertThatThrownBy(() -> {
            retryMethodWrapper.wrap(new Invocation(testClass, retry, retry, args));
        }).isEqualTo(breakException);
        verify(retryBody, times(1)).get();
    }
}
