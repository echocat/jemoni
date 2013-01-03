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
import org.echocat.jomon.cache.Cache;
import org.echocat.jomon.cache.management.CacheDefinition;
import org.echocat.jomon.cache.management.CreationCacheListener;
import org.echocat.jomon.cache.management.DestroyCacheListener;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class JmxCacheRepositoryListener implements CreationCacheListener, DestroyCacheListener {

    private final JmxRegistry _registry;
    private final Map<String, Registration> _idToRegistration = new ConcurrentHashMap<>();

    public JmxCacheRepositoryListener(@Nonnull JmxRegistry registry) {
        _registry = registry;
    }

    @Override
    public void afterCreate(@Nonnull String id, @Nonnull CacheDefinition<?, ?, ?> definition, @Nonnull Cache<?, ?> cache) {
        final Registration registration = _registry.register(new CacheDynamicMBean(cache), Cache.class, id);
        _idToRegistration.put(id, registration);
    }

    @Override
    public boolean beforeDestroy(@Nonnull String id, @Nonnull Cache<?, ?> cache) {
        final Registration registration = _idToRegistration.get(id);
        if (registration != null) {
            closeQuietly(registration);
        }
        return true;
    }

    @Override public boolean beforeCreate(@Nonnull String id, @Nonnull CacheDefinition<?, ?, ?> definition) { return true; }
    @Override public void afterDestroy(@Nonnull String id, @Nonnull Cache<?, ?> cache) {}
}
