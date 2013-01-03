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

package org.echocat.jemoni.jmx.sample;

import org.echocat.jemoni.jmx.annotations.Argument;
import org.echocat.jemoni.jmx.annotations.Attribute;
import org.echocat.jemoni.jmx.annotations.Bean;
import org.echocat.jemoni.jmx.annotations.Operation;

import static org.echocat.jemoni.jmx.AttributeDefinition.AccessMode.writeOnly;

@Bean(description = "testBean1!")
public class TestBean1 {

    private final int _roInteger = 666;
    private String _rwString;
    private long _woLong;
    private boolean _boolean;
    private boolean _hidden;

    @Attribute(name = "theRwString", description = "something here")
    public String getRwString() {
        return _rwString;
    }

    public void setRwString(String rwString) {
        _rwString = rwString;
    }

    @Attribute
    public int getRoInteger() {
        return _roInteger;
    }

    @Operation(name = "woLongHere", description = "say it loud")
    public long getWoLong() {
        return _woLong;
    }

    @Attribute(name = "foo", accessMode = writeOnly)
    public void setWoLong(long woLong) {
        _woLong = woLong;
    }

    @Attribute
    public boolean isBoolean() {
        return _boolean;
    }

    public void setBoolean(boolean aBoolean) {
        _boolean = aBoolean;
    }

    @SuppressWarnings("UnusedParameters")
    @Operation
    public void anotherSetOfWoLong(@Argument(description = "wohoo") long value, @Argument(name = "b") boolean ignored) {
        _woLong = value;
    }

    public boolean isHidden() {
        return _hidden;
    }

    public void setHidden(boolean hidden) {
        _hidden = hidden;
    }
}
