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

package org.echocat.jemoni.jmx.reflection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReflectionBasedUtils {

    private ReflectionBasedUtils() {}

    @Nonnull
    public static Class<?> correctTypeIfNeeded(@Nonnull Class<?> in) {
        final Class<?> out;
        if (in.isPrimitive()) {
            if (Boolean.TYPE.equals(in)) {
                out = Boolean.class;
            } else if (Byte.TYPE.equals(in)) {
                out = Byte.class;
            } else if (Character.TYPE.equals(in)) {
                out = Character.class;
            } else if (Short.TYPE.equals(in)) {
                out = Short.class;
            } else if (Integer.TYPE.equals(in)) {
                out = Integer.class;
            } else if (Long.TYPE.equals(in)) {
                out = Long.class;
            } else if (Float.TYPE.equals(in)) {
                out = Float.class;
            } else if (Double.TYPE.equals(in)) {
                out = Double.class;
            } else if (Void.TYPE.equals(in)) {
                out = Void.class;
            } else {
                throw new IllegalStateException("Unrecognized primitive type: " + in);
            }
        } else {
            out = in;
        }
        return out;
    }

    @Nullable
    public static Object correctValueIfNeeded(@Nonnull Class<?> requiredType, @Nullable Object in) {
        final Object out;
        if (in == null) {
            out = null;
        } else if (Character.class.equals(requiredType) && in instanceof Number) {
            out = (char)((Number)in).intValue();
        } else if (Boolean.class.equals(requiredType) && in instanceof Number) {
            out = ((Number)in).longValue() > 0;
        } else if (Byte.class.equals(requiredType) && in instanceof Number) {
            out = (byte) ((Number)in).intValue();
        } else if (Short.class.equals(requiredType) && in instanceof Number) {
            out = ((Number)in).shortValue();
        } else if (Integer.class.equals(requiredType) && in instanceof Number) {
            out = ((Number)in).intValue();
        } else if (Long.class.equals(requiredType) && in instanceof Number) {
            out = ((Number)in).longValue();
        } else if (Float.class.equals(requiredType) && in instanceof Number) {
            out = ((Number)in).floatValue();
        } else if (Double.class.equals(requiredType) && in instanceof Number) {
            out = ((Number)in).doubleValue();
        } else {
            out = in;
        }
        return out;
    }
}
