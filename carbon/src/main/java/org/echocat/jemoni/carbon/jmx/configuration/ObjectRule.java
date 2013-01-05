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

package org.echocat.jemoni.carbon.jmx.configuration;

import org.echocat.jemoni.carbon.jmx.AttributeDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.ObjectName;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.echocat.jemoni.carbon.jmx.configuration.RulesConstants.SCHEMA_NAMESPACE;

@XmlType(name = "object", namespace = SCHEMA_NAMESPACE)
public class ObjectRule extends PatternRule<ObjectRule> {

    @Nonnull
    public static ObjectRule object() {
        return new ObjectRule();
    }

    private List<AttributeRule> _attributeRules;

    @XmlElement(name = "attribute", required = false, namespace = SCHEMA_NAMESPACE)
    public List<AttributeRule> getAttributeRules() {
        return _attributeRules;
    }

    public void setAttributeRules(List<AttributeRule> attributeRules) {
        _attributeRules = attributeRules;
    }

    @Nullable
    public Boolean apply(@Nullable AttributeDefinition input) {
        final Boolean result;
        if (input == null) {
            result = false;
        } else {
            final Boolean patternMatch = apply(input, getPattern());
            if (patternMatch != null && patternMatch) {
                final Boolean attributesMatch = apply(input, _attributeRules);
                result = attributesMatch == null || attributesMatch;
            } else {
                result = patternMatch;
            }
        }
        return result;
    }

    @Nullable
    protected Boolean apply(@Nonnull AttributeDefinition input, @Nullable Pattern pattern) {
        final Boolean result;
        if (pattern == null) {
            result = null;
        } else {
            final ObjectName objectName = input.getObjectName();
            if (objectName == null) {
                result = false;
            } else {
                result = pattern.matcher(objectName.toString()).matches();
            }
        }
        return result;
    }

    @Nullable
    protected Boolean apply(@Nonnull AttributeDefinition input, @Nullable List<AttributeRule> attributeRules) {
        Boolean result = null;
        if (attributeRules != null) {
            for (AttributeRule attributeRule : attributeRules) {
                final Boolean attributeMatch = attributeRule.apply(input);
                if (attributeMatch != null) {
                    result = attributeMatch;
                    if (attributeMatch) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    @Nonnull
    public ObjectRule attributes(@Nullable List<AttributeRule> attributes) {
        setAttributeRules(attributes);
        return this;
    }

    @Nonnull
    public ObjectRule attributes(@Nullable AttributeRule... attributes) {
        return attributes(attributes != null ? asList(attributes) : null);
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (super.equals(o) ) {
            final ObjectRule that = (ObjectRule) o;
            result = (_attributeRules != null ? _attributeRules.equals(that._attributeRules) : that._attributeRules == null);
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (_attributeRules != null ? _attributeRules.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + "->" + _attributeRules;
    }

}
