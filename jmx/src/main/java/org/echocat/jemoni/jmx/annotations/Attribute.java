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

package org.echocat.jemoni.jmx.annotations;

import org.echocat.jemoni.jmx.AttributeDefinition.AccessMode;
import org.echocat.jemoni.jmx.AttributeAccessor;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.echocat.jemoni.jmx.AttributeDefinition.AccessMode.undefined;

@Retention(RUNTIME)
@Target({METHOD})
@Inherited
public @interface Attribute {

    public String name() default "";

    public String description() default "";

    public AccessMode accessMode() default undefined;

    public Class<? extends AttributeAccessor<?, ?>> accessor() default Null.class;

    public static interface Null extends AttributeAccessor<Object, Object> {}

}
