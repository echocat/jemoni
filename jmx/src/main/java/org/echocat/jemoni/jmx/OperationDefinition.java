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

public interface OperationDefinition<B> extends MemberDefinition {

    @Nonnull
    public Class<?> getReturnType();

    @Nonnull
    public ArgumentDefinition[] getArgumentDefinitions();

    @Nullable
    public Object invoke(@Nonnull B bean, @Nonnull Object[] arguments) throws Exception;

}
