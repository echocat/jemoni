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

package org.echocat.jemoni.carbon.jmx.configuration;

import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class RulesConstants {

    public static final String SCHEMA_NAMESPACE = "https://jemoni.echocat.org/schemas/jmx2carbonRules.xsd";
    public static final String SCHEMA_XSD_LOCATION = "org/echocat/jemoni/carbon/jmx/configuration/jmx2carbonRules-1.0.xsd";
    public static final Schema SCHEMA;

    static {
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
        final InputStream is = RulesConstants.class.getClassLoader().getResourceAsStream(SCHEMA_XSD_LOCATION);
        if (is == null){
            throw new IllegalStateException("There is no '" + SCHEMA_XSD_LOCATION + "' in classpath.");
        }
        try {
            SCHEMA = schemaFactory.newSchema(new StreamSource(is));
        } catch (SAXException e) {
            throw new RuntimeException("Could not load '" + SCHEMA_XSD_LOCATION + "'.", e);
        } finally {
            closeQuietly(is);
        }
    }

    private RulesConstants() {}
}
