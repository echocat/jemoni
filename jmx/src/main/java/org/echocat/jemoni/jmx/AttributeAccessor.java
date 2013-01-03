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

package org.echocat.jemoni.jmx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface AttributeAccessor<T, B> {

    @Nonnull
    public Class<T> getType();

    @Nullable
    public T get(@Nonnull B bean) throws Exception;

    @Nullable
    public void set(@Nonnull B bean, @Nullable T value) throws Exception;

}
