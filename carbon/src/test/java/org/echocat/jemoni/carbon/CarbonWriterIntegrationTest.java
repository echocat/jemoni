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

package org.echocat.jemoni.carbon;

import org.echocat.jomon.runtime.concurrent.RetryForSpecifiedTimeStrategy;
import org.echocat.jomon.runtime.concurrent.RetryingStrategy;
import org.echocat.jomon.testing.environments.LogEnvironment;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.echocat.jomon.runtime.concurrent.Retryer.executeWithRetry;
import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.CollectionMatchers.containsAllItemsOf;
import static org.echocat.jomon.testing.CollectionMatchers.hasSameSizeAs;

public class CarbonWriterIntegrationTest {

    public static final RetryingStrategy<Void> STRATEGY = RetryForSpecifiedTimeStrategy.<Void>retryForSpecifiedTimeOf("1m").withWaitBetweenEachTry("1ms", "10ms").withExceptionsThatForceRetry(AssertionError.class).asUnmodifiable();

    @Rule
    public LogEnvironment _logEnvironment = new LogEnvironment();
    @Rule
    public VirtualCarbonServerRule _carbonServer = new VirtualCarbonServerRule();

    private static final Random RANDOM = new Random();

    @Test
    public void testWrite() throws Exception {
        final CarbonWriter carbonWriter = new CarbonWriter();
        carbonWriter.setAddress(_carbonServer.getAddress());
        carbonWriter.init();
        final List<MeasurePoint> sendMeasurePoints = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            sendMeasurePoints.add(carbonWriter.write("motortalk_test.foo.bar.a", (double) RANDOM.nextInt(1000)));
            sendMeasurePoints.add(carbonWriter.write("motortalk_test.foo.bar.b", (double) RANDOM.nextInt(1000)));
            sendMeasurePoints.add(carbonWriter.write("motortalk_test.foo.bar.c", (double) RANDOM.nextInt(1000)));
        }
        executeWithRetry(new Runnable() { @Override public void run() {
            try {
                final List<MeasurePoint> measurePoints = _carbonServer.getLastRecordedMeasurePoints();
                assertThat(measurePoints, hasSameSizeAs(sendMeasurePoints));
                assertThat(measurePoints, containsAllItemsOf(sendMeasurePoints));
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }}, STRATEGY);
    }

}
