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

import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.*;
import java.io.*;

import static javax.xml.bind.JAXBContext.newInstance;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.echocat.jemoni.carbon.jmx.configuration.RulesConstants.SCHEMA;

public class RulesMarshaller {

    private static final JAXBContext JAXB_CONTEXT;
    private static final Object NAMESPACE_PREFIX_MAPPER;

    static {
        try {
            JAXB_CONTEXT = newInstance(Configuration.class, Rule.class, Format.class, ObjectRule.class, AttributeRule.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not create jaxb context.", e);
        }
        Object namespacePrefixMapper;
        try {
            namespacePrefixMapper = RulesMarshaller.class.getClassLoader().loadClass(RulesMarshaller.class.getPackage().getName() + ".RulesNamespacePrefixMapper").newInstance();
        } catch (Throwable ignored) {
            namespacePrefixMapper = null;
        }
        NAMESPACE_PREFIX_MAPPER = namespacePrefixMapper;
    }

    @Nullable
    public static Configuration unmarshall(@Nullable String content) {
        try {
            return isEmpty(content) ? null :  (Configuration) unmarshallerFor(content).unmarshal(new StringReader(content));
        } catch (JAXBException e) {
            throw new RuntimeException("Could not unmarshall: " + content, e);
        }
    }

    @Nonnull
    public static Configuration unmarshall(@Nonnull Reader reader) throws IOException {
        try {
            return (Configuration) unmarshallerFor(reader).unmarshal(reader);
        } catch (JAXBException e) {
            throw new IOException("Could not unmarshall " + reader + ".", e);
        }
    }

    @Nonnull
    public static Configuration unmarshall(@Nonnull Node rulesElement) {
        try {
            final JAXBElement<Configuration> jaxbElement = rulesElement != null ? unmarshallerFor(rulesElement).unmarshal(rulesElement, Configuration.class) : null;
            return jaxbElement != null ? jaxbElement.getValue() : null;
        } catch (JAXBException e) {
            throw new RuntimeException("Could not unmarshall " + rulesElement + ".", e);
        }
    }

    @Nonnull
    protected static Unmarshaller unmarshallerFor(@Nullable Object element) {
        final Unmarshaller unmarshaller;
        try {
            unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            unmarshaller.setSchema(SCHEMA);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create unmarshaller to unmarshall " + element + ".", e);
        }
        return unmarshaller;
    }

    public static void marshall(@Nonnull Configuration configuration, @Nonnull Writer to) throws IOException {
        try {
            marshallerFor(configuration).marshal(configuration, to);
        } catch (JAXBException e) {
            throw new IOException("Could not marshall " + configuration + " to " + to + ".", e);
        }
    }

    @Nullable
    public static String marshall(@Nullable Configuration configuration) {
        final String result;
        if (configuration != null) {
            final StringWriter to = new StringWriter();
            try {
                marshallerFor(configuration).marshal(configuration, to);
            } catch (JAXBException e) {
                throw new RuntimeException("Could not marshall " + configuration + ".", e);
            }
            result = to.toString();
        } else {
            result = null;
        }
        return result;
    }

    @Nonnull
    private static Marshaller marshallerFor(@Nonnull Configuration configuration) {
        final Marshaller marshaller;
        try {
            marshaller = JAXB_CONTEXT.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            marshaller.setSchema(SCHEMA);
            if (NAMESPACE_PREFIX_MAPPER != null) {
                marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", NAMESPACE_PREFIX_MAPPER);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create marshaller to marshall " + configuration + ".", e);
        }
        return marshaller;
    }

    private RulesMarshaller() {}
}
