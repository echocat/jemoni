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

package org.echocat.jemoni.carbon.jmx.rules;

import org.echocat.jemoni.carbon.jmx.AttributeDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.ObjectName;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collection;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.regex.Pattern.compile;
import static org.echocat.jemoni.carbon.jmx.rules.RulesConstants.SCHEMA_NAMESPACE;

@XmlType(name = "object", namespace = SCHEMA_NAMESPACE)
public class ObjectPatternRule extends PatternRule {

    @Nonnull
    public static ObjectPatternRule object(@Nullable AttributePatternRule... attributes) {
        return object((Pattern) null, attributes);
    }

    @Nonnull
    public static ObjectPatternRule object(@Nullable String pattern, @Nullable AttributePatternRule... attributes) {
        return object(pattern != null ? compile(pattern) : null, attributes);
    }

    @Nonnull
    public static ObjectPatternRule object(@Nullable Pattern pattern, @Nullable AttributePatternRule... attributes) {
        final ObjectPatternRule result = new ObjectPatternRule();
        result.setPattern(pattern);
        if (attributes != null) {
            result.setAttributeRules(asList(attributes));
        }
        return result;
    }

    private Collection<AttributePatternRule> _attributeRules;

    @XmlElement(name = "attribute", required = false, namespace = SCHEMA_NAMESPACE)
    public Collection<AttributePatternRule> getAttributeRules() {
        return _attributeRules;
    }

    public void setAttributeRules(Collection<AttributePatternRule> attributeRules) {
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
    protected Boolean apply(@Nonnull AttributeDefinition input, @Nullable Collection<AttributePatternRule> attributePatternRules) {
        Boolean result = null;
        if (attributePatternRules != null) {
            for (AttributePatternRule attributePatternRule : attributePatternRules) {
                final Boolean attributeMatch = attributePatternRule.apply(input);
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

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (super.equals(o) ) {
            final ObjectPatternRule that = (ObjectPatternRule) o;
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
