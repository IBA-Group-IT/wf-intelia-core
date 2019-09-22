package com.ibagroup.wf.intelia.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ java.lang.annotation.ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Wire {
	boolean required() default true;
	String name() default "";
    String defaultValue() default "";
}
