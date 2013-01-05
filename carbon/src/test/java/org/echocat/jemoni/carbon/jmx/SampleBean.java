/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat JeMoni, Copyright (c) 2012-2013 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jemoni.carbon.jmx;

import org.echocat.jemoni.jmx.annotations.Attribute;
import org.echocat.jemoni.jmx.annotations.Bean;

@Bean(description = "This is a sample bean")
public class SampleBean {

    @Attribute
    public String getString() {
        return "aString";
    }

    public int getExcludedOne() {
        return 666;
    }

    @Attribute
    public boolean getSimpleBoolean() {
        return false;
    }

    @Attribute
    public Boolean getBoolean() {
        return true;
    }

    @Attribute
    public char getSimpleCharacter() {
        return 2;
    }

    @Attribute
    public Character getCharacter() {
        return 3;
    }

    @Attribute
    public byte getSimpleByte() {
        return 4;
    }

    @Attribute
    public Byte getByte() {
        return 5;
    }

    @Attribute
    public short getSimpleShort() {
        return 6;
    }

    @Attribute
    public Short getShort() {
        return 7;
    }

    @Attribute
    public int getSimpleInteger() {
        return 8;
    }

    @Attribute
    public Integer getInteger() {
        return 9;
    }

    @Attribute
    public long getSimpleLong() {
        return 10;
    }

    @Attribute
    public Long getLong() {
        return 11L;
    }

    @Attribute
    public float getSimpleFloat() {
        return 12;
    }

    @Attribute
    public Float getFloat() {
        return 13F;
    }

    @Attribute
    public double getSimpleDouble() {
        return 14;
    }

    @Attribute
    public Double getDouble() {
        return 15D;
    }

}
