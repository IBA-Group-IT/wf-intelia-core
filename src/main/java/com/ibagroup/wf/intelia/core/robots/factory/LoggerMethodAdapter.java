package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;
import java.util.List;

import com.ibagroup.wf.intelia.core.mis.IRobotLogger;

public class LoggerMethodAdapter /*extends MethodAdapter*/ {

    /*private final LoggerMethodWrapper loggerMethodWrapper;

	public LoggerMethodAdapter(IRobotLogger robotLogger, MethodWrapperBuilder methodWrapperBuilder) {
		super(methodWrapperBuilder);
		this.loggerMethodWrapper = new LoggerMethodWrapper(robotLogger);
	}

	public static List<Class<?>> getIfs() {
		return null;
	}

	@Override
	public ReturnResult invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
		loggerMethodWrapper.before(thisMethod);
		Object result = proceed.invoke(self, args);
		loggerMethodWrapper.after(thisMethod);
		return new ReturnResult(true, result);
	}

	public ReturnResult handlerError(Object self, Method thisMethod, Method proceed, Object[] args,
			Throwable throwable) {
		loggerMethodWrapper.exceptionHandling(throwable);
		throw new RuntimeException(throwable);

	}*/

}
