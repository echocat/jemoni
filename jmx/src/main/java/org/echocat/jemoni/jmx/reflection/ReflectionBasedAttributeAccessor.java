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

package org.echocat.jemoni.jmx.reflection;

import org.echocat.jemoni.jmx.AttributeAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import static org.echocat.jemoni.jmx.reflection.ReflectionBasedUtils.correctTypeIfNeeded;

public class ReflectionBasedAttributeAccessor<T, B> implements AttributeAccessor<T, B> {

    private final PropertyDescriptor _descriptor;

    @Nonnull
    public ReflectionBasedAttributeAccessor(@Nonnull PropertyDescriptor descriptor) {
        _descriptor = descriptor;
    }

    @Nonnull
    @Override
    public Class<T> getType() {
        // noinspection unchecked
        return (Class<T>) correctTypeIfNeeded(_descriptor.getPropertyType());
    }

    @Override
    public void set(@Nonnull B bean, @Nullable T value) throws Exception {
        final Method writeMethod = _descriptor.getWriteMethod();
        if (writeMethod == null) {
            throw new UnsupportedOperationException("Write is not supported on " + this + ".");
        }
        final Class<T> type = getType();
        if (value != null && !type.isInstance(value)) {
            throw new IllegalArgumentException("The provided value '" + value + "' is not of type " + type.getName() + ".");
        }
        writeMethod.invoke(bean, value);
    }

    @Override
    @Nullable
    public T get(@Nonnull B bean) throws Exception {
        final Method readMethod = _descriptor.getReadMethod();
        if (readMethod == null) {
            throw new UnsupportedOperationException("Read is not supported from " + this + ".");
        }
        final Object value = readMethod.invoke(bean);
        final Class<T> type = getType();
        if (value != null && !type.isInstance(value)) {
            throw new IllegalStateException("The returned value '" + value + "' is not of expected type " + type.getName() + ".");
        }
        return type.cast(value);
    }

}
