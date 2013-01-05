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

@XmlSchema(
    namespace = SCHEMA_NAMESPACE,
    location = SCHEMA_NAMESPACE,
    xmlns = {},
    elementFormDefault = QUALIFIED,
    attributeFormDefault = UNQUALIFIED
)

package org.echocat.jemoni.carbon.jmx.configuration;

import javax.xml.bind.annotation.XmlSchema;

import static javax.xml.bind.annotation.XmlNsForm.QUALIFIED;
import static javax.xml.bind.annotation.XmlNsForm.UNQUALIFIED;
import static org.echocat.jemoni.carbon.jmx.configuration.RulesConstants.SCHEMA_NAMESPACE;
