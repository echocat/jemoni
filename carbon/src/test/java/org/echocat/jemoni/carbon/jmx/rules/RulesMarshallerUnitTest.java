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

package org.echocat.jemoni.carbon.jmx.rules;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import static org.echocat.jemoni.carbon.jmx.rules.AttributePatternRule.attribute;
import static org.echocat.jemoni.carbon.jmx.rules.ObjectPatternRule.object;
import static org.echocat.jemoni.carbon.jmx.rules.Rule.*;
import static org.echocat.jemoni.carbon.jmx.rules.Rules.rules;
import static org.echocat.jemoni.carbon.jmx.rules.RulesMarshaller.marshall;
import static org.echocat.jemoni.carbon.jmx.rules.RulesMarshaller.unmarshall;
import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.junit.Assert.assertThat;

public class RulesMarshallerUnitTest {

    @Test
    public void testUnmarshall() throws Exception {
        try (final StringReader reader = new StringReader(getReference())) {
            final Rules rules = unmarshall(reader);
            final Rules referenceRules = createReferenceRules();
            assertThat(rules, is(referenceRules));
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
        try (final InputStream is = RulesMarshallerUnitTest.class.getResourceAsStream("example1.xml")) {
            return IOUtils.toString(is, "UTF-8");
        }
    }


    @Nonnull
    public static Rules createReferenceRules() {
        return rules(
            rule("rule1", "1s", includes(
                object("objecti11.*",
                    attribute("attributei111.*"),
                    attribute("attributei112.*")
                ),
                object("object12.*",
                    attribute("attributei121.*"),
                    attribute("attributei122.*")
                )
            ), excludes(
                object("objecte11.*",
                    attribute("attributee111.*"),
                    attribute("attributee112.*")
                ),
                object("object12.*",
                    attribute("attributee121.*"),
                    attribute("attributee122.*")
                )
            )),
            rule("rule2", "2s", includes(
                object("objecti21.*",
                    attribute("attributei211.*"),
                    attribute("attributei212.*")
                ),
                object("object22.*",
                    attribute("attributei221.*"),
                    attribute("attributei222.*")
                )
            ), excludes(
                object("objecte21.*",
                    attribute("attributee211.*"),
                    attribute("attributee212.*")
                ),
                object("object22.*",
                    attribute("attributee221.*"),
                    attribute("attributee222.*")
                )
            ))
        );
    }

}
