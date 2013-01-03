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

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Handles schema <code>https://jemoni.echocat.org/schemas/carbon4spring.xsd</code>
 */
public class CarbonNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("carbonWriter", new CarbonWriterDefinitionParser());
        registerBeanDefinitionParser("jmx2carbonBridge", new Jmx2CarbonBridgeDefinitionParser());
    }

}
