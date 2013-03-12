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

package org.echocat.jemoni.jmx;

import org.echocat.jemoni.jmx.Registration.Handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.*;
import java.lang.management.ManagementFactory;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class JmxRegistry {

    private static final JmxRegistry LOCAL_INSTANCE = new JmxRegistry();

    @Nonnull
    public static JmxRegistry getLocalInstance() {
        return LOCAL_INSTANCE;
    }

    private final RegistrationHandler _handler = new RegistrationHandler();
    private final MBeanServer _server;

    private BeanFacadeFactory _beanFacadeFactory = new BeanFacadeFactory();

    public JmxRegistry() {
        this(ManagementFactory.getPlatformMBeanServer());
    }

    public JmxRegistry(MBeanServer server) {
        _server = server;
    }

    @Nonnull
    public MBeanServer getServer() {
        return _server;
    }

    @Nonnull
    public BeanFacadeFactory getBeanFacadeFactory() {
        return _beanFacadeFactory;
    }

    public void setBeanFacadeFactory(@Nonnull BeanFacadeFactory beanFacadeFactory) {
        // noinspection ObjectEquality
        if (this == LOCAL_INSTANCE) {
            throw new IllegalStateException("Modification of the local instance of " + JmxRegistry.class.getName() + " is not supported.");
        }
        _beanFacadeFactory = beanFacadeFactory;
    }

    @Nonnull
    public Registration register(@Nonnull DynamicMBean bean, @Nonnull ObjectName withName) throws InstanceAlreadyExistsException {
        try {
            _server.registerMBean(bean, withName);
        } catch (MBeanRegistrationException | NotCompliantMBeanException e) {
            throw new RuntimeException("Could not register " + bean + " at " + _server + ".", e);
        }
        return new Registration(withName, bean, _handler);
    }

    @Nonnull
    public Registration register(@Nonnull DynamicMBean bean, @Nonnull Class<?> forType) {
        return register(bean, forType, null);
    }

    @Nonnull
    public Registration register(@Nonnull DynamicMBean bean, @Nonnull Class<?> forType, @Nullable String variant) {
        Registration result = null;
        long instance = 0;
        do {
            try {
                final ObjectName objectName = getObjectNameFor(forType, instance > 0 ? Long.toString(instance) : null, variant);
                result = register(bean, objectName);
            } catch (InstanceAlreadyExistsException ignored) {
                instance++;
            }
        } while (result == null);
        return result;
    }

    @Nonnull
    public <B> RegistrationWithFacade<B> register(@Nonnull B bean, @Nonnull ObjectName withName) throws InstanceAlreadyExistsException {
        final BeanFacade<B> facade = _beanFacadeFactory.createFor(bean);
        register(facade, withName);
        return new RegistrationWithFacade<>(withName, facade, _handler);
    }

    @Nonnull
    public <B> RegistrationWithFacade<B> register(@Nonnull B bean, @Nullable String variant) {
        RegistrationWithFacade<B> result = null;
        long instance = 0;
        do {
            try {
                final ObjectName objectName = getObjectNameFor(bean.getClass(), instance > 0 ? Long.toString(instance) : null, variant);
                result = register(bean, objectName);
            } catch (InstanceAlreadyExistsException ignored) {
                instance++;
            }
        } while (result == null);
        return result;
    }

    @Nonnull
    public <B> RegistrationWithFacade<B> register(@Nonnull B bean) {
        return register(bean, (String) null);
    }

    @Nonnull
    public ObjectName getObjectNameFor(@Nonnull Class<?> type, @Nullable String instance, @Nullable String variant) {
        final StringBuilder sb = new StringBuilder();
        final Package aPackage = type.getPackage();
        final String packageName = aPackage != null ? aPackage.getName() : null;
        final String typeName = type.getName();
        if (!isEmpty(packageName)) {
            sb.append(packageName);
            sb.append(!isEmpty(variant) ? ":type=" : ":name=");
            sb.append(typeName.substring(packageName.length() + 1));
        } else {
            sb.append("_");
            sb.append(!isEmpty(variant) ? ":type=" : ":name=");
            sb.append(typeName);
        }
        if (!isEmpty(instance)) {
            sb.append('.').append(normalize(instance));
        }
        if (!isEmpty(variant)) {
            sb.append(",name=").append(normalize(variant));
        }
        try {
            return new ObjectName(sb.toString());
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Could not create a valid object name for " + typeName + " and variant '" + variant + "'.", e);
        }
    }

    @Nonnull
    public <T> T getMBeanProxy(@Nonnull String mbeanName, @Nonnull Class<T> mbeanInterface) {
        try {
            return getMBeanProxy(new ObjectName(mbeanName), mbeanInterface);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Could not create object name for " + mbeanName + ".", e);
        }
    }

    @Nonnull
    public <T> T getMBeanProxy(@Nonnull ObjectName objectName, @Nonnull Class<T> mbeanInterface) {
        try {
            final boolean emitter = getServer().isInstanceOf(objectName, NotificationEmitter.class.getName());
            // create an MXBean proxy
            return JMX.newMXBeanProxy(getServer(), objectName, mbeanInterface, emitter);
        } catch (Exception e) {
            throw new RuntimeException("Could not create mbean proxy for " + objectName + ".", e);
        }
    }

    @Nonnull
    public <T> T getMBeanProxy(@Nonnull Class<T> mbeanInterface, @Nonnull String instance) {
        final ObjectName objectName = getObjectNameFor(mbeanInterface, instance, null);
        return getMBeanProxy(objectName, mbeanInterface);
    }

    @Nonnull
    protected String normalize(@Nonnull String what) {
        final char[] in = what.toCharArray();
        final char[] out = new char[in.length];
        int i = 0;
        for (char c : in) {
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.') {
                out[i++] = c;
            } else if (Character.isWhitespace(c)) {
                out[i++] = '_';
            }
        }
        return new String(out, 0, i);
    }

    public class RegistrationHandler implements Handler {

        @Override
        public void unregister(@Nonnull Registration registration) throws Exception {
            try {
                _server.unregisterMBean(registration.getObjectName());
            } catch (InstanceNotFoundException ignored) {}
        }

    }

}
