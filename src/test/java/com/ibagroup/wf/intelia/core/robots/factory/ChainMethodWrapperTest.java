package com.ibagroup.wf.intelia.core.robots.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChainMethodWrapperTest {

    public static class TestChainMethodWrapper extends ChainMethodWrapper {
        Predicate<Method> isTargetMethod;
        Function<Invocation, Object> wrapBody;

        public TestChainMethodWrapper(Predicate<Method> isTargetMethod, Function<Invocation, Object> wrapBody) {
            this.isTargetMethod = isTargetMethod;
            this.wrapBody = wrapBody;
        }

        @Override
        public boolean isHandled(Method m) {
            return isTargetMethod.test(m);
        }

        @Override
        Object wrap(Invocation invocation) throws Throwable {
            return wrapBody.apply(invocation);
        }

    }

    @Mock
    Object self;

    @Mock
    Predicate<Method> isTargetMethod;

    @Mock
    Function<Invocation, Object> wrapBody;

    @Mock
    Method method;

    @Mock
    Method proceed;

    @Mock
    ChainMethodWrapper outerMost;

    @Mock
    ChainMethodWrapper outer;

    @Mock
    ChainMethodWrapper innerMost;

    @Mock
    ChainMethodWrapper inner;

    Object[] args;
    Invocation invocation;
    TestChainMethodWrapper wrapper;

    @Before
    public void before() {
        args = new Object[] {};
        invocation = new Invocation(self, method, proceed, args);
        wrapper = new TestChainMethodWrapper(isTargetMethod, wrapBody);
    }

    @Test
    public void testSetInner(){
        ChainMethodWrapper result = wrapper.setInner(inner);
        verify(inner).setOuter(wrapper);
        assertThat(result).isEqualTo(inner);
    }

    @Test
    public void testSetOuter() {
        ChainMethodWrapper result = wrapper.setOuter(outer);
        verify(outer).setInner(wrapper);
        assertThat(result).isEqualTo(outer);
    }

    @Test
    public void testGetInnerMost_hasInner() {
        wrapper.setInner(inner);
        when(inner.getInnerMost()).thenReturn(innerMost);
        ChainMethodWrapper result = wrapper.getInnerMost();
        assertThat(result).isEqualTo(innerMost);
    }

    @Test
    public void testGetInnerMost_noInner() {
        ChainMethodWrapper result = wrapper.getInnerMost();
        assertThat(result).isEqualTo(wrapper);
    }

    @Test
    public void testGetOuterMost_hasOuter() {
        wrapper.setOuter(outer);
        when(outer.getOuterMost()).thenReturn(outerMost);
        ChainMethodWrapper result = wrapper.getOuterMost();
        assertThat(result).isEqualTo(outerMost);
    }

    @Test
    public void testGetOuterMost_noOuter() {
        ChainMethodWrapper result = wrapper.getOuterMost();
        assertThat(result).isEqualTo(wrapper);
    }

    @Test
    public void testIsHandledByChain_handled() throws Throwable {
        when(isTargetMethod.test(method)).thenReturn(true);
        boolean result = wrapper.isHandledByChain(method);
        verify(isTargetMethod).test(method);
        assertThat(result).isTrue();
    }

    @Test
    public void testIsHandledByChain_notHandled_noInner() throws Throwable {
        when(isTargetMethod.test(method)).thenReturn(false);
        boolean result = wrapper.isHandledByChain(method);
        verify(isTargetMethod).test(method);
        assertThat(result).isFalse();
    }

    @Test
    public void testIsHandledByChain_notHandled_hasInner() throws Throwable {
        wrapper.setInner(inner);
        when(isTargetMethod.test(method)).thenReturn(false);
        wrapper.isHandledByChain(method);
        verify(isTargetMethod).test(method);
        verify(inner).isHandledByChain(method);
    }


    @Test
    public void testVerifyAndWrap_handled() throws Throwable {
        when(isTargetMethod.test(method)).thenReturn(true);
        wrapper.verifyAndWrap(invocation);
        verify(isTargetMethod).test(method);
        verify(wrapBody).apply(invocation);
    }

    @Test
    public void testVerifyAndWrap_notHandled_noInner() throws Throwable {
        when(isTargetMethod.test(method)).thenReturn(false);
        wrapper.verifyAndWrap(invocation);
        verify(isTargetMethod).test(method);
        verify(wrapBody, never()).apply(invocation);
        verify(proceed).invoke(self, args);
    }

    @Test
    public void testVerifyAndWrap_notHandled_hasInner() throws Throwable {
        when(isTargetMethod.test(method)).thenReturn(false);
        wrapper.setInner(inner);
        wrapper.verifyAndWrap(invocation);
        verify(isTargetMethod).test(method);
        verify(inner).verifyAndWrap(invocation);
        verify(wrapBody, never()).apply(invocation);
        verify(proceed, never()).invoke(self, args);
    }

    @Test
    public void testVerifyAndWrap_notHandled_hasInner_innerThrowsThrowable() throws Throwable {
        when(isTargetMethod.test(method)).thenReturn(false);
        wrapper.setInner(inner);
        Throwable throwable = new Throwable("Inner throws Throwable");
        doThrow(throwable).when(inner).verifyAndWrap(invocation);
        assertThatThrownBy(() -> {
            wrapper.verifyAndWrap(invocation);
        }).isEqualTo(throwable);
    }

}
