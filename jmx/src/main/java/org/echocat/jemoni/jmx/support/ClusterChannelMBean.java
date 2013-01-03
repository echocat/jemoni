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

package org.echocat.jemoni.jmx.support;

import org.echocat.jemoni.jmx.JmxRegistry;
import org.echocat.jemoni.jmx.Registration;
import org.echocat.jomon.net.cluster.channel.*;
import org.echocat.jomon.net.cluster.channel.multicast.MulticastClusterChannel;
import org.echocat.jomon.net.cluster.channel.tcp.TcpClusterChannel;
import org.echocat.jomon.runtime.jaxb.InetSocketAddressPropertyEditor;
import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static javax.management.MBeanOperationInfo.ACTION;
import static org.echocat.jomon.net.cluster.channel.ClusterChannelUtils.formatNodesStatusOf;

public class ClusterChannelMBean implements DynamicMBean, AutoCloseable {

    private final ClusterChannel<?, ?> _channel;
    private final Registration _registration;

    public ClusterChannelMBean(@Nonnull ClusterChannel<?, ?> clusterChannel, @Nonnull JmxRegistry registry) {
        _channel = clusterChannel;
        _registration = registry.register(this, ClusterChannel.class, clusterChannel.getName());
    }

    @Override
    public void close() throws Exception {
        _registration.close();
    }

    @Nonnull
    public ClusterChannel<?, ?> getChannel() {
        return _channel;
    }

    @SuppressWarnings("OverlyLongMethod")
    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        final Object result;
        if ("name".equals(attribute)) {
            result = _channel.getName();
        } else if ("id".equals(attribute)) {
            result = _channel.getId().toString();
        } else if ("uuid".equals(attribute)) {
            result = _channel.getUuid().toString();
        } else if ("service".equals(attribute)) {
            result = castForAttribute(ServiceEnabledClusterChannel.class).getService();
        } else if ("pingInterval".equals(attribute)) {
            result = castForAttribute(PingEnabledClusterChannel.class).getPingInterval().toString();
        } else if ("pingIntervalToTimeoutRatio".equals(attribute)) {
            result = castForAttribute(MulticastClusterChannel.class).getPingIntervalToTimeoutRatio();
        } else if ("lastPingSend".equals(attribute)) {
            result = castForAttribute(PingEnabledClusterChannel.class).getLastPingSendAt();
        } else if ("messagesReceivedPerSecond".equals(attribute)) {
            result = castForAttribute(StatisticEnabledClusterChannel.class).getMessagesReceivedPerSecond();
        } else if ("messagesSendPerSecond".equals(attribute)) {
            result = castForAttribute(StatisticEnabledClusterChannel.class).getMessagesSendPerSecond();
        } else if ("messagesReceived".equals(attribute)) {
            result = castForAttribute(StatisticEnabledClusterChannel.class).getMessagesReceived();
        } else if ("messagesSend".equals(attribute)) {
            result = castForAttribute(StatisticEnabledClusterChannel.class).getMessagesSend();
        } else if ("lastMessageReceived".equals(attribute)) {
            result = castForAttribute(StatisticEnabledClusterChannel.class).getLastMessageReceived();
        } else if ("lastMessageSend".equals(attribute)) {
            result = castForAttribute(StatisticEnabledClusterChannel.class).getLastMessageSend();
        } else if ("nodesStatus".equals(attribute)) {
            result = formatNodesStatusOf(_channel);
        } else if ("ttl".equals(attribute)) {
            result = castForAttribute(MulticastClusterChannel.class).getTtl().toString();
        } else if ("soTimeout".equals(attribute)) {
            result = castForAttribute(NetBasedClusterChannel.class).getSoTimeout().toString();
        } else if ("connectionTimeout".equals(attribute)) {
            result = castForAttribute(TcpClusterChannel.class).getConnectionTimeout().toString();
        } else if ("address".equals(attribute)) {
            final InetSocketAddress address = castForAttribute(AddressEnabledClusterChannel.class).getAddress();
            result = address != null ? address.getHostString() + ":" + address.getPort() : null;
        } else if ("remoteAddresses".equals(attribute)) {
            result = castForAttribute(RemoteAddressesEnabledClusterChannel.class).getRemoteAddressesAsString();
        } else if ("sendingQueueCapacity".equals(attribute)) {
            result = castForAttribute(SendingQueueEnabledClusterChannel.class).getSendingQueueCapacity();
        } else if ("sendingQueueSize".equals(attribute)) {
            result = castForAttribute(SendingQueueEnabledClusterChannel.class).getSendingQueueSize();
        } else if ("blocking".equals(attribute)) {
            result = castForAttribute(BlockableClusterChannel.class).isBlocking();
        } else if ("dropMessagesIfQueueIsFull".equals(attribute)) {
            result = castForAttribute(DropMessagesEnabledClusterChannel.class).isDropMessagesIfQueueIsFull();
        } else {
            throw new AttributeNotFoundException();
        }
        return result;
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        final String attributeName = attribute.getName();
        final Object value = attribute.getValue();
        if ("name".equals(attributeName)) {
            _channel.setName(cast(String.class, value));
        } else if ("service".equals(attributeName)) {
            castForAttribute(ServiceEnabledClusterChannel.class).setService(cast(String.class, value));
        } else if ("pingInterval".equals(attributeName)) {
            castForAttribute(PingEnabledClusterChannel.class).setPingInterval(new Duration(cast(String.class, value)));
        } else if ("pingIntervalToTimeoutRatio".equals(attributeName)) {
            castForAttribute(MulticastClusterChannel.class).setPingIntervalToTimeoutRatio(cast(Number.class, value).doubleValue());
        } else if ("ttl".equals(attributeName)) {
            castForAttribute(MulticastClusterChannel.class).setTtl(new Duration(cast(String.class, value)));
        } else if ("soTimeout".equals(attributeName)) {
            castForAttribute(NetBasedClusterChannel.class).setSoTimeout(new Duration(cast(String.class, value)));
        } else if ("connectionTimeout".equals(attributeName)) {
            castForAttribute(TcpClusterChannel.class).setConnectionTimeout(new Duration(cast(String.class, value)));
        } else if ("sendingQueueCapacity".equals(attributeName)) {
            castForAttribute(SendingQueueEnabledClusterChannel.class).setSendingQueueCapacity(cast(Number.class, value).intValue());
        } else if ("blocking".equals(attributeName)) {
            castForAttribute(BlockableClusterChannel.class).setBlocking(TRUE.equals(cast(Boolean.class, value)));
        } else if ("dropMessagesIfQueueIsFull".equals(attributeName)) {
            castForAttribute(DropMessagesEnabledClusterChannel.class).setDropMessagesIfQueueIsFull(TRUE.equals(cast(Boolean.class, value)));
        } else if ("address".equals(attributeName)) {
            if (value != null) {
                final InetSocketAddressPropertyEditor editor = new InetSocketAddressPropertyEditor();
                editor.setAsText(cast(String.class, value));
                castForAttribute(AddressEnabledClusterChannel.class).setAddress((InetSocketAddress) editor.getValue());
            } else {
                castForAttribute(AddressEnabledClusterChannel.class).setAddress(null);
            }
        } else if ("remoteAddresses".equals(attributeName)) {
            castForAttribute(RemoteAddressesEnabledClusterChannel.class).setRemoteAddressesAsString(cast(String.class, value));
        } else {
            throw new AttributeNotFoundException();
        }
    }

    private <T> T cast(@Nonnull Class<T> type, @Nullable Object value) throws InvalidAttributeValueException {
        if (!type.isInstance(value)) {
            throw new InvalidAttributeValueException();
        }
        return type.cast(value);
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        final Object result;
        if ("ping".equals(actionName)) {
            castForOperation(PingEnabledClusterChannel.class).ping();
            result = null;
        } else if ("getNodeStatus".equals(actionName)) {
            result = formatNodesStatusOf(_channel);
        } else {
            throw new UnsupportedOperationException();
        }
        return result;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(_channel.getClass().getName(), null, getAttributesArray(), null, getOperationsArray(), null);
    }

    @Nonnull
    protected MBeanAttributeInfo[] getAttributesArray() {
        final List<MBeanAttributeInfo> result = getAttributes();
        return result.toArray(new MBeanAttributeInfo[result.size()]);
    }

    @Nonnull
    protected List<MBeanAttributeInfo> getAttributes() {
        final List<MBeanAttributeInfo> result = new ArrayList<>();
        result.add(new MBeanAttributeInfo("id", String.class.getName(), null, true, false, false));
        result.add(new MBeanAttributeInfo("uuid", String.class.getName(), null, true, false, false));
        result.add(new MBeanAttributeInfo("name", String.class.getName(), null, true, true, false));
        result.add(new MBeanAttributeInfo("nodesStatus", String.class.getName(), null, true, false, false));
        if (_channel instanceof PingEnabledClusterChannel) {
            result.add(new MBeanAttributeInfo("pingInterval", String.class.getName(), null, true, true, false));
            result.add(new MBeanAttributeInfo("lastPingSend", Date.class.getName(), null, true, false, false));
        }
        if (_channel instanceof StatisticEnabledClusterChannel) {
            result.add(new MBeanAttributeInfo("messagesReceivedPerSecond", Double.class.getName(), null, true, false, false));
            result.add(new MBeanAttributeInfo("messagesSendPerSecond", Double.class.getName(), null, true, false, false));
            result.add(new MBeanAttributeInfo("messagesReceived", Long.class.getName(), null, true, false, false));
            result.add(new MBeanAttributeInfo("messagesSend", Long.class.getName(), null, true, false, false));
        }
        if (_channel instanceof AddressEnabledClusterChannel) {
            result.add(new MBeanAttributeInfo("address", String.class.getName(), null, true, true, false));
        }
        if (_channel instanceof RemoteAddressesEnabledClusterChannel) {
            result.add(new MBeanAttributeInfo("remoteAddresses", String.class.getName(), null, true, true, false));
        }
        if (_channel instanceof ServiceEnabledClusterChannel) {
            result.add(new MBeanAttributeInfo("service", String.class.getName(), null, true, true, false));
        }
        if (_channel instanceof BlockableClusterChannel) {
            result.add(new MBeanAttributeInfo("blocking", Boolean.class.getName(), null, true, true, false));
        }
        if (_channel instanceof DropMessagesEnabledClusterChannel) {
            result.add(new MBeanAttributeInfo("dropMessagesIfQueueIsFull", Boolean.class.getName(), null, true, true, false));
        }
        if (_channel instanceof SendingQueueEnabledClusterChannel) {
            result.add(new MBeanAttributeInfo("sendingQueueCapacity", Integer.class.getName(), null, true, true, false));
            result.add(new MBeanAttributeInfo("sendingQueueSize", Integer.class.getName(), null, true, false, false));
        }
        if (_channel instanceof NetBasedClusterChannel) {
            result.add(new MBeanAttributeInfo("soTimeout", String.class.getName(), null, true, true, false));
        }
        if (_channel instanceof TcpClusterChannel) {
            result.add(new MBeanAttributeInfo("connectionTimeout", String.class.getName(), null, true, true, false));
        }
        if (_channel instanceof MulticastClusterChannel) {
            result.add(new MBeanAttributeInfo("pingIntervalToTimeoutRatio", Double.class.getName(), null, true, true, false));
            result.add(new MBeanAttributeInfo("ttl", String.class.getName(), null, true, true, false));
        }
        return result;
    }

    @Nonnull
    protected MBeanOperationInfo[] getOperationsArray() {
        final List<MBeanOperationInfo> result = getOperations();
        return result.toArray(new MBeanOperationInfo[result.size()]);
    }

    @Nonnull
    protected List<MBeanOperationInfo> getOperations() {
        final List<MBeanOperationInfo> result = new ArrayList<>();
        if (_channel instanceof PingEnabledClusterChannel) {
            result.add(new MBeanOperationInfo("ping", null, new MBeanParameterInfo[0], Void.TYPE.getName(), ACTION));
        }
        result.add(new MBeanOperationInfo("getNodeStatus", null, new MBeanParameterInfo[0], String.class.getName(), ACTION));
        return result;
    }

    @Nonnull
    protected <T extends ClusterChannel<?, ?>> T castForAttribute(@Nonnull Class<T> type) throws AttributeNotFoundException {
        if (type.isInstance(_channel)) {
            return type.cast(_channel);
        } else {
            throw new AttributeNotFoundException();
        }
    }

    @Nonnull
    protected <T extends ClusterChannel<?, ?>> T castForOperation(@Nonnull Class<T> type) throws MBeanException {
        if (type.isInstance(_channel)) {
            return type.cast(_channel);
        } else {
            throw new MBeanException(null, "Illegal method.");
        }
    }

    @Override public AttributeList getAttributes(String[] attributes) { throw new UnsupportedOperationException(); }
    @Override public AttributeList setAttributes(AttributeList attributes) { throw new UnsupportedOperationException(); }


}
