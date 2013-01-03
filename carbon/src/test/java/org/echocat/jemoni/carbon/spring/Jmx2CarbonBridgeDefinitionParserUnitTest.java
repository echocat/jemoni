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

import org.echocat.jemoni.carbon.CarbonWriter;
import org.echocat.jemoni.carbon.jmx.Jmx2CarbonBridge;
import org.echocat.jomon.testing.environments.LogEnvironment;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static java.lang.Thread.currentThread;
import static org.echocat.jemoni.carbon.jmx.rules.RulesMarshallerUnitTest.createReferenceRules;
import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.echocat.jomon.testing.BaseMatchers.isSameAs;

public class Jmx2CarbonBridgeDefinitionParserUnitTest {

    @Rule
    public final LogEnvironment _logEnvironment = new LogEnvironment();

    @Test
    public void test() throws Exception {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("jmx2carbonBridgeTestBeans.xml", CarbonWriterDefinitionParserUnitTest.class);
        try {
            final CarbonWriter carbonWriter = context.getBean("carbonWriter", CarbonWriter.class);
            final ClassLoader classLoader = context.getBean("classLoader", ClassLoader.class);

            final Jmx2CarbonBridge bridge1 = context.getBean("bridge1", Jmx2CarbonBridge.class);
            assertThat(bridge1.getCarbonWriter(), isSameAs(carbonWriter));
            assertThat(bridge1.getClassLoader(), isSameAs(classLoader));
            assertThat(bridge1.getPathPrefix(), is("foo."));
            assertThat(bridge1.getRules(), is(createReferenceRules()));

            final Jmx2CarbonBridge bridge2 = context.getBean(Jmx2CarbonBridge.class.getName(), Jmx2CarbonBridge.class);
            assertThat(bridge2.getCarbonWriter(), isSameAs(carbonWriter));
            assertThat(bridge2.getClassLoader(), isSameAs(currentThread().getContextClassLoader()));
            assertThat(bridge2.getRules(), is(null));
        } finally {
            context.close();
        }
    }

}
