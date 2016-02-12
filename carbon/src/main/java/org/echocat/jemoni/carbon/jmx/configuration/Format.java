/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat JeMoni, Copyright (c) 2012-2016 echocat
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
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.echocat.jemoni.carbon.jmx.configuration.RulesConstants.SCHEMA_NAMESPACE;

@XmlType(name = "format", namespace = SCHEMA_NAMESPACE, propOrder = {"includes", "excludes"})
public class Format extends IncludeExcludeSupport<Format> {

    @Nonnull
    public static Format format(@Nonnull Pattern pattern, @Nonnull String replacement) {
        return new Format().pattern(pattern).replacement(replacement);
    }

    @Nonnull
    public static Format format(@Nonnull String pattern, @Nonnull String replacement) {
        return new Format().pattern(pattern).replacement(replacement);
    }

    private Pattern _pattern;
    private String _replacement;

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

    @XmlAttribute(name = "pattern", required = true)
    @XmlJavaTypeAdapter(PatternAdapter.class)
    public Pattern getPattern() {
        return _pattern;
    }

    public void setPattern(Pattern pattern) {
        _pattern = pattern;
    }

    @XmlAttribute(name = "replacement", required = true)
    public String getReplacement() {
        return _replacement;
    }

    public void setReplacement(String replacement) {
        _replacement = replacement;
    }

    @Nonnull
    public String format(@Nonnull String in) {
        final String result;
        final Pattern pattern = _pattern;
        final String replacement = _replacement;
        if (pattern != null) {
            final Matcher matcher = pattern.matcher(in);
            result = matcher.replaceAll(replacement != null ? replacement : "");
        } else {
            result = in;
        }
        return result;
    }

    @Nonnull
    public Format pattern(@Nonnull Pattern pattern) {
        setPattern(pattern);
        return this;
    }

    @Nonnull
    public Format pattern(@Nonnull String pattern) {
        return pattern(compile(pattern));
    }

    @Nonnull
    public Format replacement(@Nonnull String replacement) {
        setReplacement(replacement);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (o == this) {
            result = true;
        } else if (!super.equals(o) || o.getClass() != Format.class) {
            result = false;
        } else {
            final Format that = (Format) o;
            result = (_pattern != null ? _pattern.equals(that._pattern) : that._pattern == null) && (_replacement != null ? _replacement.equals(that._replacement) : that._replacement == null);
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (_pattern != null ? _pattern.hashCode() : 0);
        result = 31 * result + (_replacement != null ? _replacement.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append('{');
        sb.append("pattern=").append(_pattern);
        sb.append(", replacement=").append(_replacement);
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
