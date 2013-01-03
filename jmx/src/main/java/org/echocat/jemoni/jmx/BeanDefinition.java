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
import java.util.Set;

public interface BeanDefinition<B> {

    @Nullable
    public String getDescription();

    @Nonnull
    public Set<OperationDefinition<B>> getOperationDefinitions();

    @Nonnull
    public Set<AttributeDefinition<?, B>> getAttributeDefinitions();

}
