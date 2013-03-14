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
import org.echocat.jemoni.jmx.RegistrationWithFacade;
import org.echocat.jemoni.jmx.annotations.Argument;
import org.echocat.jemoni.jmx.annotations.Attribute;
import org.echocat.jemoni.jmx.annotations.Bean;
import org.echocat.jemoni.jmx.annotations.Operation;

import javax.annotation.*;
import java.lang.Thread.State;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Runtime.getRuntime;
import static java.lang.management.ManagementFactory.*;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

@Bean(description = "Display basic information of the JVM.")
public class JvmHealth implements AutoCloseable {

    private final JmxRegistry _registry;

    private RegistrationWithFacade<JvmHealth> _registration;

    public JvmHealth(@Nonnull JmxRegistry registry) {
        _registry = registry;
    }

    @PostConstruct
    public void init() throws Exception {
        _registration = _registry.register(this);
    }

    @Override
    @PreDestroy
    public void close() throws Exception {
        try {
            closeQuietly(_registration);
        } finally {
            _registration = null;
        }
    }

    @Nonnegative
    @Attribute(description = "Maximum amount of memory in bytes that can be used for memory management. This is -1 if the maximum memory size is undefined. This amount of memory is not guaranteed to be available for memory management if it is greater than the amount of committed memory. The Java virtual machine may fail to allocate memory even if the amount of used memory does not exceed this maximum size.")
    public long getHeapMax() {
        return getMemoryMXBean().getHeapMemoryUsage().getMax();
    }

    @Nonnegative
    @Attribute(description = "Amount of memory in bytes that is committed for the Java virtual machine to use. This amount of memory is guaranteed for the Java virtual machine to use.")
    public long getHeapCommitted() {
        return getMemoryMXBean().getHeapMemoryUsage().getCommitted();
    }

    @Nonnegative
    @Attribute(description = "Amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management. This is -1 if the initial memory size is undefined.")
    public long getHeapInit() {
        return getMemoryMXBean().getHeapMemoryUsage().getInit();
    }

    @Nonnegative
    @Attribute(description = "Amount of used memory in bytes.")
    public long getHeapUsed() {
        return getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    @Nonnegative
    @Attribute(description = "Maximum amount of memory in bytes that can be used for memory management. This is -1 if the maximum memory size is undefined. This amount of memory is not guaranteed to be available for memory management if it is greater than the amount of committed memory. The Java virtual machine may fail to allocate memory even if the amount of used memory does not exceed this maximum size.")
    public long getNonHeapMax() {
        return getMemoryMXBean().getNonHeapMemoryUsage().getMax();
    }

    @Nonnegative
    @Attribute(description = "Amount of memory in bytes that is committed for the Java virtual machine to use. This amount of memory is guaranteed for the Java virtual machine to use.")
    public long getNonHeapCommitted() {
        return getMemoryMXBean().getNonHeapMemoryUsage().getCommitted();
    }

    @Nonnegative
    @Attribute(description = "Amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management. This is -1 if the initial memory size is undefined.")
    public long getNonHeapInit() {
        return getMemoryMXBean().getNonHeapMemoryUsage().getInit();
    }

    @Nonnegative
    @Attribute(description = "Amount of used memory in bytes.")
    public long getNonHeapUsed() {
        return getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
    }

    @Nonnegative
    @Attribute(description = "Total number of collections that have occurred.")
    public long getGcCollectionCount() {
        long result = 0;
        for (GarbageCollectorMXBean bean : getGarbageCollectorMXBeans()) {
            final long current = bean != null ? bean.getCollectionCount() : -1;
            if (current >= 0) {
                result += current;
            }
        }
        return result;
    }

    @Nonnegative
    @Attribute(description = "Approximate accumulated collection elapsed time in milliseconds. The Java virtual machine implementation may use a high resolution timer to measure the elapsed time.  This method may return the same value even if the collection count has been incremented if the collection elapsed time is very short.")
    public long getGcCollectionTime() {
        long result = 0;
        for (GarbageCollectorMXBean bean : getGarbageCollectorMXBeans()) {
            final long current = bean != null ? bean.getCollectionTime() : -1;
            if (current >= 0) {
                result += current;
            }
        }
        return result;
    }

    @Nonnegative
    @Attribute(description = "Amount of all currently busy threads in the Java Virtual Machine.")
    public long getBusyThreadCount() {
        final ThreadMXBean threadMXBean = getThreadMXBean();
        int count = 0;
        for (ThreadInfo threadInfo : threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds())) {
            if (threadInfo != null && threadInfo.getThreadState() == State.RUNNABLE) {
                count++;
            }
        }
        return count;
    }

    @Nonnegative
    @Attribute(description = "Amount of all currently alive threads in the Java Virtual Machine.")
    public long getTotalThreadCount() {
        return getThreadMXBean().getThreadCount();
    }

    @Nonnegative
    @Attribute(description = "Amount of all currently loaded classes in the Java Virtual Machine.")
    public long getLoadedClassCount() {
        return getClassLoadingMXBean().getLoadedClassCount();
    }

    @Nonnegative
    @Attribute(description = "Amount of all loaded classes since the Java Virtual Machine has started.")
    public long getTotalLoadedClassCount() {
        return getClassLoadingMXBean().getTotalLoadedClassCount();
    }

    @Nonnegative
    @Attribute(description = "Amount of all unloaded classes since the Java Virtual Machine has started.")
    public long getUnloadedClassCount() {
        return getClassLoadingMXBean().getUnloadedClassCount();
    }

    @Nonnegative
    @Attribute(description = "Uptime of the Java virtual machine in milliseconds.")
    public long getUptime() {
        return getRuntimeMXBean().getUptime();
    }

    @Operation(description = "Runs the garbage collector. Calling this method suggests that the Java virtual machine expend effort toward recycling unused objects in order to make the memory they currently occupy available for quick reuse. When control returns from the method call, the virtual machine has made its best effort to recycle all discarded objects.")
    public void gc() {
        // noinspection CallToSystemGC
        getRuntime().gc();
    }

    @Operation(description = "Terminates the currently running Java virtual machine by initiating its shutdown sequence. This method never returns normally.  The argument serves as a status code; by convention, a nonzero status code indicates abnormal termination.")
    public void exit(@Argument(name = "exitCode") @Nonnegative int exitCode) {
        // noinspection CallToSystemExit
        getRuntime().exit(exitCode);
    }

    @Operation(description = "Retrieves all system properties.")
    public Map<String, String> getSystemProperties() {
        // noinspection unchecked,RedundantCast
        final Map<String, String> properties = new TreeMap<>((Map<String, String>) (Object) System.getProperties());
        return properties;
    }

    @Nullable
    @Operation(description = "Retrieves a given system property. If null this property is not set.")
    public String getSystemProperty(@Argument(name = "name") @Nonnull String name) {
        return System.getProperty(name);
    }

    @Operation(description = "Set a system property to the given value.")
    public void setSystemProperty(@Argument(name = "name") @Nonnull String name, @Argument(name = "value") @Nullable String value) {
        System.setProperty(name, value);
    }

}
