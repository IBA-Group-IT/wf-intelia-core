package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;
import java.util.List;

public class GenericMethodAdapter extends MethodAdapter {

//	private final LoggerMethodWrapper loggerMethodWrapper;
//	private final IRobotLogger robotLogger;

	public GenericMethodAdapter(MethodWrapperBuilder methodWrapperBuilder) {
		super(methodWrapperBuilder);
//		this.robotLogger = robotLogger;
//		this.methodWrappers = methodWrappers;
//		this.loggerMethodWrapper = new LoggerMethodWrapper(robotLogger);
	}

	public static List<Class<?>> getIfs() {
		return null;
	}

	@Override
	public ReturnResult invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
	    executeBeforePhase(thisMethod);
		Object result = proceed.invoke(self, args);
		executeAfterPhase(thisMethod);
		
		return new ReturnResult(true, result);
	}

	public ReturnResult handlerError(Object self, Method thisMethod, Method proceed, Object[] args,
			Throwable throwable) {
		throw new RuntimeException(throwable);
	}

}
