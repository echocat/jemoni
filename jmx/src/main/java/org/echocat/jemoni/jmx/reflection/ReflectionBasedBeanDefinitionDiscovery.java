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

import org.echocat.jemoni.jmx.*;
import org.echocat.jemoni.jmx.annotations.Bean;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static org.echocat.jemoni.jmx.reflection.ReflectionBasedAttributeDefinitionDiscovery.reflectionBasedAttributeDefinitionDiscovery;
import static org.echocat.jemoni.jmx.reflection.ReflectionBasedOperationDefinitionDiscovery.reflectionBasedOperationDefinitionDiscovery;

public class ReflectionBasedBeanDefinitionDiscovery implements BeanDefinitionDiscovery {

    private static final ReflectionBasedBeanDefinitionDiscovery INSTANCE = new ReflectionBasedBeanDefinitionDiscovery();
    
    public static ReflectionBasedBeanDefinitionDiscovery reflectionBasedBeanDefinitionDiscovery() {
        return INSTANCE;
    }
    
    private OperationDefinitionDiscovery _operationDefinitionDiscovery = reflectionBasedOperationDefinitionDiscovery();
    private AttributeDefinitionDiscovery _attributeDefinitionDiscovery = reflectionBasedAttributeDefinitionDiscovery();

    private final Map<Class<?>, BeanDefinition<?>> _typeToDefinitionsCache = new WeakHashMap<>();
    
    @Nonnull
    public OperationDefinitionDiscovery getOperationDefinitionDiscovery() {
        return _operationDefinitionDiscovery;
    }

    public void setOperationDefinitionDiscovery(@Nonnull OperationDefinitionDiscovery operationDefinitionDiscovery) {
        checkForModificationsOnDefaultInstance();
        _operationDefinitionDiscovery = operationDefinitionDiscovery;
    }

    @Nonnull
    public AttributeDefinitionDiscovery getAttributeDefinitionDiscovery() {
        return _attributeDefinitionDiscovery;
    }
    
    public void setAttributeDefinitionDiscovery(@Nonnull AttributeDefinitionDiscovery attributeDefinitionDiscovery) {
        checkForModificationsOnDefaultInstance();
        _attributeDefinitionDiscovery = attributeDefinitionDiscovery;
    }

    @Nonnull
    @Override
    public <B> BeanDefinition<B> discoverFor(@Nonnull Class<B> beanType) {
        BeanDefinition<B> result;
        synchronized (_typeToDefinitionsCache) {
            // noinspection unchecked
            result = (BeanDefinition<B>) _typeToDefinitionsCache.get(beanType);
        }
        if (result == null) {
            result = generateDefinitionsFor(beanType);
            synchronized (_typeToDefinitionsCache) {
                _typeToDefinitionsCache.put(beanType, result);
            }
        }
        return result;
    }

    @Nonnull
    protected <B> BeanDefinition<B> generateDefinitionsFor(@Nonnull Class<B> beanType) {
        final Set<OperationDefinition<B>> operations = _operationDefinitionDiscovery.discoverFor(beanType);
        final Set<AttributeDefinition<?, B>> attributes = _attributeDefinitionDiscovery.discoverFor(beanType);
        final ReflectionBasedBeanDefinition<B> result = new ReflectionBasedBeanDefinition<>(operations, attributes);

        final Bean bean = beanType.getAnnotation(Bean.class);
        if (bean != null) {
            result.setDescription(bean.description());
        }

        return result;
    }

    private void checkForModificationsOnDefaultInstance() {
        // noinspection ObjectEquality
        if (this == INSTANCE) {
            throw new UnsupportedOperationException("Modifying of default instance of " + getClass().getName() + " is not supported.");
        }
    }
}
