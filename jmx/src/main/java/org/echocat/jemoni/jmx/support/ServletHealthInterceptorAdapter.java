package org.echocat.jemoni.jmx.support;

import org.echocat.jemoni.jmx.support.ServletHealth.ScopeMapping;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import java.util.Collection;

public class ServletHealthInterceptorAdapter implements ServletHealthInterceptor {

    @Override
    public boolean isRecordAllowed(@Nonnull ServletRequest request, @Nonnull ScopeMapping globalMapping, @Nullable ScopeMapping specificMapping) {
        return true;
    }

    @Nullable
    @Override
    public String getSpecificTargetName(@Nonnull ServletRequest request, @Nonnull ScopeMapping specificMapping) {
        return null;
    }

    @Nullable
    @Override
    public Collection<String> getPossibleNames(@Nonnull String defaultName) {
        return null;
    }

}
