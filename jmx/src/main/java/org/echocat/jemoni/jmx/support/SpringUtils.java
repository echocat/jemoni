/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat JeMoni, Copyright (c) 2012-2013 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jemoni.jmx.support;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.Thread.currentThread;
import static java.lang.reflect.Modifier.isStatic;
import static org.apache.commons.lang3.StringUtils.join;

public class SpringUtils {

    private static final Class<?> WEB_APPLICATION_CONTEXT_UTILS = findClass("org.springframework.web.context.support.WebApplicationContextUtils");
    private static final Class<?> WEB_APPLICATION_CONTEXT = findClass("org.springframework.web.context.WebApplicationContext");

    private static final Method GET_WEB_APPLICATION_CONTEXT = getMethod(WEB_APPLICATION_CONTEXT_UTILS, true, WEB_APPLICATION_CONTEXT, "getWebApplicationContext", ServletContext.class);
    private static final Method GET_BEAN = getMethod(WEB_APPLICATION_CONTEXT, false, Object.class, "getBean", String.class);

    @Nonnull
    public static <T> T getBeanFor(@Nonnull ServletContext servletContext, @Nonnull String beanName, @Nonnull Class<T> beanType) throws ServletException {
        final Object applicationContext = getApplicationContext(servletContext);
        final Object plainBean;
        try {
            plainBean = beanType.cast(GET_BEAN.invoke(applicationContext, beanName));
        } catch (Exception e) {
            final Throwable target = e instanceof InvocationTargetException ? ((InvocationTargetException)e).getTargetException() : null;
            if (target != null && target.getClass().getName().endsWith("NoSuchBeanDefinitionException")) {
                throw new ServletException("Could not find bean '" + beanName + "' at " + applicationContext + ".", target);
            } else {
                throw new ServletException("Could not retrieve bean '" + beanName + "' from " + applicationContext + ".", target != null ? target : e);
            }
        }
        if (!beanType.isInstance(plainBean)) {
            throw new ServletException("Could bean '" + beanName + "' is of type " + plainBean.getClass().getName() + " not of expected " + beanType.getName() + ".");
        }
        return beanType.cast(plainBean);
    }

    public static boolean isSpringAvailable() {
        return GET_WEB_APPLICATION_CONTEXT != null && GET_BEAN != null;
    }

    public static void assertSpringAvailable() throws ServletException {
        if (!isSpringAvailable()) {
            throw new ServletException("There is no spring available. Could not find a valid implementation in classpath.");
        }
    }

    @Nonnull
    public static Object getApplicationContext(@Nonnull ServletContext servletContext) throws ServletException {
        assertSpringAvailable();
        final Object applicationContext;
        try {
            applicationContext = GET_WEB_APPLICATION_CONTEXT.invoke(null, servletContext);
        } catch (Exception e) {
            final Throwable target = e instanceof InvocationTargetException ? ((InvocationTargetException)e).getTargetException() : null;
            throw new ServletException("Could not retrieve spring context from " + servletContext + ".", target != null ? target : e);
        }
        if (applicationContext == null) {
            throw new ServletException("Could not find a spring context.");
        }
        return applicationContext;
    }

    @Nullable
    private static Class<?> findClass(@Nonnull String name) {
        Class<?> result;
        try {
            result = currentThread().getContextClassLoader().loadClass(name);
        } catch (ClassNotFoundException ignored) {
            result = null;
        }
        return result;
    }

    @Nullable
    private static Method getMethod(@Nullable Class<?> fromType, boolean expectedStatic, Class<?> returnType, @Nonnull String name, @Nullable Class<?>... parameterTypes) {
        final Method method;
        if (fromType != null) {
            try {
                method = fromType.getMethod(name, parameterTypes);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(buildMessageFor(fromType, expectedStatic, returnType, name, parameterTypes), e);
            }
            final int modifiers = method.getModifiers();
            if ((expectedStatic && !isStatic(modifiers)) || (!expectedStatic && isStatic(modifiers)) || !returnType.isAssignableFrom(method.getReturnType())) {
                throw new RuntimeException(buildMessageFor(fromType, expectedStatic, returnType, name, parameterTypes));
            }
        } else {
            method = null;
        }
        return method;
    }

    @Nonnull
    private static String buildMessageFor(@Nonnull Class<?> fromType, boolean expectedStatic, @Nonnull Class<?> returnType, @Nonnull String name, @Nullable Class<?>[] parameterTypes) {
        return "Could not find method " + (expectedStatic ? "static " : " ") + returnType.getName() + " public "  + fromType.getName() + "." + name + "(" + join(parameterTypes) + ").";
    }

    private SpringUtils() {}
}
