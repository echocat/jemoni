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

import org.apache.commons.lang3.StringUtils;
import org.echocat.jomon.cache.*;
import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.ProducingType;

import javax.annotation.Nonnull;
import javax.management.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static javax.management.MBeanOperationInfo.ACTION;

public class CacheDynamicMBean implements DynamicMBean {

    private final Cache<?, ?> _cache;

    public CacheDynamicMBean(@Nonnull Cache<?, ?> cache) {
        _cache = cache;
    }

    @SuppressWarnings("OverlyLongMethod")
    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        final Object result;
        if ("type".equals(attribute)) {
            result = _cache.getClass().getName();
        } else if ("keyType".equals(attribute)) {
            result = _cache.getKeyType().getName();
        } else if ("valueType".equals(attribute)) {
            result = _cache.getValueType().getName();
        } else if ("size".equals(attribute)) {
            result = cast(StatisticsEnabledCache.class).size();
        } else if ("hitRatio".equals(attribute)) {
            final StatisticsEnabledCache<?, ?> statisticsEnabledCache = cast(StatisticsEnabledCache.class);
            final Long numberOfHits = statisticsEnabledCache.getNumberOfHits();
            final Long numberOfRequests = statisticsEnabledCache.getNumberOfRequests();
            if (numberOfHits != null && numberOfRequests != null) {
                result = numberOfRequests != 0 ? (double) numberOfHits / (double) numberOfRequests : 0;
            } else {
                result = null;
            }
        } else if ("dropRatio".equals(attribute)) {
            final StatisticsEnabledCache<?, ?> statisticsEnabledCache = cast(StatisticsEnabledCache.class);
            final Long numberOfDrops = statisticsEnabledCache.getNumberOfDrops();
            final Long numberOfRequests = statisticsEnabledCache.getNumberOfRequests();
            if (numberOfDrops != null && numberOfRequests != null) {
                result = numberOfRequests != 0 ? (double) numberOfDrops / (double) numberOfRequests : 0;
            } else {
                result = null;
            }
        } else if ("numberOfMisses".equals(attribute)) {
            final StatisticsEnabledCache<?, ?> statisticsEnabledCache = cast(StatisticsEnabledCache.class);
            final Long numberOfHits = statisticsEnabledCache.getNumberOfHits();
            final Long numberOfRequests = statisticsEnabledCache.getNumberOfRequests();
            if (numberOfHits != null && numberOfRequests != null) {
                result = numberOfRequests - numberOfHits;
            } else {
                result = null;
            }
        } else if ("numberOfRequests".equals(attribute)) {
            result = cast(StatisticsEnabledCache.class).getNumberOfRequests();
        } else if ("numberOfHits".equals(attribute)) {
            result = cast(StatisticsEnabledCache.class).getNumberOfHits();
        } else if ("numberOfDrops".equals(attribute)) {
            result = cast(StatisticsEnabledCache.class).getNumberOfDrops();
        } else if ("created".equals(attribute)) {
            result = cast(StatisticsEnabledCache.class).getCreated();
        } else if ("maximumLifetime".equals(attribute)) {
            final Duration maximumLifetime = cast(LimitedCache.class).getMaximumLifetime();
            result = maximumLifetime != null ? maximumLifetime.toString() : null;
        } else if ("capacity".equals(attribute)) {
            result = cast(LimitedCache.class).getCapacity();
        } else if ("producingType".equals(attribute)) {
            result = cast(ProducingTypeEnabledCache.class).getProducingType().name();
        } else if ("listeners".equals(attribute)) {
            // noinspection unchecked
            final Collection<CacheListener> listeners = cast(ListenerEnabledCache.class).getListeners();
            result = getListenersAsString(listeners);
        } else {
            throw new AttributeNotFoundException();
        }
        return result;
    }

    @Nonnull
    protected String getListenersAsString(@Nonnull Collection<CacheListener> listeners) {
        final StringBuilder sb = new StringBuilder();
        for (CacheListener listener : listeners) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(listener);
        }
        return sb.toString();
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        final String attributeName = attribute.getName();
        final Object value = attribute.getValue();
        if ("maximumLifetime".equals(attributeName)) {
            if (value != null && !(value instanceof String)) {
                throw new InvalidAttributeValueException();
            }
            cast(LimitedCache.class).setMaximumLifetime(value != null && !value.toString().trim().isEmpty() ? new Duration(value.toString()) : null);
        } else if ("capacity".equals(attributeName)) {
            if (value != null && !(value instanceof Number)) {
                throw new InvalidAttributeValueException();
            }
            cast(LimitedCache.class).setCapacity(value != null ? ((Number) value).longValue() : null);
        } else if ("producingType".equals(attributeName)) {
            if (value != null && !(value instanceof String)) {
                throw new InvalidAttributeValueException();
            }
            final ProducingType producingType;
            if (value == null || value.toString().trim().isEmpty()) {
                producingType = ProducingType.DEFAULT;
            } else {
                try {
                    producingType = ProducingType.valueOf(value.toString().trim());
                } catch (IllegalArgumentException ignored) {
                    throw new InvalidAttributeValueException("Illegal value: " + value + ". Possible values: " + StringUtils.join(ProducingType.values(), ", "));
                }
            }
            cast(ProducingTypeEnabledCache.class).setProducingType(producingType);
        } else {
            throw new AttributeNotFoundException();
        }
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        final Object result;
        if ("clear".equals(actionName) && _cache instanceof ClearableCache) {
            ((ClearableCache<?, ?>) _cache).clear();
            result = null;
        } else if ("resetStatistics".equals(actionName) && _cache instanceof StatisticsEnabledCache) {
            ((StatisticsEnabledCache<?, ?>) _cache).resetStatistics();
            result = null;
        } else if ("getListeners".equals(actionName) && _cache instanceof ListenerEnabledCache) {
            // noinspection unchecked
            result = getListenersAsString(((ListenerEnabledCache)_cache).getListeners());
        } else if ("getKeys".equals(actionName) && _cache instanceof KeysEnabledCache) {
            // noinspection unchecked
            try (final CloseableIterator<Object> iterator = ((KeysEnabledCache<Object, ?>) _cache).iterator()) {
                final StringBuilder sb = new StringBuilder();
                if (params.length > 0 && params[0] instanceof Number && ((Number) params[0]).intValue() >= 0) {
                    final int limit = ((Number) params[0]).intValue();
                    for (int i = 0; iterator.hasNext() && i < limit; i++) {
                        if (sb.length() > 0) {
                            sb.append('\n');
                        }
                        sb.append(iterator.next());
                    }
                } else {
                    while (iterator.hasNext()) {
                        if (sb.length() > 0) {
                            sb.append('\n');
                        }
                        sb.append(iterator.next());
                    }
                }
                result = sb.toString();
            }
        } else if ("get".equals(actionName)) {
            final String key = params.length > 0 && params[0] != null ? params[0].toString() : null;
            // noinspection unchecked
            final Object plainResult = ((Cache<String, Object>) _cache).get(key);
            result = plainResult != null ? plainResult.toString() : null;
        } else if ("remove".equals(actionName)) {
            final String key = params.length > 0 && params[0] != null ? params[0].toString() : null;
            // noinspection unchecked
            ((Cache<String, Object>) _cache).remove(key);
            result = null;
        } else if ("contains".equals(actionName)) {
            final String key = params.length > 0 && params[0] != null ? params[0].toString() : null;
            // noinspection unchecked
            result = ((Cache<String, Object>) _cache).contains(key);
        } else {
            throw new UnsupportedOperationException();
        }
        return result;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(_cache.getClass().getName(), null, getAttributes(), null, getOperations(), null);
    }

    @Nonnull
    protected MBeanAttributeInfo[] getAttributes() {
        final List<MBeanAttributeInfo> result = new ArrayList<>();
        result.add(new MBeanAttributeInfo("type", String.class.getName(), null, true, false, false));
        result.add(new MBeanAttributeInfo("keyType", String.class.getName(), null, true, false, false));
        result.add(new MBeanAttributeInfo("valueType", String.class.getName(), null, true, false, false));
        if (_cache instanceof StatisticsEnabledCache) {
            result.add(new MBeanAttributeInfo("size", Long.class.getName(), null, true, false, false));
            result.add(new MBeanAttributeInfo("hitRatio", Double.class.getName(), null, true, false, false));
            result.add(new MBeanAttributeInfo("dropRatio", Double.class.getName(), null, true, false, false));
            result.add(new MBeanAttributeInfo("numberOfRequests", Long.class.getName(), null, true, false, false));
            result.add(new MBeanAttributeInfo("numberOfHits", Long.class.getName(), null, true, false, false));
            result.add(new MBeanAttributeInfo("numberOfDrops", Long.class.getName(), null, true, false, false));
            result.add(new MBeanAttributeInfo("numberOfMisses", Long.class.getName(), null, true, false, false));
            result.add(new MBeanAttributeInfo("created", Date.class.getName(), null, true, false, false));
        }
        if (_cache instanceof LimitedCache) {
            result.add(new MBeanAttributeInfo("maximumLifetime", String.class.getName(), null, true, true, false));
            result.add(new MBeanAttributeInfo("capacity", Long.class.getName(), null, true, true, false));
        }
        if (_cache instanceof ListenerEnabledCache) {
            result.add(new MBeanAttributeInfo("listeners", String.class.getName(), null, true, false, false));
        }
        if (_cache instanceof ProducingTypeEnabledCache) {
            result.add(new MBeanAttributeInfo("producingType", String.class.getName(), null, true, true, false));
        }
        return result.toArray(new MBeanAttributeInfo[result.size()]);
    }

    @Nonnull
    protected MBeanOperationInfo[] getOperations() {
        final List<MBeanOperationInfo> result = new ArrayList<>();
        result.add(new MBeanOperationInfo("get", null, new MBeanParameterInfo[]{
            new MBeanParameterInfo("key", String.class.getName(), null)
        }, String.class.getName(), ACTION));
        result.add(new MBeanOperationInfo("remove", null, new MBeanParameterInfo[]{
            new MBeanParameterInfo("key", String.class.getName(), null)
        }, Void.TYPE.getName(), ACTION));
        result.add(new MBeanOperationInfo("contains", null, new MBeanParameterInfo[]{
            new MBeanParameterInfo("key", String.class.getName(), null)
        }, Boolean.TYPE.getName(), ACTION));
        if (_cache instanceof KeysEnabledCache) {
            result.add(new MBeanOperationInfo("getKeys", null, new MBeanParameterInfo[]{
                new MBeanParameterInfo("limit", Integer.class.getName(), null)
            }, String.class.getName(), ACTION));
        }
        if (_cache instanceof ClearableCache) {
            result.add(new MBeanOperationInfo("clear", null, new MBeanParameterInfo[0], Void.TYPE.getName(), ACTION));
        }
        if (_cache instanceof StatisticsEnabledCache) {
            result.add(new MBeanOperationInfo("resetStatistics", null, new MBeanParameterInfo[0], Void.TYPE.getName(), ACTION));
        }
        if (_cache instanceof ListenerEnabledCache) {
            result.add(new MBeanOperationInfo("getListeners", null, new MBeanParameterInfo[0], String.class.getName(), ACTION));
        }
        return result.toArray(new MBeanOperationInfo[result.size()]);
    }

    @Nonnull
    protected <T extends Cache<?, ?>> T cast(@Nonnull Class<T> type) throws AttributeNotFoundException {
        if (type.isInstance(_cache)) {
            return type.cast(_cache);
        } else {
            throw new AttributeNotFoundException();
        }
    }

    @Override public AttributeList getAttributes(String[] attributes) { throw new UnsupportedOperationException(); }
    @Override public AttributeList setAttributes(AttributeList attributes) { throw new UnsupportedOperationException(); }
}
