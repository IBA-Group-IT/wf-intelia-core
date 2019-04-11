package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;
import java.util.List;

public abstract class MethodWrapper implements Wrapper {
	
	static boolean isHandled(Method m) {
		return false;
	}
	
	abstract void before(Method thisMethod);
	
	abstract void after(Method thisMethod);
	
	abstract void exceptionHandling(Throwable exception);
	
	public static List<Class<?>> getIfs() {
		return null;
	}

}
