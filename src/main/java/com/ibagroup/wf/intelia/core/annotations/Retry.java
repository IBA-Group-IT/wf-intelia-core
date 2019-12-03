package com.ibagroup.wf.intelia.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.ibagroup.wf.intelia.core.robots.factory.RetryMethodWrapper;

/**
 * Runtime method annotation to help internally handle retries of a failing method
 * 
 * @see RetryMethodWrapper
 * @author dmitriev
 *
 */
@Target({java.lang.annotation.ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {
    int attempts() default 3;

    int delay() default 1000;

    String breakOn() default "";
}
