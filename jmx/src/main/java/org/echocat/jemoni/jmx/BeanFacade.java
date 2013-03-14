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

import org.echocat.jemoni.jmx.AttributeDefinition.AccessMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static javax.management.MBeanOperationInfo.ACTION;
import static org.echocat.jemoni.jmx.reflection.ReflectionBasedUtils.correctValueIfNeeded;

public class BeanFacade<B> implements DynamicMBean {

    private final B _bean;
    private final BeanDefinition<B> _beanDefinition;
    private final Map<String, OperationDefinition<B>> _nameToOperation;
    private final Map<String, AttributeDefinition<?, B>> _nameToAttribute;

    public BeanFacade(@Nonnull B bean, @Nullable BeanDefinition<B> beanDefinition) {
        _bean = bean;
        _beanDefinition = beanDefinition;
        _nameToOperation = asNameToOperation(beanDefinition.getOperationDefinitions());
        _nameToAttribute = asNameToAttribute(beanDefinition.getAttributeDefinitions());
    }

    @Override
    public Object getAttribute(String name) throws AttributeNotFoundException, MBeanException, ReflectionException {
        final AttributeDefinition<?, B> attributeDefinition = _nameToAttribute.get(name);
        if (attributeDefinition == null) {
            throw new AttributeNotFoundException(name);
        }
        try {
            return attributeDefinition.get(_bean);
        } catch (InvocationTargetException e) {
            final Throwable target = e.getTargetException();
            if (target instanceof Exception) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw new ReflectionException((Exception) target, target.getMessage());
            } else {
                throw new ReflectionException(e);
            }
        } catch (Exception e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        final String name = attribute.getName();
        final AttributeDefinition<?, B> attributeDefinition = _nameToAttribute.get(name);
        if (attributeDefinition == null) {
            throw new AttributeNotFoundException(name);
        }
        final Class<?> requiredType = attributeDefinition.getType();
        final Object value = correctValueIfNeeded(requiredType, attribute.getValue());
        if (value != null && !requiredType.isInstance(value)) {
            throw new InvalidAttributeValueException();
        }
        try {
            // noinspection unchecked
            ((AttributeDefinition<Object, B>)attributeDefinition).set(_bean, value);
        } catch (InvocationTargetException e) {
            final Throwable target = e.getTargetException();
            if (target instanceof Exception) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw new ReflectionException((Exception) target, target.getMessage());
            } else {
                throw new ReflectionException(e);
            }
        } catch (Exception e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object invoke(String name, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        final OperationDefinition<B> operationDefinition = _nameToOperation.get(name);
        if (operationDefinition == null) {
            throw new UnsupportedOperationException(name);
        }
        try {
            return operationDefinition.invoke(_bean, params);
        } catch (InvocationTargetException e) {
            final Throwable target = e.getTargetException();
            if (target instanceof Exception) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw new ReflectionException((Exception) target, target.getMessage());
            } else {
                throw new ReflectionException(e);
            }
        } catch (Exception e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(
            _bean.getClass().getName(),
            _beanDefinition.getDescription(),
            toMBeanAttributes(_nameToAttribute.values()),
            new MBeanConstructorInfo[0],
            toMBeanOperations(_nameToOperation.values()),
            new MBeanNotificationInfo[0]
        );
    }

    @Nonnull
    protected MBeanOperationInfo[] toMBeanOperations(@Nonnull Iterable<OperationDefinition<B>> operations) {
        final List<MBeanOperationInfo> result = new ArrayList<>();
        for (OperationDefinition<B> operation : operations) {
            result.add(toMBeanOperation(operation));
        }
        return result.toArray(new MBeanOperationInfo[result.size()]);
    }

    @Nonnull
    protected MBeanOperationInfo toMBeanOperation(@Nonnull OperationDefinition<B> operation) {
        final Class<?> returnType = operation.getReturnType();
        return new MBeanOperationInfo(
            operation.getName(),
            operation.getDescription(),
            getMBeanParameterInfoFor(operation),
            Void.class.equals(returnType) ? "void" : returnType.getName(),
            ACTION
        );
    }

    @Nonnull
    protected MBeanParameterInfo[] getMBeanParameterInfoFor(@Nonnull OperationDefinition<B> operation) {
        final ArgumentDefinition[] argumentDefinitions = operation.getArgumentDefinitions();
        final MBeanParameterInfo[] all = new MBeanParameterInfo[argumentDefinitions.length];
        for (int i = 0; i < argumentDefinitions.length; i++) {
            final ArgumentDefinition argumentDefinition = argumentDefinitions[i];
            all[i] = new MBeanParameterInfo(
                argumentDefinition.getName(),
                argumentDefinition.getType().getName(),
                argumentDefinition.getDescription()
            );
        }
        return all;
    }

    @Nonnull
    protected MBeanAttributeInfo[] toMBeanAttributes(@Nonnull Iterable<AttributeDefinition<?, B>> attributes) {
        final List<MBeanAttributeInfo> result = new ArrayList<>();
        for (AttributeDefinition<?, B> attribute : attributes) {
            result.add(toMBeanAttribute(attribute));
        }
        return result.toArray(new MBeanAttributeInfo[result.size()]);
    }

    @Nonnull
    protected MBeanAttributeInfo toMBeanAttribute(@Nonnull AttributeDefinition<?, B> attribute) {
        final AccessMode accessMode = attribute.getAccessMode();
        final Class<?> type = attribute.getType();
        return new MBeanAttributeInfo(
            attribute.getName(),
            type.getName(),
            attribute.getDescription(),
            accessMode.isReadingPossible(),
            accessMode.isWritingPossible(),
            Boolean.class.isAssignableFrom(type)
        );
    }

    @Nonnull
    protected Map<String, OperationDefinition<B>> asNameToOperation(@Nullable Iterable<OperationDefinition<B>> operations) {
        final Map<String, OperationDefinition<B>> result = new TreeMap<>();
        if (operations != null) {
            for (OperationDefinition<B> operation : operations) {
                result.put(operation.getName(), operation);
            }
        }
        return result;
    }

    @Nonnull
    protected Map<String, AttributeDefinition<?, B>> asNameToAttribute(@Nullable Iterable<AttributeDefinition<?, B>> attributes) {
        final Map<String, AttributeDefinition<?, B>> result = new TreeMap<>();
        if (attributes != null) {
            for (AttributeDefinition<?, B> attribute : attributes) {
                result.put(attribute.getName(), attribute);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "BeanFacade for " + _bean;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof BeanFacade)) {
            result = false;
        } else {
            final BeanFacade<?> that = (BeanFacade) o;
            result = _bean.equals(that._bean);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return _bean.hashCode();
    }
}
