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
import javax.xml.bind.annotation.XmlType;
import java.util.regex.Pattern;

import static org.echocat.jemoni.carbon.jmx.configuration.RulesConstants.SCHEMA_NAMESPACE;

@XmlType(name = "attribute", namespace = SCHEMA_NAMESPACE)
public class AttributeRule extends PatternRule<AttributeRule> {

    @Nonnull
    public static AttributeRule attribute(@Nonnull String pattern) {
        return new AttributeRule().pattern(pattern);
    }

    @Nonnull
    public static AttributeRule attribute(@Nullable Pattern pattern) {
        return new AttributeRule().pattern(pattern);
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
