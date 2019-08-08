package com.ibagroup.wf.intelia.core.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodUtils {
    private static final Logger logger = LoggerFactory.getLogger(MethodUtils.class);

    public static List<Method> findAllMehodsHavingAnnotation(Class examineClass, Class annotationClass) {
        // TODO extract to MachineAdaptations
        return org.apache.commons.lang3.reflect.MethodUtils.getMethodsListWithAnnotation(examineClass, annotationClass);
    }

    public static Optional<Method> findAnyMehodHavingAnnotation(Class examineClass, Class annotationClass) {
        return findAllMehodsHavingAnnotation(examineClass, annotationClass).stream().findAny();
    }

    public static void findAndInvokeAllMethodsWithAnnotation(final Object targetObj, Class annotationClass) throws Throwable {

        List<Method> annotatedMethods = findAllMehodsHavingAnnotation(targetObj.getClass(), annotationClass);
        for (Method method : annotatedMethods) {
            try {
                method.invoke(targetObj);
            } catch (Throwable e) {
                if (e instanceof InvocationTargetException) {
                    e = e.getCause();
                }
                logger.error("@" + annotationClass.getSimpleName() + " Method " + method.getName() + " failed", e);
                throw e;
            }
        }
    }
}
