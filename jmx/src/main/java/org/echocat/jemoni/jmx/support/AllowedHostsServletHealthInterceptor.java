package org.echocat.jemoni.jmx.support;

import org.echocat.jemoni.jmx.support.ServletHealth.ScopeMapping;
import org.echocat.jomon.cache.Cache;
import org.echocat.jomon.cache.ClearableCache;
import org.echocat.jomon.cache.LruCache;
import org.echocat.jomon.cache.management.CacheProvider;
import org.echocat.jomon.runtime.util.ValueProducer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.valueOf;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.split;
import static org.echocat.jemoni.jmx.support.InetAddressMatcher.matcher;
import static org.echocat.jomon.cache.management.DefaultCacheDefinition.lruCache;

public class AllowedHostsServletHealthInterceptor extends ServletHealthInterceptorAdapter {

    private static final Pattern ADDRESS_AND_BITS_EXTRACT_PATTERN = Pattern.compile("([^/]*)(?:|/(\\d+))");

    private final Cache<String, Boolean> _addressToAllowed;
    private final ValueProducer<String, Boolean> _addressToAllowedProducer = new ValueProducer<String, Boolean>() { @Nonnull @Override public Boolean produce(@Nonnull String remoteAddress) throws Exception {
        return isRespectedByIncludes(remoteAddress) && !isRespectedByExcludes(remoteAddress);
    }};

    private Collection<InetAddressMatcher> _includes;
    private Collection<InetAddressMatcher> _excludes;

    public AllowedHostsServletHealthInterceptor() {
        this(null);
    }

    public AllowedHostsServletHealthInterceptor(@Nullable CacheProvider cacheProvider) {
        _addressToAllowed = cacheProvider != null ? cacheProvider.provide(AllowedHostsServletHealthInterceptor.class, "addressToAllowed", lruCache(String.class, Boolean.class).withCapacity(1000)) : createFallBackCache();
    }

    @Override
    public boolean isRecordAllowed(@Nonnull ServletRequest request, @Nonnull ScopeMapping globalMapping, @Nullable ScopeMapping specificMapping) {
        final String remoteAddress = request.getRemoteAddr();
        return _addressToAllowed.get(remoteAddress, _addressToAllowedProducer);
    }

    protected boolean isRespectedByIncludes(@Nonnull String address) {
        return isRespectedBy(_includes, address, true);
    }

    protected boolean isRespectedByExcludes(@Nonnull String address) {
        return isRespectedBy(_excludes, address, false);
    }

    protected boolean isRespectedBy(@Nullable Collection<InetAddressMatcher> matches, @Nonnull String address, boolean fallbackValue) {
        boolean result;
        if (matches != null) {
            result = false;
            for (InetAddressMatcher include : matches) {
                if (include.matches(address)) {
                    result = true;
                    break;
                }
            }
        } else {
            result = fallbackValue;
        }
        return result;
    }

    public Collection<InetAddressMatcher> getIncludes() {
        return _includes;
    }

    public void setIncludes(Collection<InetAddressMatcher> includes) {
        _includes = includes;
        if (_addressToAllowed instanceof ClearableCache) {
            ((ClearableCache) _addressToAllowed).clear();
        }
    }

    public Collection<InetAddressMatcher> getExcludes() {
        return _excludes;
    }

    public void setExcludes(Collection<InetAddressMatcher> excludes) {
        _excludes = excludes;
        if (_addressToAllowed instanceof ClearableCache) {
            ((ClearableCache) _addressToAllowed).clear();
        }
    }

    public String getIncludesPattern() {
        return toPattern(getIncludes());
    }

    public void setIncludesPattern(String pattern) {
        setIncludes(parseMatchersBy(pattern));
    }

    public String getExcludesPattern() {
        return toPattern(getExcludes());
    }

    public void setExcludesPattern(String pattern) {
        setExcludes(parseMatchersBy(pattern));
    }

    @Nullable
    protected Collection<InetAddressMatcher> parseMatchersBy(@Nullable String pattern) throws IllegalArgumentException {
        final Collection<InetAddressMatcher> matches;
        if (isEmpty(pattern)) {
            matches = null;
        } else {
            matches = new ArrayList<>();
            final String[] patternParts = split(pattern, ",;");
            for (String patternPart : patternParts) {
                final String trimmedPatternPart = patternPart.trim();
                if (!trimmedPatternPart.isEmpty()) {
                    matches.add(parseMatcherBy(patternPart));
                }
            }
        }
        return matches;
    }

    @Nullable
    protected String toPattern(@Nullable Collection<InetAddressMatcher> matchers) {
        final String result;
        if (matchers == null) {
            result = null;
        } else {
            final StringBuilder sb = new StringBuilder();
            for (InetAddressMatcher matcher : matchers) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(matcher.getAddress().getHostAddress());
                final int bits = matcher.getBits();
                if (bits > 0) {
                    sb.append('/').append(bits);
                }
            }
            result = sb.toString();
        }
        return result;
    }

    @Nonnull
    protected InetAddressMatcher parseMatcherBy(@Nonnull String pattern) {
        final Matcher regexMatcher = ADDRESS_AND_BITS_EXTRACT_PATTERN.matcher(pattern);
        if (!regexMatcher.matches()) {
            throw new IllegalArgumentException("Pattern does not match required syntax \"<host>[/<bits>][,...]\": " + pattern);
        }
        final String host = regexMatcher.group(1);
        final String plainBits = regexMatcher.group(2);
        final int bits;
        try {
            bits = isEmpty(plainBits) ? 0 : valueOf(plainBits);
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException("Illegal pattern: " + pattern);
        }
        return matcher(host, bits);
    }

    @Nonnull
    protected Cache<String, Boolean> createFallBackCache() {
        final LruCache<String, Boolean> cache = new LruCache<>(String.class, Boolean.class);
        cache.setCapacity(1000L);
        return cache;
    }

}
