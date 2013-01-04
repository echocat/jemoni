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

import org.echocat.jomon.runtime.jaxb.PatternAdapter;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public abstract class PatternRule<T extends PatternRule<T>> {

    private Pattern _pattern;

    @XmlAttribute(name = "pattern", required = false)
    @XmlJavaTypeAdapter(PatternAdapter.class)
    public Pattern getPattern() {
        return _pattern;
    }

    public void setPattern(Pattern pattern) {
        _pattern = pattern;
    }

    @Nonnull
    public T pattern(@Nonnull Pattern pattern) {
        setPattern(pattern);
        return thisInstance();
    }

    @Nonnull
    public T pattern(@Nonnull String pattern) {
        return pattern(compile(pattern));
    }

    @Nonnull
    protected T thisInstance() {
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (o == null || !getClass().equals(o.getClass())) {
            result = false;
        } else {
            final PatternRule<?> that = (PatternRule) o;
            if (_pattern != null) {
                result = _pattern.pattern().equals(that._pattern.pattern()) && _pattern.flags() == that._pattern.flags();
            } else {
                result = that._pattern == null;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        return _pattern != null ? _pattern.hashCode() : 0;
    }

    @Override
    public String toString() {
        return _pattern != null ? _pattern.toString() : "<null>";
    }

}
