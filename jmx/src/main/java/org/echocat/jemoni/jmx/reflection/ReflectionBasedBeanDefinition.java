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

import org.echocat.jemoni.jmx.AttributeDefinition;
import org.echocat.jemoni.jmx.BeanDefinition;
import org.echocat.jemoni.jmx.OperationDefinition;

import javax.annotation.Nonnull;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ReflectionBasedBeanDefinition<B> implements BeanDefinition<B> {

    private final Set<OperationDefinition<B>> _operationDefinitions;
    private final Set<AttributeDefinition<?, B>> _attributeDefinitions;

    private String _description;

    public ReflectionBasedBeanDefinition(@Nonnull Set<OperationDefinition<B>> operationDefinitions, @Nonnull Set<AttributeDefinition<?, B>> attributeDefinitions) {
        _operationDefinitions = operationDefinitions;
        _attributeDefinitions = attributeDefinitions;
    }

    @Override
    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = !isEmpty(description) ? description : null;
    }

    @Nonnull
    @Override
    public Set<OperationDefinition<B>> getOperationDefinitions() {
        return _operationDefinitions;
    }

    @Nonnull
    @Override
    public Set<AttributeDefinition<?, B>> getAttributeDefinitions() {
        return _attributeDefinitions;
    }

    @Override
    public String toString() {
        return "Bean{attributes=" + _attributeDefinitions + ", operations=" + _operationDefinitions + "}";
    }
}
