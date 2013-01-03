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

import org.echocat.jemoni.carbon.jmx.Jmx2CarbonBridge;
import org.echocat.jemoni.carbon.jmx.rules.Rules;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;

import static org.echocat.jemoni.carbon.jmx.rules.RulesConstants.SCHEMA_NAMESPACE;
import static org.echocat.jemoni.carbon.jmx.rules.RulesMarshaller.unmarshall;
import static org.springframework.util.StringUtils.hasText;

public class Jmx2CarbonBridgeDefinitionParser extends AbstractSingleBeanDefinitionParser {

    public static final String JMX_REGISTRY_REF_ATTRIBUTE = "jmxRegistry-ref";
    public static final String MBEAN_SERVER_REF_ATTRIBUTE = "mBeanServer-ref";
    public static final String WRITER_REF_ATTRIBUTE = "writer-ref";
    public static final String CLASS_LOADER_REF_ATTRIBUTE = "classLoader-ref";
    public static final String PATH_PREFIX_ATTRIBUTE = "pathPrefix";

    @Override
    protected Class<?> getBeanClass(Element element) {
        return Jmx2CarbonBridge.class;
    }

    @Override
    protected void doParse(@Nonnull Element element, @Nonnull BeanDefinitionBuilder bean) {
        final String jmxRegistryRef = element.getAttribute(JMX_REGISTRY_REF_ATTRIBUTE);
        final String mBeanServerRef = element.getAttribute(MBEAN_SERVER_REF_ATTRIBUTE);
        if (hasText(jmxRegistryRef)) {
            if (hasText(mBeanServerRef)) {
                throw new IllegalArgumentException("The " + JMX_REGISTRY_REF_ATTRIBUTE + " and " + MBEAN_SERVER_REF_ATTRIBUTE + " attributes could not be used at the same time.");
            }
            bean.addConstructorArgReference(jmxRegistryRef);
        } else if (hasText(mBeanServerRef)) {
            bean.addConstructorArgReference(mBeanServerRef);
        }
        bean.addConstructorArgReference(element.getAttribute(WRITER_REF_ATTRIBUTE));

        final String classLoaderRef = element.getAttribute(CLASS_LOADER_REF_ATTRIBUTE);
        if (hasText(classLoaderRef)) {
            bean.addPropertyReference("classLoader", classLoaderRef);
        }

        final String pathPrefix = element.getAttribute(PATH_PREFIX_ATTRIBUTE);
        if (hasText(pathPrefix)) {
            bean.addPropertyValue("pathPrefix", pathPrefix);
        }

        Rules rules = null;
        final NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if ("rules".equals(child.getLocalName()) && SCHEMA_NAMESPACE.equals(child.getNamespaceURI())) {
                rules = unmarshall(child);
            }
        }
        bean.addPropertyValue("rules", rules);
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
        final String id = super.resolveId(element, definition, parserContext);
        return hasText(id) ? id : getBeanClass(element).getName();
    }

}
