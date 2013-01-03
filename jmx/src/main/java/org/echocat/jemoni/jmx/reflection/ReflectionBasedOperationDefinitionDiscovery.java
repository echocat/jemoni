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

import org.echocat.jemoni.jmx.OperationDefinition;
import org.echocat.jemoni.jmx.OperationDefinitionDiscovery;
import org.echocat.jemoni.jmx.OperationInvoker;
import org.echocat.jemoni.jmx.annotations.Operation;
import org.echocat.jemoni.jmx.annotations.Operation.Null;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static java.util.Collections.unmodifiableSet;

public class ReflectionBasedOperationDefinitionDiscovery implements OperationDefinitionDiscovery {

    private static final ReflectionBasedOperationDefinitionDiscovery INSTANCE = new ReflectionBasedOperationDefinitionDiscovery();

    @Nonnull
    public static ReflectionBasedOperationDefinitionDiscovery reflectionBasedOperationDefinitionDiscovery() {
        return INSTANCE;
    }

    private final Map<Class<?>, Set<OperationDefinition<?>>> _typeToDefinitionsCache = new WeakHashMap<>();

    @Nonnull
    @Override
    public <B> Set<OperationDefinition<B>> discoverFor(@Nonnull Class<B> beanType) {
        Set<OperationDefinition<B>> result;
        synchronized (_typeToDefinitionsCache) {
            // noinspection unchecked,RedundantCast
            result = (Set<OperationDefinition<B>>) (Object) _typeToDefinitionsCache.get(beanType);
        }
        if (result == null) {
            // noinspection unchecked,RedundantCast
            result = (Set<OperationDefinition<B>>) (Object) generateDefinitionsFor(beanType);
            synchronized (_typeToDefinitionsCache) {
                 // noinspection unchecked,RedundantCast
                _typeToDefinitionsCache.put(beanType, (Set<OperationDefinition<?>>) (Object) result);
            }
        }
        return result;
    }

    @Nonnull
    protected Set<OperationDefinition<?>> generateDefinitionsFor(@Nonnull Class<?> beanType) {
        final Set<OperationDefinition<?>> result = new HashSet<>();

        for (Method method : beanType.getMethods()) {
            final Operation operation = method.getAnnotation(Operation.class);
            if (operation != null) {
                result.add(generateDefinitionFor(method, operation));
            }
        }

        return unmodifiableSet(result);
    }

    @Nonnull
    protected OperationDefinition<?> generateDefinitionFor(@Nonnull Method method, @Nonnull Operation operation) {
        final ReflectionBasedOperationDefinition<Object> definition = new ReflectionBasedOperationDefinition<>(method);
        definition.setName(operation.name());
        definition.setDescription(operation.description());
        final Class<? extends OperationInvoker<?>> invoker = operation.invoker();
        if (!Null.class.equals(invoker)) {
            definition.setInvoker(createInvoker(invoker));
        }
        return definition;
    }

    @Nonnull
    protected OperationInvoker<Object> createInvoker(@Nonnull Class<? extends OperationInvoker<?>> invoker) {
        try {
            // noinspection unchecked
            return (OperationInvoker<Object>) invoker.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create new instance of '" + invoker + "'.", e);
        }
    }
}
