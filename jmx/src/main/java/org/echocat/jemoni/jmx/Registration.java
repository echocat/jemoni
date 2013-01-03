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
import javax.management.DynamicMBean;
import javax.management.ObjectName;

public class Registration implements AutoCloseable {

    private final ObjectName _objectName;
    private final DynamicMBean _dynamicMBean;
    private final Handler _handler;

    public Registration(@Nonnull ObjectName objectName, @Nonnull DynamicMBean dynamicMBean, @Nonnull Handler handler) {
        _objectName = objectName;
        _dynamicMBean = dynamicMBean;
        _handler = handler;
    }

    @Nonnull
    public ObjectName getObjectName() {
        return _objectName;
    }

    @Nonnull
    public DynamicMBean getDynamicMBean() {
        return _dynamicMBean;
    }

    @Override
    public void close() throws Exception {
        _handler.unregister(this);
    }

    public interface Handler {

        public void unregister(@Nonnull Registration registration) throws Exception;

    }
}
