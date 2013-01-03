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

package org.echocat.jemoni.jmx.support;

import org.echocat.jemoni.jmx.JmxRegistry;
import org.echocat.jemoni.jmx.Registration;
import org.echocat.jomon.cache.CacheListener;
import org.echocat.jomon.cache.LocalTrackingEnabledCacheListener;
import org.echocat.jomon.cache.LocalTrackingEnabledCacheListener.Report;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static javax.management.MBeanOperationInfo.ACTION;

public class CacheListenerMBean implements DynamicMBean, AutoCloseable {

    private static final DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS = DecimalFormatSymbols.getInstance(Locale.US);

    private final CacheListener _listener;
    private final Registration _registration;

    public CacheListenerMBean(@Nonnull CacheListener listener, @Nonnull JmxRegistry registry) {
        _listener = listener;
        _registration = registry.register(this, _listener.getClass());
    }

    @Override
    public void close() throws Exception {
        _registration.close();
    }

    @Override
    public Object getAttribute(String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException {
        final Object result;
        if ("numberOfLocalTrackedEvents".equals(attributeName)) {
            result = castForAttribute(LocalTrackingEnabledCacheListener.class).getNumberOfLocalTrackedEvents();
        } else {
            throw new AttributeNotFoundException();
        }
        return result;
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        final String attributeName = attribute.getName();
        final Object value = attribute.getValue();
        if ("numberOfLocalTrackedEvents".equals(attributeName)) {
            castForAttribute(LocalTrackingEnabledCacheListener.class).setNumberOfLocalTrackedEvents(cast(Number.class, value).intValue());
        } else {
            throw new AttributeNotFoundException();
        }
    }

    private <T> T cast(@Nonnull Class<T> type, @Nullable Object value) throws InvalidAttributeValueException {
        if (!type.isInstance(value)) {
            throw new InvalidAttributeValueException();
        }
        return type.cast(value);
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        final Object result;
        if ("getReport".equals(actionName)) {
            final NumberFormat full = new DecimalFormat("#,##0", DECIMAL_FORMAT_SYMBOLS);
            final NumberFormat part = new DecimalFormat("#,##0.00", DECIMAL_FORMAT_SYMBOLS);
            final Collection<? extends Report> reports = castForOperation(LocalTrackingEnabledCacheListener.class).getReports();
            final StringBuilder sb = new StringBuilder();
            for (Report report : reports) {
                if (sb.length() > 0) {
                    sb.append("\n====================================================================================================================\n");
                }
                sb.append('[').append(report.getEvent()).append("] ")
                    .append(full.format(report.getNumberOfInvocations())).append(" total, ")
                    .append(part.format(report.getNumberOfInvocationsPerSecond())).append(" i/s");
                for (StackTraceElement stackTraceElement : report.getStackTrace()) {
                    sb.append("\n\tat ").append(stackTraceElement);
                }
            }
            result = sb.toString();
        } else {
            throw new UnsupportedOperationException();
        }
        return result;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(_listener.getClass().getName(), null, getAttributesArray(), null, getOperationsArray(), null);
    }

    @Nonnull
    protected MBeanAttributeInfo[] getAttributesArray() {
        final List<MBeanAttributeInfo> result = getAttributes();
        return result.toArray(new MBeanAttributeInfo[result.size()]);
    }

    @Nonnull
    protected List<MBeanAttributeInfo> getAttributes() {
        final List<MBeanAttributeInfo> result = new ArrayList<>();
        if (_listener instanceof LocalTrackingEnabledCacheListener) {
            result.add(new MBeanAttributeInfo("numberOfLocalTrackedEvents", Integer.class.getName(), null, true, true, false));
        }
        return result;
    }

    @Nonnull
    protected MBeanOperationInfo[] getOperationsArray() {
        final List<MBeanOperationInfo> result = getOperations();
        return result.toArray(new MBeanOperationInfo[result.size()]);
    }

    @Nonnull
    protected List<MBeanOperationInfo> getOperations() {
        final List<MBeanOperationInfo> result = new ArrayList<>();
        if (_listener instanceof LocalTrackingEnabledCacheListener) {
            result.add(new MBeanOperationInfo("getReport", null, new MBeanParameterInfo[0], String.class.getName(), ACTION));
        }
        return result;
    }

    @Nonnull
    protected <T extends CacheListener> T castForAttribute(@Nonnull Class<T> type) throws AttributeNotFoundException {
        if (type.isInstance(_listener)) {
            return type.cast(_listener);
        } else {
            throw new AttributeNotFoundException();
        }
    }

    @Nonnull
    protected <T extends CacheListener> T castForOperation(@Nonnull Class<T> type) throws MBeanException {
        if (type.isInstance(_listener)) {
            return type.cast(_listener);
        } else {
            throw new MBeanException(null, "Illegal method.");
        }
    }

    @Override public AttributeList getAttributes(String[] attributes) { throw new UnsupportedOperationException(); }
    @Override public AttributeList setAttributes(AttributeList attributes) { throw new UnsupportedOperationException(); }


}
