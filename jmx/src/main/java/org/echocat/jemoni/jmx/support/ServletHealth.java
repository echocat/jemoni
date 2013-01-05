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

package org.echocat.jemoni.jmx.support;

import org.apache.commons.collections15.map.LRUMap;
import org.echocat.jemoni.jmx.JmxRegistry;
import org.echocat.jemoni.jmx.Registration;
import org.echocat.jemoni.jmx.support.ServletHealth.ScopeMapping;
import org.echocat.jomon.runtime.concurrent.StopWatch;
import org.echocat.jomon.runtime.iterators.ConvertingIterator;
import org.echocat.jomon.runtime.math.OverPeriodAverageDoubleCounter;
import org.echocat.jomon.runtime.math.OverPeriodCounter;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.Entry;
import org.echocat.jomon.runtime.util.Entry.Impl;

import javax.annotation.*;
import javax.management.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.echocat.jemoni.jmx.JmxRegistry.getLocalInstance;
import static org.echocat.jemoni.jmx.support.SpringUtils.getBeanFor;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class ServletHealth implements AutoCloseable, Filter, Iterable<Entry<Pattern, ScopeMapping>> {

    public static final String MAPPING_INIT_ATTRIBUTE = "mapping";
    public static final String REGISTRY_REF_INIT_ATTRIBUTE = "registry-ref";
    public static final String INTERCEPTOR_REF_INIT_ATTRIBUTE = "interceptor-ref";

    public static final String REQUESTS_PER_SECOND_ATTRIBUTE_NAME = "requestsPerSecond";
    public static final String AVERAGE_REQUEST_DURATION_ATTRIBUTE_NAME = "averageRequestDuration";
    public static final String CURRENT_REQUEST_STOP_WATCH_ATTRIBUTE_NAME = ServletHealth.class.getName() + ".currentRequestStopWatch";

    private final Map<String, ScopeMapping> _pathToMappingCache = new LRUMap<>(10000);

    private JmxRegistry _registry;

    private Map<Pattern, ScopeMapping> _patternToMapping;
    private Map<String, ScopeMapping> _nameToMapping;
    private Interceptor _interceptor;

    private Registration _registration;

    public ServletHealth() {
        setMapping(null);
    }

    public void setMapping(@Nullable String mappingAsString) {
        final Map<Pattern, ScopeMapping> patternToMapping = new LinkedHashMap<>();
        final Interceptor interceptor = _interceptor;

        if (mappingAsString != null) {
            for (String parts : mappingAsString.split("[,\\n\\r]")) {
                final String trimmedPart = parts.trim();
                if (!trimmedPart.isEmpty()) {
                    final int lastArrow = trimmedPart.lastIndexOf('>');
                    if (lastArrow > 0 && lastArrow + 1 < trimmedPart.length()) {
                        try {
                            final Pattern pattern = Pattern.compile(trimmedPart.substring(0, lastArrow).trim());
                            final String name = trimmedPart.substring(lastArrow + 1).trim();
                            if (name.isEmpty()) {
                                throw new IllegalArgumentException("Illegal formatted mapping: " + mappingAsString);
                            }
                            final Collection<String> possibleSpecificNames = interceptor != null ? interceptor.getPossibleNames(name) : null;
                            patternToMapping.put(pattern, new ScopeMapping(name, possibleSpecificNames));
                        } catch (PatternSyntaxException e) {
                            throw new IllegalArgumentException("Illegal formatted mapping: " + mappingAsString, e);
                        }
                    } else {
                        throw new IllegalArgumentException("Illegal formatted mapping: " + mappingAsString);
                    }
                }
            }
        }

        patternToMapping.put(null, new ScopeMapping(null, Collections.<String>emptyList()));

        _nameToMapping = asNameToMapping(patternToMapping.values());
        _patternToMapping = unmodifiableMap(patternToMapping);
        synchronized (_pathToMappingCache) {
            _pathToMappingCache.clear();
        }
    }

    @Nullable
    public String getMapping() {
        final StringBuilder sb = new StringBuilder();

        for (Map.Entry<Pattern, ScopeMapping> patternAndMapping : _patternToMapping.entrySet()) {
            if (patternAndMapping.getKey() != null) {
                if (sb.length() > 0) {
                    sb.append(",\n");
                }
                sb.append(patternAndMapping.getKey()).append('>').append(patternAndMapping.getValue().getDefaultName());
            }
        }

        return sb.length() > 0 ? sb.toString() : null;
    }



    public Interceptor getInterceptor() {
        return _interceptor;
    }

    public void setInterceptor(Interceptor interceptor) {
        _interceptor = interceptor;
        setMapping(getMapping());
    }

    public void setRegistry(@Nullable JmxRegistry registry) {
        _registry = registry;
    }

    @Nonnull
    public JmxRegistry getRegistry() {
        final JmxRegistry registry = _registry;
        return registry != null ? registry : getLocalInstance();
    }

    @Nonnull
    protected Map<String, ScopeMapping> asNameToMapping(@Nonnull Iterable<ScopeMapping> values) {
        final Map<String, ScopeMapping> result = new LinkedHashMap<>();
        for (ScopeMapping mapping : values) {
            for (String name : mapping.getAllNames()) {
                result.put(name, mapping);
            }
        }
        return unmodifiableMap(result);
    }

    public void init() {
        _registration = getRegistry().register(new MBeanInformation(), getClass());
    }

    @Override
    public void init(@Nonnull FilterConfig filterConfig) throws ServletException {
        final String mapping = filterConfig.getInitParameter(MAPPING_INIT_ATTRIBUTE);
        if (mapping != null) {
            setMapping(mapping);
        }
        final String registryRef = filterConfig.getInitParameter(REGISTRY_REF_INIT_ATTRIBUTE);
        if (!isEmpty(registryRef)) {
            setRegistry(getBeanFor(filterConfig.getServletContext(), registryRef, JmxRegistry.class));
        }
        final String interceptorRef = filterConfig.getInitParameter(INTERCEPTOR_REF_INIT_ATTRIBUTE);
        if (!isEmpty(interceptorRef)) {
            setInterceptor(getBeanFor(filterConfig.getServletContext(), interceptorRef, Interceptor.class));
        }
        init();
    }

    @Override
    public void close() {
        try {
            closeQuietly(_registration);
        } finally {
            _registration = null;
        }
    }

    @Override
    public void destroy() {
        close();
    }

    @Override
    @Nonnull
    public Iterator<Entry<Pattern, ScopeMapping>> iterator() {
        return new ConvertingIterator<Map.Entry<Pattern, ScopeMapping>, Entry<Pattern, ScopeMapping>>(_patternToMapping.entrySet().iterator()) {
            @Override
            protected Entry<Pattern, ScopeMapping> convert(Map.Entry<Pattern, ScopeMapping> input) {
                return new Impl<>(input.getKey(), input.getValue());
            }
        };
    }

    @Override
    public void doFilter(@Nonnull ServletRequest request, @Nonnull ServletResponse response, @Nonnull FilterChain chain) throws IOException, ServletException {
        final StopWatch stopWatch = new StopWatch();
        request.setAttribute(CURRENT_REQUEST_STOP_WATCH_ATTRIBUTE_NAME, stopWatch);
        final ScopeMapping globalMapping =  _patternToMapping.get(null);
        final ScopeMapping specificMapping = request instanceof HttpServletRequest ? getMappingFor(((HttpServletRequest)request).getRequestURI()) : null;
        try {
            chain.doFilter(request, response);
        } finally {
            request.removeAttribute(CURRENT_REQUEST_STOP_WATCH_ATTRIBUTE_NAME);
            final Duration duration = stopWatch.getCurrentDuration();
            final Interceptor interceptor = _interceptor;
            if (interceptor == null || interceptor.isRecordAllowed(request, globalMapping, specificMapping)) {
                globalMapping.record(null, duration);
                if (specificMapping != null) {
                    final String targetName = interceptor != null ? interceptor.getSpecificTargetName(request, specificMapping) : null;
                    specificMapping.record(targetName, duration);
                }
            }
        }
    }

    @Nullable
    public static Duration findCurrentRequestDurationOf(@Nonnull ServletRequest request) {
        final Object plainStopWatch = request.getAttribute(CURRENT_REQUEST_STOP_WATCH_ATTRIBUTE_NAME);
        return plainStopWatch instanceof StopWatch ? ((StopWatch) plainStopWatch).getCurrentDuration() : null;
    }

    @Nullable
    protected ScopeMapping getMappingFor(@Nonnull String path) {
        ScopeMapping mapping;
        synchronized (_pathToMappingCache) {
            mapping = _pathToMappingCache.get(path);
        }
        if (mapping == null) {
            for (Map.Entry<Pattern, ScopeMapping> patternAndScope : _patternToMapping.entrySet()) {
                final Pattern pattern = patternAndScope.getKey();
                if (pattern != null && pattern.matcher(path).matches()) {
                    mapping = patternAndScope.getValue();
                    break;
                }
            }
            synchronized (_pathToMappingCache) {
                _pathToMappingCache.put(path, mapping);
            }
        }
        return mapping;
    }

    @Nullable
    protected ScopeMapping getMapping(@Nonnull String defaultName) {
        return _nameToMapping.get(defaultName);
    }

    public static class ScopeMapping {

        private final Map<String, MeasurePoints> _nameToMeasurePoints;
        private final String _defaultName;

        public ScopeMapping(@Nullable String defaultName, @Nullable Collection<String> names) {
            _defaultName = defaultName;
            _nameToMeasurePoints = new LinkedHashMap<>();
            if (names == null || names.isEmpty()) {
                _nameToMeasurePoints.put(defaultName, new MeasurePoints());
            } else {
                for (String possibleSpecificName : names) {
                    _nameToMeasurePoints.put(possibleSpecificName, new MeasurePoints());
                }
            }
        }

        @Nullable
        public String getDefaultName() {
            return _defaultName;
        }

        @Nonnull
        public Collection<String> getAllNames() {
            return unmodifiableCollection(_nameToMeasurePoints.keySet());
        }

        @Nonnegative
        public double getAverageRequestDuration(@Nonnull String name) {
            return _nameToMeasurePoints.get(name).getAverageRequestDuration();
        }

        @Nonnegative
        public double getRequestsPerSecond(@Nonnull String name) {
            return _nameToMeasurePoints.get(name).getRequestsPerSecond();
        }

        public void record(@Nullable String targetName, @Nonnull Duration requestDuration) {
            final String name = targetName != null ? targetName : _defaultName;
            final MeasurePoints measurePoints = _nameToMeasurePoints.get(name);
            if (measurePoints == null) {
                throw new IllegalStateException("Used a name that is unknown: " + name);
            }
            measurePoints.record(requestDuration);
        }

    }

    public static class MeasurePoints {
        private final OverPeriodCounter _requestsPerSecond = new OverPeriodCounter(new Duration("1m"), new Duration("1s"));
        private final OverPeriodAverageDoubleCounter _averageRequestDuration = new OverPeriodAverageDoubleCounter(new Duration("1m"), new Duration("1s"));

        public void record(@Nonnull Duration requestDuration) {
            _requestsPerSecond.record();
            _averageRequestDuration.record((double) requestDuration.toMilliSeconds());
        }

        @Nonnegative
        public double getAverageRequestDuration() {
            return _averageRequestDuration.get();
        }

        @Nonnegative
        public double getRequestsPerSecond() {
            return _requestsPerSecond.getAsDouble();
        }

    }

    protected class MBeanInformation implements DynamicMBean {

        @Override
        public Object getAttribute(String name) throws AttributeNotFoundException, MBeanException, ReflectionException {
            final Object result;
            final int firstDot = name.indexOf('.');
            final boolean withPrefix = firstDot > 0 && firstDot + 1 < name.length();
            final String mappingName = withPrefix ? name.substring(0, firstDot) : null;
            final String valueName = withPrefix ? name.substring(firstDot + 1) : name;
            final ScopeMapping mapping = _nameToMapping.get(mappingName);
            if (mapping != null) {
                if (REQUESTS_PER_SECOND_ATTRIBUTE_NAME.equals(valueName)) {
                    result = mapping.getRequestsPerSecond(mappingName);
                } else if (AVERAGE_REQUEST_DURATION_ATTRIBUTE_NAME.equals(valueName)) {
                    result = mapping.getAverageRequestDuration(mappingName);
                } else {
                    throw new AttributeNotFoundException();
                }
            } else {
                throw new AttributeNotFoundException();
            }
            return result;
        }

        @Nonnull
        protected MBeanAttributeInfo[] getMBeanAttributesFor() {
            final List<MBeanAttributeInfo> attributes = new ArrayList<>();
            for (ScopeMapping mapping : _patternToMapping.values()) {
                for (String name : mapping.getAllNames()) {
                    attributes.add(new MBeanAttributeInfo(
                        name != null ? name + "." + REQUESTS_PER_SECOND_ATTRIBUTE_NAME : REQUESTS_PER_SECOND_ATTRIBUTE_NAME,
                        Double.class.getName(),
                        null,
                        true,
                        false,
                        false
                    ));
                    attributes.add(new MBeanAttributeInfo(
                        name != null ? name + "." + AVERAGE_REQUEST_DURATION_ATTRIBUTE_NAME : AVERAGE_REQUEST_DURATION_ATTRIBUTE_NAME,
                        Double.class.getName(),
                        null,
                        true,
                        false,
                        false
                    ));
                }
            }
            return attributes.toArray(new MBeanAttributeInfo[attributes.size()]);
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            return new MBeanInfo(
                ServletHealth.this.getClass().getName(),
                "Display basic information of servlet handling.",
                getMBeanAttributesFor(),
                null,
                null,
                null
            );
        }

        @Override public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException { throw new UnsupportedOperationException(); }
        @Override public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException { throw new AttributeNotFoundException(); }
        @Override public AttributeList getAttributes(String[] attributes) { throw new UnsupportedOperationException(); }
        @Override public AttributeList setAttributes(AttributeList attributes) { throw new UnsupportedOperationException(); }

    }

    public static interface Interceptor {

        public boolean isRecordAllowed(@Nonnull ServletRequest request, @Nonnull ScopeMapping globalMapping, @Nullable ScopeMapping specificMapping);

        @Nullable
        public String getSpecificTargetName(@Nonnull ServletRequest request, @Nonnull ScopeMapping specificMapping);

        @Nullable
        public Collection<String> getPossibleNames(@Nonnull String defaultName);

    }

}
