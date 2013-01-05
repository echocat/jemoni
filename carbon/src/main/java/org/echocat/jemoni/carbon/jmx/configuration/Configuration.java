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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static org.echocat.jemoni.carbon.jmx.configuration.RulesConstants.SCHEMA_NAMESPACE;

@XmlRootElement(name = "configuration", namespace = SCHEMA_NAMESPACE)
@XmlType(name = "configuration", namespace = SCHEMA_NAMESPACE, propOrder = {"rules", "formats"})
public class Configuration implements Iterable<Rule> {

    @Nonnull
    public static Configuration configuration() {
        return new Configuration();
    }

    private List<Rule> _rules;
    private List<Format> _formats;

    @XmlElement(name = "rule", required = false, namespace = SCHEMA_NAMESPACE)
    public List<Rule> getRules() {
        return _rules;
    }

    public void setRules(List<Rule> rules) {
        _rules = rules;
    }

    @XmlElement(name = "format", required = false, namespace = SCHEMA_NAMESPACE)
    public List<Format> getFormats() {
        return _formats;
    }

    public void setFormats(List<Format> formats) {
        _formats = formats;
    }

    @Override
    public Iterator<Rule> iterator() {
        return _rules != null ? _rules.iterator() : Collections.<Rule>emptyIterator();
    }

    public boolean hasItems() {
        return _rules != null && !_rules.isEmpty();
    }

    @Nonnull
    public Configuration rules(@Nullable List<Rule> rules) {
        setRules(rules);
        return this;
    }

    @Nonnull
    public Configuration rules(@Nullable Rule... rules) {
        return rules(rules != null ? asList(rules) : null);
    }

    @Nonnull
    public Configuration formats(@Nullable List<Format> formats) {
        setFormats(formats);
        return this;
    }

    @Nonnull
    public Configuration formats(@Nullable Format... formats) {
        return formats(formats != null ? asList(formats) : null);
    }

    @Nonnull
    public String format(@Nonnull AttributeDefinition what, @Nonnull String propertyName) {
        final List<Format> formats = _formats;
        String out = propertyName;
        if (formats != null) {
            for (Format format : formats) {
                if (format.apply(what)) {
                    out = format.format(propertyName);
                }
            }
        }
        return out;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (o == null || o.getClass() != Configuration.class) {
            result = false;
        } else {
            final Configuration that = (Configuration) o;
            result = (_rules != null ? _rules.equals(that._rules) : that._rules == null);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return _rules != null ? _rules.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append('{');
        sb.append("rules=").append(_rules);
        sb.append(", formats=").append(_formats);
        sb.append('}');
        return sb.toString();
    }

}
