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

package org.echocat.jemoni.jmx.reflection;

import org.echocat.jemoni.jmx.ArgumentDefinition;
import org.echocat.jemoni.jmx.OperationDefinition;
import org.echocat.jemoni.jmx.OperationInvoker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ReflectionBasedOperationDefinition<B> implements OperationDefinition<B> {

    private final Method _method;

    private String _name;
    private String _description;
    private OperationInvoker<B> _invoker;

    @Nonnull
    public ReflectionBasedOperationDefinition(@Nonnull Method method) {
        _method = method;
        setName(null);
        setDescription(null);
        setInvoker(null);
    }

    @Nonnull
    @Override
    public String getName() {
        return _name;
    }

    @Nullable
    @Override
    public String getDescription() {
        return _description;
    }

    @Nonnull
    @Override
    public Class<?> getReturnType() {
        return _invoker.getReturnType();
    }

    @Nonnull
    @Override
    public ArgumentDefinition[] getArgumentDefinitions() {
        return _invoker.getArgumentDefinitions();
    }

    @Override
    public Object invoke(@Nonnull B bean, @Nonnull Object[] arguments) throws Exception {
        return _invoker.invoke(bean, arguments);
    }

    public void setName(@Nullable String name) {
        _name = !isEmpty(name) ? name : _method.getName();
    }

    public void setDescription(@Nullable String description) {
        _description = !isEmpty(description) ? description : null;
    }

    public void setInvoker(@Nullable OperationInvoker<B> invoker) {
        _invoker = invoker != null ? invoker : getInvokerFor(_method);
    }

    @Nonnull
    protected OperationInvoker<B> getInvokerFor(@Nonnull Method method) {
        return new ReflectionBasedOperationInvoker<>(method);
    }

    @Nonnull
    protected Method getMethod() {
        return _method;
    }

    @Nonnull
    protected OperationInvoker<B> getInvoker() {
        return _invoker;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getReturnType().getName()).append(" ").append(getName()).append('(');
        boolean first = true;
        for (ArgumentDefinition argumentDefinition : getArgumentDefinitions()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(argumentDefinition.getType().getName()).append(' ').append(argumentDefinition.getName());
        }
        sb.append(')');
        return sb.toString();
    }

}
