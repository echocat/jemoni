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

package org.echocat.jemoni.carbon.spring;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;

import static org.springframework.util.StringUtils.hasText;

public class RegisterPropertyEditorsDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return CustomEditorConfigurer.class;
    }

    @Override
    protected void doParse(@Nonnull Element element, @Nonnull BeanDefinitionBuilder bean) {
        bean.addPropertyValue("propertyEditorRegistrars", new PropertyEditorRegistrar[] {
            new CarbonPropertyEditorRegistrar()
        });
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
        final String id = super.resolveId(element, definition, parserContext);
        return hasText(id) ? id : "org.echocat.jemoni.carbon.propertyEditors";
    }

}
