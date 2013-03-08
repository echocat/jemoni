package org.echocat.jemoni.jmx.support;

import org.echocat.jemoni.jmx.support.ServletHealth.ScopeMapping;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import java.util.Collection;

public interface ServletHealthInterceptor {

    public boolean isRecordAllowed(@Nonnull ServletRequest request, @Nonnull ScopeMapping globalMapping, @Nullable ScopeMapping specificMapping);

    @Nullable
    public String getSpecificTargetName(@Nonnull ServletRequest request, @Nonnull ScopeMapping specificMapping);

    @Nullable
    public Collection<String> getPossibleNames(@Nonnull String defaultName);

}
