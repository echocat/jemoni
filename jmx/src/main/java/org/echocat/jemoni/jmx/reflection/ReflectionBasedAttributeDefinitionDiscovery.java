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

import org.echocat.jemoni.jmx.AttributeAccessor;
import org.echocat.jemoni.jmx.AttributeDefinition;
import org.echocat.jemoni.jmx.AttributeDefinitionDiscovery;
import org.echocat.jemoni.jmx.annotations.Attribute;
import org.echocat.jemoni.jmx.annotations.Attribute.Null;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static java.beans.Introspector.getBeanInfo;
import static java.util.Collections.unmodifiableSet;

public class ReflectionBasedAttributeDefinitionDiscovery implements AttributeDefinitionDiscovery {

    private static final ReflectionBasedAttributeDefinitionDiscovery INSTANCE = new ReflectionBasedAttributeDefinitionDiscovery();

    @Nonnull
    public static ReflectionBasedAttributeDefinitionDiscovery reflectionBasedAttributeDefinitionDiscovery() {
        return INSTANCE;
    }

    private final Map<Class<?>, Set<AttributeDefinition<?, ?>>> _typeToDefinitionsCache = new WeakHashMap<>();

    @Nonnull
    @Override
    public <B> Set<AttributeDefinition<?, B>> discoverFor(@Nonnull Class<B> beanType) {
        Set<AttributeDefinition<?, B>> result;
        synchronized (_typeToDefinitionsCache) {
            // noinspection unchecked,RedundantCast
            result = (Set<AttributeDefinition<?, B>>) (Object) _typeToDefinitionsCache.get(beanType);
        }
        if (result == null) {
            // noinspection unchecked,RedundantCast
            result = (Set<AttributeDefinition<?, B>>) (Object) generateDefinitionsFor(beanType);
            synchronized (_typeToDefinitionsCache) {
                // noinspection unchecked,RedundantCast
                _typeToDefinitionsCache.put(beanType, (Set<AttributeDefinition<?, ?>>) (Object) result);
            }
        }
        return result;
    }

    @Nonnull
    protected Set<AttributeDefinition<?, ?>> generateDefinitionsFor(@Nonnull Class<?> beanType) {
        final Set<AttributeDefinition<?, ?>> result = new HashSet<>();

        final BeanInfo beanInfo = getBeanInfoFor(beanType);

        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            final Attribute attribute = findAnnotationFor(descriptor);
            if (attribute != null) {
                result.add(generateDefinitionFor(descriptor, attribute));
            }
        }

        return unmodifiableSet(result);
    }

    @Nonnull
    protected BeanInfo getBeanInfoFor(@Nonnull Class<?> beanType) {
        try {
            return beanType.isInterface() ? getBeanInfo(beanType) : getBeanInfo(beanType, Object.class);
        } catch (IntrospectionException e) {
            throw new RuntimeException("Could not retrieve bean info for " + beanType.getName() + ".", e);
        }
    }

    @Nullable
    protected Attribute findAnnotationFor(@Nonnull PropertyDescriptor descriptor) {
        final Method readMethod = descriptor.getReadMethod();
        final Method writeMethod = descriptor.getWriteMethod();
        Attribute attribute = readMethod != null ? readMethod.getAnnotation(Attribute.class) : null;
        if (attribute == null) {
            attribute = writeMethod != null ? writeMethod.getAnnotation(Attribute.class) : null;
        }
        return attribute;
    }

    @Nonnull
    protected AttributeDefinition<?, ?> generateDefinitionFor(@Nonnull PropertyDescriptor descriptor, @Nonnull Attribute attribute) {
        final ReflectionBasedAttributeDefinition<Object, Object> definition = new ReflectionBasedAttributeDefinition<>(descriptor);
        definition.setName(attribute.name());
        definition.setDescription(attribute.description());
        definition.setAccessMode(attribute.accessMode());
        final Class<? extends AttributeAccessor<?, ?>> accessorType = attribute.accessor();
        if (!Null.class.equals(accessorType)) {
            definition.setAccessor(createInvoker(accessorType));
        }
        return definition;
    }

    @Nonnull
    protected AttributeAccessor<Object, Object> createInvoker(@Nonnull Class<? extends AttributeAccessor<?, ?>> accessorType) {
        try {
            // noinspection unchecked
            return (AttributeAccessor<Object, Object>) accessorType.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create new instance of '" + accessorType + "'.", e);
        }
    }
}
