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

import org.echocat.jemoni.carbon.jmx.AttributeDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static java.util.Arrays.asList;

public abstract class IncludeExcludeSupport<T extends IncludeExcludeSupport<T>> {

    private List<ObjectRule> _includes;
    private List<ObjectRule> _excludes;

    protected List<ObjectRule> getIncludes() {
        return _includes;
    }

    protected void setIncludes(List<ObjectRule> includes) {
        _includes = includes;
    }

    protected List<ObjectRule> getExcludes() {
        return _excludes;
    }

    protected void setExcludes(List<ObjectRule> excludes) {
        _excludes = excludes;
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
    protected Boolean apply(@Nonnull AttributeDefinition attributeDefinition, @Nullable List<ObjectRule> rules) {
        Boolean result;
        if (rules == null || rules.isEmpty()) {
            result = null;
        } else {
            result = null;
            for (ObjectRule rule : rules) {
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

    @Nonnull
    public T includes(@Nullable List<ObjectRule> includes) {
        setIncludes(includes);
        return thisInstance();
    }

    @Nonnull
    public T includes(@Nullable ObjectRule... includes) {
        return includes(includes != null ? asList(includes) : null);
    }

    @Nonnull
    public T excludes(@Nullable List<ObjectRule> excludes) {
        setExcludes(excludes);
        return thisInstance();
    }

    @Nonnull
    public T excludes(@Nullable ObjectRule... excludes) {
        return excludes(excludes != null ? asList(excludes) : null);
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
        } else if (!(o instanceof IncludeExcludeSupport)) {
            result = false;
        } else {
            final IncludeExcludeSupport<?> that = (IncludeExcludeSupport) o;
            result = (_includes != null ? _includes.equals(that._includes) : that._includes == null) && (_excludes != null ? _excludes.equals(that._excludes) : that._excludes == null);
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = (_includes != null ? _includes.hashCode() : 0);
        result = 31 * result + (_excludes != null ? _excludes.hashCode() : 0);
        return result;
    }

}
