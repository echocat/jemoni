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
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.testing.environments.LogEnvironment;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.InetSocketAddress;

import static java.nio.charset.Charset.forName;
import static org.echocat.jemoni.carbon.CarbonWriter.DEFAULT_CHARSET;
import static org.echocat.jemoni.carbon.CarbonWriter.DEFAULT_MAX_BUFFER_LIFETIME;
import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.is;

public class WriterDefinitionParserUnitTest {

    @Rule
    public final LogEnvironment _logEnvironment = new LogEnvironment();

    @Test
    public void test() throws Exception {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("writerTestBeans.xml", WriterDefinitionParserUnitTest.class);
        try {
            final CarbonWriter defaultWriter = context.getBean(CarbonWriter.class.getName(), CarbonWriter.class);
            assertThat(defaultWriter.getAddress(), is(new InetSocketAddress("localhost", 667)));
            assertThat(defaultWriter.getMaxBufferLifetime(), is(DEFAULT_MAX_BUFFER_LIFETIME));
            assertThat(defaultWriter.getCharset(), is(DEFAULT_CHARSET));

            final CarbonWriter xxxWriter = context.getBean("xxx", CarbonWriter.class);
            assertThat(xxxWriter.getAddress(), is(new InetSocketAddress("localhost", 666)));
            assertThat(xxxWriter.getMaxBufferLifetime(), is(new Duration("666h")));
            assertThat(xxxWriter.getCharset(), is(forName("ISO-8859-15")));
        } finally {
            context.close();
        }
    }

}
