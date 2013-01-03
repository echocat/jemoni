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

package org.echocat.jemoni.jmx;

import javax.annotation.Nonnull;

import static org.echocat.jemoni.jmx.reflection.ReflectionBasedBeanDefinitionDiscovery.reflectionBasedBeanDefinitionDiscovery;

public class BeanFacadeFactory {

    private BeanDefinitionDiscovery _beanDefinitionDiscovery = reflectionBasedBeanDefinitionDiscovery();

    @Nonnull
    public BeanDefinitionDiscovery getBeanDefinitionDiscovery() {
        return _beanDefinitionDiscovery;
    }

    public void setBeanDefinitionDiscovery(@Nonnull BeanDefinitionDiscovery beanDefinitionDiscovery) {
        _beanDefinitionDiscovery = beanDefinitionDiscovery;
    }

    @Nonnull
    public <B> BeanFacade<B> createFor(@Nonnull B bean) {
        final Class<?> beanType = bean.getClass();
        // noinspection unchecked
        final BeanDefinition<B> beanDefinition = (BeanDefinition<B>) _beanDefinitionDiscovery.discoverFor(beanType);
        return new BeanFacade<>(bean, beanDefinition);
    }

}
