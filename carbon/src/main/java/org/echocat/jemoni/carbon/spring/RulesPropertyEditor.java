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

package org.echocat.jemoni.carbon.spring;

import org.echocat.jemoni.carbon.jmx.configuration.Configuration;

import javax.annotation.Nullable;
import java.beans.PropertyEditorSupport;

import static org.echocat.jemoni.carbon.jmx.configuration.RulesMarshaller.marshall;
import static org.echocat.jemoni.carbon.jmx.configuration.RulesMarshaller.unmarshall;

public class RulesPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(@Nullable String text) throws IllegalArgumentException {
        setValue(unmarshall(text));
    }

    @Override
    @Nullable
    public String getAsText() {
        final Object value = getValue();
        return value instanceof Configuration ? marshall((Configuration) value) : null;
    }

}
