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

import org.echocat.jemoni.jmx.sample.TestBean1;
import org.junit.Test;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JmxRegistryUnitTest {

    @Test
    public void testRegister() throws Exception {
        final JmxRegistry registry = new JmxRegistry();

        final TestBean1 testBean1 = new TestBean1();
        try (final RegistrationWithFacade<TestBean1> registration = registry.register(testBean1)) {

            final MBeanInfo info = registry.getServer().getMBeanInfo(registration.getObjectName());
            check(info.getAttributes());
            check(info.getOperations());
            assertThat(info.getNotifications().length, is(0));
            assertThat(info.getConstructors().length, is(0));
        }
    }

    protected void check(MBeanAttributeInfo[] attributes) {
        assertThat(attributes.length, is(4));

        assertThat(attributes[0].getName(), is("boolean"));
        assertThat(attributes[0].getDescription(), is((Object)null));
        assertThat(attributes[0].getType(), is(Boolean.class.getName()));
        assertThat(attributes[0].isReadable(), is(true));
        assertThat(attributes[0].isWritable(), is(true));

        assertThat(attributes[1].getName(), is("foo"));
        assertThat(attributes[1].getDescription(), is((Object)null));
        assertThat(attributes[1].getType(), is(Long.class.getName()));
        assertThat(attributes[1].isReadable(), is(false));
        assertThat(attributes[1].isWritable(), is(true));

        assertThat(attributes[2].getName(), is("roInteger"));
        assertThat(attributes[2].getDescription(), is((Object)null));
        assertThat(attributes[2].getType(), is(Integer.class.getName()));
        assertThat(attributes[2].isReadable(), is(true));
        assertThat(attributes[2].isWritable(), is(false));

        assertThat(attributes[3].getName(), is("theRwString"));
        assertThat(attributes[3].getDescription(), is("something here"));
        assertThat(attributes[3].getType(), is(String.class.getName()));
        assertThat(attributes[3].isReadable(), is(true));
        assertThat(attributes[3].isWritable(), is(true));
    }

    protected void check(MBeanOperationInfo[] operations) {
        assertThat(operations.length, is(2));

        assertThat(operations[0].getName(), is("anotherSetOfWoLong"));
        assertThat(operations[0].getDescription(), is((Object)null));
        assertThat(operations[0].getReturnType(), is("void"));
        final MBeanParameterInfo[] signature0 = operations[0].getSignature();
        assertThat(signature0.length, is(2));
        assertThat(signature0[0].getName(), is("argument0"));
        assertThat(signature0[0].getDescription(), is("wohoo"));
        assertThat(signature0[0].getType(), is(Long.class.getName()));
        assertThat(signature0[1].getName(), is("b"));
        assertThat(signature0[1].getDescription(), is((Object)null));
        assertThat(signature0[1].getType(), is(Boolean.class.getName()));

        assertThat(operations[1].getName(), is("woLongHere"));
        assertThat(operations[1].getDescription(), is("say it loud"));
        assertThat(operations[1].getReturnType(), is(Long.class.getName()));
        assertThat(operations[1].getSignature().length, is(0));
    }

}
