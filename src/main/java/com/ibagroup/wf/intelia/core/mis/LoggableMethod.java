package com.ibagroup.wf.intelia.core.mis;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
public @interface LoggableMethod {
	String module();
	String operation();
	boolean transactional() default false;
	
}
