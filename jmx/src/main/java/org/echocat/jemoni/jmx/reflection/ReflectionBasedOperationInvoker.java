/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat JeMoni, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jemoni.jmx.reflection;

import org.echocat.jemoni.jmx.ArgumentDefinition;
import org.echocat.jemoni.jmx.OperationInvoker;
import org.echocat.jemoni.jmx.annotations.Argument;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.echocat.jemoni.jmx.annotations.Argument.Null;
import static org.echocat.jemoni.jmx.reflection.ReflectionBasedUtils.correctTypeIfNeeded;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ReflectionBasedOperationInvoker<B> implements OperationInvoker<B> {

    private final Method _method;
    private final ArgumentDefinition[] _argumentDefinitions;

    public ReflectionBasedOperationInvoker(@Nonnull Method method) {
        _method = method;
        _argumentDefinitions = getArgumentDefinitionsFor(method);
    }

    @Nonnull
    protected ArgumentDefinition[] getArgumentDefinitionsFor(@Nonnull Method method) {
        final Class<?>[] types = method.getParameterTypes();
        final Annotation[][] annotations = method.getParameterAnnotations();
        final ArgumentDefinition[] result = new ArgumentDefinition[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = getArgumentDefinitionFor(types[i], annotations[i], i);
        }
        return result;
    }

    @Nonnull
    @Override
    public Class<?> getReturnType() {
        return correctTypeIfNeeded(_method.getReturnType());
    }

    @Nonnull
    @Override
    public ArgumentDefinition[] getArgumentDefinitions() {
        return _argumentDefinitions;
    }

    @Override
    public Object invoke(@Nonnull B bean, @Nonnull Object[] arguments) throws Exception {
        return _method.invoke(bean, arguments);
    }

    @Nonnull
    protected ArgumentDefinition getArgumentDefinitionFor(@Nonnull Class<?> type, @Nonnull Annotation[] annotation, int index) {
        final Argument argument = findArgumentAnnotationIn(annotation);
        final String nameFromArgument = argument != null ? argument.name() : null;
        final String descriptionFromArgument = argument != null ? argument.description() : null;
        final Class<?> typeFromArgument = argument != null ? argument.type() : Null.class;

        return new ArgumentDefinitionImpl(
            Null.class.equals(typeFromArgument) ? type : typeFromArgument,
            isEmpty(nameFromArgument) ? "argument" + index : nameFromArgument,
            isEmpty(descriptionFromArgument) ? null : descriptionFromArgument
        );
    }

    @Nullable
    protected Argument findArgumentAnnotationIn(@Nonnull Annotation[] annotations) {
        Argument result = null;
        for (Annotation annotation : annotations) {
            if (Argument.class.equals(annotation.annotationType())) {
                result = (Argument) annotation;
                break;
            }
        }
        return result;
    }

    protected static class ArgumentDefinitionImpl implements ArgumentDefinition {

        private final Class<?> _type;
        private final String _name;
        private final String _description;

        public ArgumentDefinitionImpl(@Nonnull Class<?> type, @Nonnull String name, @Nullable String description) {
            _type = type;
            _name = name;
            _description = description;
        }

        @Nonnull
        @Override
        public Class<?> getType() {
            return correctTypeIfNeeded(_type);
        }

        @Nonnull
        @Override
        public String getName() {
            return _name;
        }

        @Override
        @Nullable
        public String getDescription() {
            return _description;
        }

    }

}
