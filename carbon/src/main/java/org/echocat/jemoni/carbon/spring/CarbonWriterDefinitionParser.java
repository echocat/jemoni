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

import org.echocat.jemoni.carbon.CarbonWriter;
import org.echocat.jomon.runtime.util.Duration;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import static org.echocat.jemoni.carbon.CarbonWriter.DEFAULT_CHARSET;
import static org.echocat.jemoni.carbon.CarbonWriter.DEFAULT_MAX_BUFFER_LIFETIME;
import static org.springframework.util.StringUtils.hasText;

public class CarbonWriterDefinitionParser extends AbstractSingleBeanDefinitionParser {

    public static final String ADDRESS_ATTRIBUTE = "address";
    public static final String MAX_BUFFER_LIFETIME_ATTRIBUTE = "maxBufferLifetime";
    public static final String CHARSET_ATTRIBUTE = "charset";

    @Override
    protected Class<?> getBeanClass(Element element) {
        return CarbonWriter.class;
    }

    @Override
    protected void doParse(@Nonnull Element element, @Nonnull BeanDefinitionBuilder bean) {
        bean.addPropertyValue("address", toSocketAddress(element.getAttribute(ADDRESS_ATTRIBUTE)));
        bean.addPropertyValue("maxBufferLifetime", toMaxBufferLifetime(element.getAttribute(MAX_BUFFER_LIFETIME_ATTRIBUTE)));
        bean.addPropertyValue("charset", toCharset(element.getAttribute(CHARSET_ATTRIBUTE)));
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
        final String id = super.resolveId(element, definition, parserContext);
        return hasText(id) ? id : getBeanClass(element).getName();
    }

    @Nonnull
    protected InetSocketAddress toSocketAddress(@Nullable String plain) {
        final InetSocketAddress address;
        if (plain != null) {
            final String trimmedText = plain.trim();
            if (!trimmedText.isEmpty()) {
                final int lastDoubleDot = trimmedText.lastIndexOf(':');
                if (lastDoubleDot > 0 && lastDoubleDot + 1 < trimmedText.length()) {
                    final String host = trimmedText.substring(0, lastDoubleDot).trim();
                    final String plainPort = trimmedText.substring(lastDoubleDot + 1).trim();
                    final int port;
                    try {
                        port = Integer.parseInt(plainPort);
                    } catch (NumberFormatException ignored) {
                        throw new IllegalArgumentException("Illegal port: " + plainPort);
                    }
                    address = new InetSocketAddress(host, port);
                } else {
                    throw new IllegalArgumentException("Port missing");
                }
            } else {
                address = null;
            }
        } else {
            address = null;
        }
        if (address == null) {
            throw new IllegalArgumentException("Could not interpret '" + plain + "' as network socket address.");
        }
        return address;
    }

    @Nonnull
    protected Duration toMaxBufferLifetime(@Nullable String plain) {
        return hasText(plain) ? new Duration(plain) : DEFAULT_MAX_BUFFER_LIFETIME;
    }

    @Nonnull
    protected Charset toCharset(@Nullable String plain) {
        return hasText(plain) ? Charset.forName(plain) : DEFAULT_CHARSET;
    }
}
