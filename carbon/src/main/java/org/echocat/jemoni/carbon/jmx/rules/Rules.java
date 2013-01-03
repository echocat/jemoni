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

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.echocat.jemoni.carbon.jmx.rules.RulesConstants.SCHEMA_NAMESPACE;

@XmlRootElement(name = "rules", namespace = SCHEMA_NAMESPACE)
@XmlType(name = "rules", namespace = SCHEMA_NAMESPACE)
public class Rules implements Iterable<Rule> {

    @Nonnull
    public static Rules rules(@Nonnull Rule... rules) {
        final Rules result = new Rules();
        if (rules != null) {
            result.setRules(asList(rules));
        }
        return result;
    }

    private Collection<Rule> _rules;

    @XmlElement(name = "rule", required = false, namespace = SCHEMA_NAMESPACE)
    public Collection<Rule> getRules() {
        return _rules;
    }

    public void setRules(Collection<Rule> rules) {
        _rules = rules;
    }

    @Override
    public Iterator<Rule> iterator() {
        return _rules != null ? _rules.iterator() : Collections.<Rule>emptyIterator();
    }

    public boolean hasItems() {
        return _rules != null && !_rules.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (o == null || o.getClass() != Rules.class) {
            result = false;
        } else {
            final Rules that = (Rules) o;
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
        sb.append('}');
        return sb.toString();
    }

}
