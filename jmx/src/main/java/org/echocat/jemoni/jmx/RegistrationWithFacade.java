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
import javax.management.ObjectName;

public class RegistrationWithFacade<B> extends Registration {

    private final BeanFacade<B> _facade;

    public RegistrationWithFacade(@Nonnull ObjectName objectName, @Nonnull BeanFacade<B> facade, @Nonnull Handler handler) {
        super(objectName, facade, handler);
        _facade = facade;
    }

    @Nonnull
    public BeanFacade<B> getFacade() {
        return _facade;
    }

}
