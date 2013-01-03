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

package org.echocat.jemoni.carbon.jmx;

import org.echocat.jemoni.carbon.CarbonWriter;
import org.echocat.jemoni.carbon.MeasurePoint;
import org.echocat.jemoni.carbon.VirtualCarbonServerRule;
import org.echocat.jemoni.jmx.JmxRegistry;
import org.echocat.jemoni.jmx.Registration;
import org.echocat.jomon.testing.environments.LogEnvironment;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static java.util.regex.Pattern.quote;
import static org.echocat.jemoni.carbon.jmx.rules.AttributePatternRule.attribute;
import static org.echocat.jemoni.carbon.jmx.rules.ObjectPatternRule.object;
import static org.echocat.jemoni.carbon.jmx.rules.Rule.*;
import static org.echocat.jemoni.carbon.jmx.rules.Rules.rules;
import static org.echocat.jomon.runtime.util.Duration.sleep;
import static org.echocat.jomon.testing.BaseMatchers.hasItems;
import static org.junit.Assert.assertThat;

public class Jmx2CarbonBridgeIntegrationTest {

    @Rule
    public LogEnvironment _logEnvironment = new LogEnvironment();
    @Rule
    public VirtualCarbonServerRule _carbonServer = new VirtualCarbonServerRule();

    @Test
    public void test() throws Exception {
        final JmxRegistry registry = new JmxRegistry();
        final SampleBean sampleBean = new SampleBean();
        try (final Registration ignored = registry.register(sampleBean)) {
            try (final CarbonWriter carbonWriter = new CarbonWriter()) {
                carbonWriter.setAddress(_carbonServer.getAddress());
                carbonWriter.init();

                try (final Jmx2CarbonBridge bridge = new Jmx2CarbonBridge(carbonWriter)) {
                    bridge.setPathPrefix("foo.");
                    bridge.setRules(rules(rule("100ms",
                        includes(object(".*" + quote(sampleBean.getClass().getPackage().getName()) + ".*")),
                        excludes(object(attribute("excludedOne")))
                    )));
                    bridge.init();
                    sleep("500ms");
                }
            }
        }

        final List<MeasurePoint> measurePoints = _carbonServer.getLastRecordedMeasurePoints();
        assertThat(measurePoints, hasItems());
    }
}
