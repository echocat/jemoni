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

package org.echocat.jemoni.carbon.jmx.rules;

import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.*;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import static javax.xml.bind.JAXBContext.newInstance;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static org.echocat.jemoni.carbon.jmx.rules.RulesConstants.SCHEMA;

public class RulesMarshaller {

    private static final JAXBContext JAXB_CONTEXT;
    private static final Object NAMESPACE_PREFIX_MAPPER;

    static {
        try {
            JAXB_CONTEXT = newInstance(Rules.class, Rule.class, ObjectPatternRule.class, AttributePatternRule.class);
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

    @Nonnull
    public static Rules unmarshall(@Nonnull Reader reader) throws IOException {
        final Unmarshaller unmarshaller = createUnmarshallerFor(reader);
        try {
            return (Rules) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new IOException("Could not unmarshall " + reader + ".", e);
        }
    }

    @Nonnull
    public static Rules unmarshall(@Nonnull Node rulesElement) {
        final Unmarshaller unmarshaller = createUnmarshallerFor(rulesElement);
        try {
            final JAXBElement<Rules> jaxbElement = rulesElement != null ? unmarshaller.unmarshal(rulesElement, Rules.class) : null;
            return jaxbElement != null ? jaxbElement.getValue() : null;
        } catch (JAXBException e) {
            throw new RuntimeException("Could not unmarshall " + rulesElement + ".", e);
        }
    }

    @Nonnull
    protected static Unmarshaller createUnmarshallerFor(@Nullable Object element) {
        final Unmarshaller unmarshaller;
        try {
            unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            unmarshaller.setSchema(SCHEMA);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create unmarshaller to unmarshall " + element + ".", e);
        }
        return unmarshaller;
    }

    public static void marshall(@Nonnull Rules rules, @Nonnull Writer to) throws IOException {
        final Marshaller marshaller;
        try {
            marshaller = JAXB_CONTEXT.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            marshaller.setSchema(SCHEMA);
            if (NAMESPACE_PREFIX_MAPPER != null) {
                marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", NAMESPACE_PREFIX_MAPPER);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create marshaller to marshall " + rules + ".", e);
        }
        try {
            marshaller.marshal(rules, to);
        } catch (Exception e) {
            throw new IOException("Could not marshall " + rules + " to " + to + ".", e);
        }
    }

    private RulesMarshaller() {}
}
