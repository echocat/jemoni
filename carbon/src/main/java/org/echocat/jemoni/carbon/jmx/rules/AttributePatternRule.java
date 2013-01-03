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
import javax.xml.bind.annotation.XmlType;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.echocat.jemoni.carbon.jmx.rules.RulesConstants.SCHEMA_NAMESPACE;

@XmlType(name = "attribute", namespace = SCHEMA_NAMESPACE)
public class AttributePatternRule extends PatternRule {

    @Nonnull
    public static AttributePatternRule attribute(@Nullable String pattern) {
        return attribute(pattern != null ? compile(pattern) : null);
    }

    @Nonnull
    public static AttributePatternRule attribute(@Nullable Pattern pattern) {
        final AttributePatternRule result = new AttributePatternRule();
        result.setPattern(pattern);
        return result;
    }

    @Nullable
    public Boolean apply(@Nullable AttributeDefinition input) {
        final Boolean result;
        if (input != null) {
            final String attributeName = input.getName();
            if (attributeName != null) {
                final Pattern pattern = getPattern();
                if (pattern != null) {
                    result = pattern.matcher(attributeName).matches();
                } else {
                    result = null;
                }
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }

}
