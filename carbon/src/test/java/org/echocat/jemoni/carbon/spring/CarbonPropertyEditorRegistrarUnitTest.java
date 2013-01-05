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
import org.echocat.jomon.testing.environments.LogEnvironment;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.echocat.jemoni.carbon.jmx.configuration.RulesMarshallerUnitTest.createReferenceRules;
import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.is;

public class CarbonPropertyEditorRegistrarUnitTest {

    @Rule
    public final LogEnvironment _logEnvironment = new LogEnvironment();

    @Test
    public void test() throws Exception {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("registerPropertyEditorsTestBeans.xml", CarbonPropertyEditorRegistrarUnitTest.class);
        try {
            final Jmx2CarbonBridge bridge = context.getBean("bridge", Jmx2CarbonBridge.class);
            assertThat(bridge.getConfiguration(), is(createReferenceRules()));
        } finally {
            context.close();
        }
    }

}
