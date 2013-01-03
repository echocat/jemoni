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

import org.apache.commons.collections15.map.LRUMap;
import org.echocat.jemoni.jmx.JmxRegistry;
import org.echocat.jemoni.jmx.Registration;
import org.echocat.jomon.runtime.concurrent.StopWatch;
import org.echocat.jomon.runtime.math.OverPeriodAverageDoubleCounter;
import org.echocat.jomon.runtime.math.OverPeriodCounter;
import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.*;
import javax.management.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class ServletHealth implements AutoCloseable, Filter {

    public static final String REQUESTS_PER_SECOND_ATTRIBUTE_NAME = "requestsPerSecond";
    public static final String AVERAGE_REQUEST_DURATION_ATTRIBUTE_NAME = "averageRequestDuration";
    public static final String CURRENT_REQUEST_STOP_WATCH_ATTRIBUTE_NAME = ServletHealth.class.getName() + ".currentRequestStopWatch";

    private final Map<String, ScopeMapping> _pathToMappingCache = new LRUMap<>(10000);
    private final JmxRegistry _registry;

    private Map<Pattern, ScopeMapping> _patternToMapping;
    private Map<String, ScopeMapping> _nameToMapping;
    private Interceptor _interceptor;

    private Registration _registration;

    public ServletHealth(@Nonnull JmxRegistry registry) {
        _registry = registry;
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

        for (Entry<Pattern, ScopeMapping> patternAndMapping : _patternToMapping.entrySet()) {
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

    @Nonnull
    protected Map<String, ScopeMapping> asNameToMapping(@Nonnull Iterable<ScopeMapping> values) {
        final Map<String, ScopeMapping> result = new HashMap<>();
        for (ScopeMapping mapping : values) {
            for (String name : mapping.getAllNames()) {
                result.put(name, mapping);
            }
        }
        return unmodifiableMap(result);
    }

    @PostConstruct
    public void init() throws Exception {
        _registration = _registry.register(new MBeanInformation(), getClass());
    }

    @Override
    @PreDestroy
    public void close() throws Exception {
        try {
            closeQuietly(_registration);
        } finally {
            _registration = null;
        }
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
            for (Entry<Pattern, ScopeMapping> patternAndScope : _patternToMapping.entrySet()) {
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

    @Override public void init(FilterConfig filterConfig) throws ServletException {}
    @Override public void destroy() {}

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
