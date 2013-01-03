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
import javax.management.ObjectName;
import java.util.Collection;
import java.util.Iterator;

public class AttributeDefinitions implements Iterable<AttributeDefinition> {

    private final ObjectName _objectName;
    private final Collection<AttributeDefinition> _definitions;

    public AttributeDefinitions(@Nonnull ObjectName objectName, @Nonnull Collection<AttributeDefinition> definitions) {
        _objectName = objectName;
        _definitions = definitions;
    }

    @Nonnull
    public ObjectName getObjectName() {
        return _objectName;
    }

    @Nonnull
    public Collection<AttributeDefinition> getDefinitions() {
        return _definitions;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof AttributeDefinitions)) {
            result = false;
        } else {
            final AttributeDefinitions that = (AttributeDefinitions) o;
            result = _objectName.equals(that._objectName) && _definitions.equals(that._definitions);
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = _objectName.hashCode();
        result = 31 * result + _definitions.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return _objectName + "->" + _definitions;
    }

    @Override
    public Iterator<AttributeDefinition> iterator() {
        return _definitions.iterator();
    }
}
