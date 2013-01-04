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

import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.echocat.jemoni.carbon.jmx.configuration.RulesConstants.SCHEMA_NAMESPACE;

@XmlType(name = "rule", namespace = SCHEMA_NAMESPACE, propOrder = {"includes", "excludes"})
public class Rule extends IncludeExcludeSupport<Rule> {

    @Nonnull
    public static Rule rule() {
        return new Rule();
    }

    private String _name = randomUUID().toString();
    private Duration _updateEvery = new Duration("10s");

    @Override
    @XmlElement(name = "include", required = false, namespace = SCHEMA_NAMESPACE)
    public List<ObjectRule> getIncludes() {
        return super.getIncludes();
    }

    @Override
    public void setIncludes(List<ObjectRule> includes) {
        super.setIncludes(includes);
    }

    @Override
    @XmlElement(name = "exclude", required = false, namespace = SCHEMA_NAMESPACE)
    public List<ObjectRule> getExcludes() {
        return super.getExcludes();
    }

    @Override
    public void setExcludes(List<ObjectRule> excludes) {
        super.setExcludes(excludes);
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

    @Nonnull
    public Rule name(@Nullable String name) {
        setName(name);
        return this;
    }

    @Nonnull
    public Rule updateEvery(@Nullable Duration updateEvery) {
        setUpdateEvery(updateEvery);
        return this;
    }

    @Nonnull
    public Rule updateEvery(@Nullable String updateEvery) {
        return updateEvery(updateEvery != null ? new Duration(updateEvery) : null);
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (o == this) {
            result = true;
        } else if (!super.equals(o) || o.getClass() != Rule.class) {
            result = false;
        } else {
            final Rule that = (Rule) o;
            result = (_name != null ? _name.equals(that._name) : that._name == null) && (_updateEvery != null ? _updateEvery.equals(that._updateEvery) : that._updateEvery == null);
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (_name != null ? _name.hashCode() : 0);
        result = 31 * result + (_updateEvery != null ? _updateEvery.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append('{');
        sb.append("name=").append(_name);
        sb.append(", updateEvery=").append(_updateEvery);
        final List<ObjectRule> includes = getIncludes();
        if (includes != null && !includes.isEmpty()) {
            sb.append(", includes=").append(includes);
        }
        final List<ObjectRule> excludes = getExcludes();
        if (excludes != null && !excludes.isEmpty()) {
            sb.append(", excludes=").append(excludes);
        }
        sb.append('}');
        return sb.toString();
    }

}
