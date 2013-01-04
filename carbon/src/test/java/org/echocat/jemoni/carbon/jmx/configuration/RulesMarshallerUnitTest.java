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

package org.echocat.jemoni.carbon.jmx.configuration;

import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.*;

import static org.echocat.jemoni.carbon.jmx.configuration.AttributeRule.attribute;
import static org.echocat.jemoni.carbon.jmx.configuration.Configuration.configuration;
import static org.echocat.jemoni.carbon.jmx.configuration.Format.format;
import static org.echocat.jemoni.carbon.jmx.configuration.ObjectRule.object;
import static org.echocat.jemoni.carbon.jmx.configuration.Rule.rule;
import static org.echocat.jemoni.carbon.jmx.configuration.RulesMarshaller.marshall;
import static org.echocat.jemoni.carbon.jmx.configuration.RulesMarshaller.unmarshall;
import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.junit.Assert.assertThat;

public class RulesMarshallerUnitTest {

    @Test
    public void testUnmarshall() throws Exception {
        try (final StringReader reader = new StringReader(getReference())) {
            final Configuration configuration = unmarshall(reader);
            final Configuration referenceConfiguration = createReferenceRules();
            assertThat(configuration, is(referenceConfiguration));
        }
    }

    @Test
    public void testMarshall() throws Exception {
        try (final StringWriter writer = new StringWriter()) {
            marshall(createReferenceRules(), writer);
            assertThat(writer.toString(), is(getReference()));
        }
    }

    @Nonnull
    protected static String getReference() throws IOException {
        final StringBuilder sb = new StringBuilder();
        try (final InputStream is = RulesMarshallerUnitTest.class.getResourceAsStream("example1.xml")) {
            try (final Reader reader = new InputStreamReader(is, "UTF-8")) {
                int c = reader.read();
                while (c >= 0) {
                    if (c != '\r') {
                        sb.append((char) c);
                    }
                    c = reader.read();
                }
            }
        }
        return sb.toString();
    }


    @Nonnull
    public static Configuration createReferenceRules() {
        return configuration()
            .formats(format("foo", "bar").includes(
                object().pattern("objecti31.*").attributes(
                    attribute("attributei311.*"),
                    attribute("attributei312.*")
                ),
                object().pattern("object32.*").attributes(
                    attribute("attributei321.*"),
                    attribute("attributei322.*")
                )
            ).excludes(
                object().pattern("objecte31.*").attributes(
                    attribute("attributee311.*"),
                    attribute("attributee312.*")
                ),
                object().pattern("object32.*").attributes(
                    attribute("attributee321.*"),
                    attribute("attributee322.*")
                )
            )).rules(
                rule().name("rule1").updateEvery("1s").includes(
                    object().pattern("objecti11.*").attributes(
                        attribute("attributei111.*"),
                        attribute("attributei112.*")
                    ),
                    object().pattern("object12.*").attributes(
                        attribute("attributei121.*"),
                        attribute("attributei122.*")
                    )
                ).excludes(
                    object().pattern("objecte11.*").attributes(
                        attribute("attributee111.*"),
                        attribute("attributee112.*")
                    ),
                    object().pattern("object12.*").attributes(
                        attribute("attributee121.*"),
                        attribute("attributee122.*")
                    )
                ),
                rule().name("rule2").updateEvery("2s").includes(
                    object().pattern("objecti21.*").attributes(
                        attribute("attributei211.*"),
                        attribute("attributei212.*")
                    ),
                    object().pattern("object22.*").attributes(
                        attribute("attributei221.*"),
                        attribute("attributei222.*")
                    )
                ).excludes(
                    object().pattern("objecte21.*").attributes(
                        attribute("attributee211.*"),
                        attribute("attributee212.*")
                    ),
                    object().pattern("object22.*").attributes(
                        attribute("attributee221.*"),
                        attribute("attributee222.*")
                    )
                )
            );
    }

}
