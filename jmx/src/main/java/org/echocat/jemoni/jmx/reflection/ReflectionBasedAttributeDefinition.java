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
import org.echocat.jemoni.jmx.AttributeDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.echocat.jemoni.jmx.AttributeDefinition.AccessMode.*;

public class ReflectionBasedAttributeDefinition<T, B> implements AttributeDefinition<T, B> {

    private final PropertyDescriptor _descriptor;

    private String _name;
    private String _description;
    private AccessMode _accessMode;
    private AttributeAccessor<T, B> _accessor;

    @Nonnull
    public ReflectionBasedAttributeDefinition(@Nonnull PropertyDescriptor descriptor) {
        _descriptor = descriptor;
        setName(null);
        setDescription(null);
        setAccessMode(null);
        setAccessor(null);
    }

    @Nonnull
    @Override
    public String getName() {
        return _name;
    }

    @Nullable
    @Override
    public String getDescription() {
        return _description;
    }

    @Override
    @Nonnull
    public Class<T> getType() {
        return _accessor.getType();
    }

    @Nonnull
    @Override
    public AccessMode getAccessMode() {
        return _accessMode;
    }

    @Override
    @Nullable
    public T get(@Nonnull B bean) throws Exception {
        final Method readMethod = _descriptor.getReadMethod();
        if (readMethod == null || !getAccessMode().isReadingPossible()) {
            throw new UnsupportedOperationException("Read is not supported from " + this + ".");
        }
        return _accessor.get(bean);
    }

    @Override
    public void set(@Nonnull B bean, @Nullable T value) throws Exception {
        final Method writeMethod = _descriptor.getWriteMethod();
        if (writeMethod == null || !getAccessMode().isWritingPossible()) {
            throw new UnsupportedOperationException("Write is not supported on " + this + ".");
        }
        _accessor.set(bean, value);
    }

    public void setName(@Nullable String name) {
        _name = !isEmpty(name) ? name : _descriptor.getName();
    }

    public void setDescription(@Nullable String description) {
        if (!isEmpty(description)) {
            _description = description;
        } else {
            final String shortDescription = _descriptor.getShortDescription();
            _description = !isEmpty(shortDescription) && !shortDescription.equals(_descriptor.getDisplayName()) ? shortDescription : null;
        }
    }

    public void setAccessMode(@Nullable AccessMode accessMode) {
        _accessMode = accessMode != null && accessMode != undefined ? accessMode : getAccessModeBy(_descriptor);
    }

    public void setAccessor(@Nullable AttributeAccessor<T, B> accessor) {
        _accessor = accessor != null ? accessor : getAccessorFor(_descriptor);
    }

    @Nonnull
    protected AccessMode getAccessModeBy(@Nonnull PropertyDescriptor descriptor) {
        final Method readMethod = descriptor.getReadMethod();
        final Method writeMethod = descriptor.getWriteMethod();
        final AccessMode accessMode;
        if (readMethod != null && writeMethod != null) {
            accessMode = AccessMode.readWrite;
        } else if (readMethod != null) {
            accessMode = AccessMode.readOnly;
        } else if (writeMethod != null) {
            accessMode = AccessMode.writeOnly;
        } else {
            throw new IllegalStateException("There is no read and no write method?");
        }
        return accessMode;
    }

    @Nonnull
    protected AttributeAccessor<T, B> getAccessorFor(@Nonnull PropertyDescriptor descriptor) {
        return new ReflectionBasedAttributeAccessor<>(descriptor);
    }

    @Nonnull
    protected PropertyDescriptor getDescriptor() {
        return _descriptor;
    }

    @Nonnull
    protected AttributeAccessor<T, B> getAccessor() {
        return _accessor;
    }

    @Override
    public String toString() {
        final String am;
        final AccessMode accessMode = getAccessMode();
        if (accessMode == readOnly) {
            am = " (r)";
        } else if (accessMode == writeOnly) {
            am = " (w)";
        } else if (accessMode == readWrite) {
            am = " (rw)";
        } else {
            am = "";
        }
        return getType().getName() + " " + getName() + am;
    }
}
