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

package org.echocat.jemoni.carbon;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.System.currentTimeMillis;

public class MeasurePoint {

    private static final String TIME_PATTERN = "HH:mm:ss";

    private final String _path;
    private final Date _timestamp;
    private final Number _value;

    public MeasurePoint(@Nonnull String path, @Nonnull Number value) {
        this(path, new Date(currentTimeMillis() / 1000 * 1000), value);
    }

    public MeasurePoint(@Nonnull String path, @Nonnull Date timestamp, @Nonnull Number value) {
        _path = path;
        _timestamp = timestamp;
        _value = value;
    }

    @Nonnull
    public String getPath() {
        return _path;
    }

    @Nonnull
    public Date getTimestamp() {
        return _timestamp;
    }

    @Nonnull
    public Number getValue() {
        return _value;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof MeasurePoint)) {
            result = false;
        } else {
            final MeasurePoint that = (MeasurePoint) o;
            result = getPath().equals(that.getPath()) && getTimestamp().equals(that.getTimestamp()) && getValue().equals(that.getValue());
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = getPath().hashCode();
        result = 31 * result + getTimestamp().hashCode();
        result = 31 * result + getValue().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getPath() + "(" + new SimpleDateFormat(TIME_PATTERN).format(getTimestamp()) + "): " + getValue();
    }
}
