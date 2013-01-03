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

package org.echocat.jemoni.carbon.jmx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.ObjectName;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.join;

public class AttributeDefinition {

    private final ObjectName _objectName;
    private final String _name;
    private final Class<?> _valueType;
    private final Set<AttributeDefinition> _children;

    public AttributeDefinition(@Nullable ObjectName objectName, @Nonnull String name, @Nonnull Class<?> valueType) {
        this(objectName, name, valueType, null);
    }

    public AttributeDefinition(@Nullable ObjectName objectName, @Nonnull String name, @Nonnull Class<?> valueType, @Nullable Set<AttributeDefinition> children) {
        _objectName = objectName;
        _name = name;
        _valueType = valueType;
        _children = children;
    }

    @Nonnull
    public ObjectName getObjectName() {
        return _objectName;
    }

    @Nonnull
    public String getName() {
        return _name;
    }

    @Nonnull
    public Class<?> getValueType() {
        return _valueType;
    }

    @Nonnull
    public Set<AttributeDefinition> getChildren() {
        return _children;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof AttributeDefinition)) {
            result = false;
        } else {
            final AttributeDefinition that = (AttributeDefinition) o;
            result = (_objectName != null ? _objectName.equals(that._objectName) : _objectName == null) && _name.equals(that._name) && (_children != null ? _children.equals(that._children) : that._children == null);
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = (_objectName != null ? _objectName.hashCode() : 0);
        result = 31 * result + _name.hashCode();
        result = 31 * result + (_children != null ? _children.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (_objectName != null) {
            sb.append(_objectName).append("->");
        }
        sb.append(_name).append('(').append(_valueType.getName()).append(')');
        if (_children != null && !_children.isEmpty()) {
            sb.append('{').append(join(_children, ',')).append('}');
        }
        return sb.toString();
    }
}
