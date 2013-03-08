package org.echocat.jemoni.jmx.support;

import org.echocat.jemoni.jmx.support.ServletHealth.ScopeMapping;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import java.util.Collection;
import java.util.LinkedHashSet;

import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class CombinedServletHealthInterceptor implements ServletHealthInterceptor {

    private Iterable<ServletHealthInterceptor> _interceptors;

    public CombinedServletHealthInterceptor() {
        this((Iterable<ServletHealthInterceptor>) null);
    }

    public CombinedServletHealthInterceptor(@Nullable Iterable<ServletHealthInterceptor> interceptors) {
        _interceptors = interceptors;
    }

    public CombinedServletHealthInterceptor(@Nullable ServletHealthInterceptor... interceptors) {
        this(interceptors != null ? asImmutableList(interceptors) : null);
    }

    public Iterable<ServletHealthInterceptor> getInterceptors() {
        return _interceptors;
    }

    public void setInterceptors(Iterable<ServletHealthInterceptor> interceptors) {
        _interceptors = interceptors;
    }

    @Override
    public boolean isRecordAllowed(@Nonnull ServletRequest request, @Nonnull ScopeMapping globalMapping, @Nullable ScopeMapping specificMapping) {
        boolean result = true;
        final Iterable<ServletHealthInterceptor> interceptors = _interceptors;
        if (interceptors != null) {
            for (ServletHealthInterceptor interceptor : interceptors) {
                if (!interceptor.isRecordAllowed(request, globalMapping, specificMapping)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    @Nullable
    @Override
    public String getSpecificTargetName(@Nonnull ServletRequest request, @Nonnull ScopeMapping specificMapping) {
        String result = null;
        final Iterable<ServletHealthInterceptor> interceptors = _interceptors;
        if (interceptors != null) {
            for (ServletHealthInterceptor interceptor : interceptors) {
                result = interceptor.getSpecificTargetName(request, specificMapping);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    @Nullable
    @Override
    public Collection<String> getPossibleNames(@Nonnull String defaultName) {
        Collection<String> result = null;
        final Iterable<ServletHealthInterceptor> interceptors = _interceptors;
        if (interceptors != null) {
            for (ServletHealthInterceptor interceptor : interceptors) {
                final Collection<String> possibleNamesOfInterceptor = interceptor.getPossibleNames(defaultName);
                if (possibleNamesOfInterceptor != null) {
                    if (result == null) {
                        result = new LinkedHashSet<>();
                    }
                    result.addAll(possibleNamesOfInterceptor);
                }
            }
        }
        return result;
    }

}
