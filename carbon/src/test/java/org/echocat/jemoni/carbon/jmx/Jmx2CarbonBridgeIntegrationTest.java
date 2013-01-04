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
import org.echocat.jemoni.carbon.jmx.configuration.Configuration;
import org.echocat.jemoni.jmx.JmxRegistry;
import org.echocat.jemoni.jmx.Registration;
import org.echocat.jemoni.jmx.annotations.Attribute;
import org.echocat.jomon.runtime.StringUtils;
import org.echocat.jomon.testing.environments.LogEnvironment;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.beans.Introspector.getBeanInfo;
import static java.lang.Boolean.TRUE;
import static java.util.regex.Pattern.quote;
import static org.echocat.jemoni.carbon.jmx.configuration.AttributeRule.attribute;
import static org.echocat.jemoni.carbon.jmx.configuration.Configuration.configuration;
import static org.echocat.jemoni.carbon.jmx.configuration.Format.format;
import static org.echocat.jemoni.carbon.jmx.configuration.ObjectRule.object;
import static org.echocat.jemoni.carbon.jmx.configuration.Rule.rule;
import static org.echocat.jemoni.jmx.AttributeDefinition.AccessMode.undefined;
import static org.echocat.jomon.runtime.util.Duration.sleep;
import static org.echocat.jomon.testing.BaseMatchers.is;
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

        //noinspection UnusedDeclaration
        try (final Registration registration = registry.register(sampleBean)) {
            try (final CarbonWriter carbonWriter = writer()) {
                //noinspection UnusedDeclaration
                try (final Jmx2CarbonBridge bridge = bridgeFor(carbonWriter, sampleBean)) {
                    sleep("500ms");
                    checkMeasurePoints(sampleBean, _carbonServer.getLastRecordedMeasurePoints());
                }
            }
        }
    }

    @Nonnull
    protected CarbonWriter writer() throws Exception {
        final CarbonWriter writer = new CarbonWriter();
        writer.setAddress(_carbonServer.getAddress());
        writer.init();
        return writer;
    }

    @Nonnull
    protected Jmx2CarbonBridge bridgeFor(@Nonnull CarbonWriter writer, @Nonnull SampleBean sampleBean) throws Exception {
        final Jmx2CarbonBridge bridge = new Jmx2CarbonBridge(writer);
        bridge.setPathPrefix("foo.");
        bridge.setConfiguration(rulesFor(sampleBean));
        bridge.init();
        return bridge;
    }

    @Nonnull
    protected Configuration rulesFor(@Nonnull SampleBean sampleBean) {
        final String originalPackage = sampleBean.getClass().getPackage().getName().replace('.', '_');
        return configuration().rules(
            rule()
                .updateEvery("100ms")
                .includes(object().pattern(".*" + quote(sampleBean.getClass().getPackage().getName()) + ".*"))
                .excludes(object().attributes(attribute("excludedOne")))
        ).formats(
            format(quote(originalPackage), "bar")
        );
    }

    protected void checkMeasurePoints(@Nonnull SampleBean sampleBean, @Nonnull List<MeasurePoint> measurePoints) throws Exception {
        final Map<String, Number> properties = getPropertiesOf(sampleBean);
        for (Entry<String, Number> attributeToValue : properties.entrySet()) {
            final String attribute = attributeToValue.getKey();
            final String expectedPath = "foo.bar." + sampleBean.getClass().getSimpleName() + "." + attribute;
            final Number value = attributeToValue.getValue();
            boolean found = false;
            for (MeasurePoint measurePoint : measurePoints) {
                final String path = measurePoint.getPath();
                if (path.equals(expectedPath)) {
                    if (measurePoint.getValue().equals(value.doubleValue())) {
                        found = true;
                        break;
                    }
                }
            }
            assertThat("Expected measurePoint " + expectedPath + " with value " + value, found, is(true));
        }
    }

    @Nonnull
    protected Map<String, Number> getPropertiesOf(@Nonnull Object bean) throws Exception {
        final BeanInfo info = getBeanInfo(bean.getClass());
        final Map<String, Number> result = new HashMap<>();
        for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
            if (isAcceptableDescriptor(descriptor)) {
                final String name = getNameFor(descriptor);
                final Object value  = descriptor.getReadMethod().invoke(bean);
                result.put(name, eval(value));
            }
        }
        return result;
    }

    @Nullable
    private Number eval(@Nullable Object value) {
        final Number result;
        if (value == null) {
            result = null;
        } else if (value instanceof Number) {
            result = (Number) value;
        } else if (value instanceof Character) {
            result = (int) ((Character) value);
        } else if (value instanceof Boolean) {
            result = TRUE.equals(value) ? 1 : 0;
        } else {
            throw new IllegalArgumentException("Could not handle: " + value);
        }
        return result;
    }

    protected String getNameFor(@Nonnull PropertyDescriptor descriptor) {
        final Method method = descriptor.getReadMethod();
        if (method == null) {
            throw new IllegalArgumentException("Could not access read method of " + descriptor);
        }
        final Attribute annotation = method.getAnnotation(Attribute.class);
        return annotation != null && !StringUtils.isEmpty(annotation.name()) ? annotation.name() : descriptor.getName();
    }

    protected boolean isAcceptableDescriptor(@Nonnull PropertyDescriptor descriptor) {
        final Method method = descriptor.getReadMethod();
        return method != null && isAcceptableAnnotationAt(method) && isAcceptableValueType(descriptor.getPropertyType());
    }

    protected boolean isAcceptableAnnotationAt(@Nonnull Method method) {
        final Attribute annotation = method.getAnnotation(Attribute.class);
        return annotation != null && (annotation.accessMode() == undefined || annotation.accessMode().isReadingPossible());
    }

    protected boolean isAcceptableValueType(@Nonnull Class<?> type) {
        return Number.class.isAssignableFrom(type) || Boolean.class.equals(type) || Character.class.equals(type);
    }
}
