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
import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.echocat.jemoni.carbon.jmx.rules.RulesConstants.SCHEMA_NAMESPACE;

@XmlType(name = "rule", namespace = SCHEMA_NAMESPACE, propOrder = {"includes", "excludes"})
public class Rule {

    @Nonnull
    public static Rule rule(@Nullable String name, @Nullable Duration updateEvery, @Nullable Collection<ObjectPatternRule> includes, @Nullable Collection<ObjectPatternRule> excludes) {
        final Rule result = new Rule();
        if (!isEmpty(name)) {
            result.setName(name);
        }
        if (updateEvery != null) {
            result.setUpdateEvery(updateEvery);
        }
        result.setIncludes(includes);
        result.setExcludes(excludes);
        return result;
    }

    @Nonnull
    public static Rule rule(@Nullable Duration updateEvery, @Nullable Collection<ObjectPatternRule> includes, @Nullable Collection<ObjectPatternRule> excludes) {
        return rule(null, updateEvery, includes, excludes);
    }

    @Nonnull
    public static Rule rule(@Nullable String name, @Nullable String updateEvery, @Nullable Collection<ObjectPatternRule> includes, @Nullable Collection<ObjectPatternRule> excludes) {
        return rule(name, updateEvery != null ? new Duration(updateEvery) : null, includes, excludes);
    }

    @Nonnull
    public static Rule rule(@Nullable String updateEvery, @Nullable Collection<ObjectPatternRule> includes, @Nullable Collection<ObjectPatternRule> excludes) {
        return rule(null, updateEvery, includes, excludes);
    }

    @Nullable
    public static Collection<ObjectPatternRule> includes(@Nullable ObjectPatternRule... rules) {
        return rules != null ? asList(rules) : null;
    }

    @Nullable
    public static Collection<ObjectPatternRule> excludes(@Nullable ObjectPatternRule... rules) {
        return includes(rules);
    }

    private Collection<ObjectPatternRule> _includes;
    private Collection<ObjectPatternRule> _excludes;
    private String _name = randomUUID().toString();
    private Duration _updateEvery = new Duration("10s");

    @XmlElement(name = "include", required = false, namespace = SCHEMA_NAMESPACE)
    public Collection<ObjectPatternRule> getIncludes() {
        return _includes;
    }

    public void setIncludes(Collection<ObjectPatternRule> includes) {
        _includes = includes;
    }

    @XmlElement(name = "exclude", required = false, namespace = SCHEMA_NAMESPACE)
    public Collection<ObjectPatternRule> getExcludes() {
        return _excludes;
    }

    public void setExcludes(Collection<ObjectPatternRule> excludes) {
        _excludes = excludes;
    }

    @Nonnull
    @XmlAttribute(name = "name", required = false)
    public String getName() {
        return _name;
    }

    public void setName(@Nonnull String name) {
        _name = name;
    }

    @Nonnull
    @XmlAttribute(name = "updateEvery", required = false)
    public Duration getUpdateEvery() {
        return _updateEvery;
    }

    public void setUpdateEvery(@Nonnull Duration updateEvery) {
        _updateEvery = updateEvery;
    }

    public boolean apply(@Nullable AttributeDefinition input) {
        final boolean result;
        if (input != null) {
            final Boolean includeMatch = apply(input, _includes);
            if (includeMatch == null || includeMatch) {
                final Boolean excludeMatch = apply(input, _excludes);
                result = excludeMatch == null || !excludeMatch;
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }

    @Nullable
    protected Boolean apply(@Nonnull AttributeDefinition attributeDefinition, @Nullable Collection<ObjectPatternRule> rules) {
        Boolean result;
        if (rules == null || rules.isEmpty()) {
            result = null;
        } else {
            result = null;
            for (ObjectPatternRule rule : rules) {
                final Boolean ruleMatch = rule.apply(attributeDefinition);
                if (ruleMatch != null) {
                    result = ruleMatch;
                    if (ruleMatch) {
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
        if (this == o) {
            result = true;
        } else if (o == null || o.getClass() != Rule.class) {
            result = false;
        } else {
            final Rule that = (Rule) o;
            result = (_name != null ? _name.equals(that._name) : that._name == null) && (_updateEvery != null ? _updateEvery.equals(that._updateEvery) : that._updateEvery == null) && (_includes != null ? _includes.equals(that._includes) : that._includes == null) && (_excludes != null ? _excludes.equals(that._excludes) : that._excludes == null);
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = _name != null ? _name.hashCode() : 0;
        result = 31 * result + (_updateEvery != null ? _updateEvery.hashCode() : 0);
        result = 31 * result + (_includes != null ? _includes.hashCode() : 0);
        result = 31 * result + (_excludes != null ? _excludes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append('{');
        sb.append("name=").append(_name);
        sb.append(", updateEvery=").append(_updateEvery);
        if (_includes != null && !_includes.isEmpty()) {
            sb.append(", includes=").append(_includes);
        }
        if (_excludes != null && !_excludes.isEmpty()) {
            sb.append(", excludes=").append(_excludes);
        }
        sb.append('}');
        return sb.toString();
    }

}
