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

package org.echocat.jemoni.carbon;

import org.echocat.jomon.runtime.concurrent.RetryForSpecifiedCountStrategy;
import org.echocat.jomon.runtime.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Character.isLetterOrDigit;
import static java.lang.Character.isWhitespace;
import static java.lang.Thread.currentThread;
import static java.nio.ByteBuffer.allocate;
import static java.nio.charset.Charset.forName;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.echocat.jomon.runtime.concurrent.Retryer.executeWithRetry;
import static org.echocat.jomon.runtime.concurrent.ThreadUtils.stop;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class CarbonWriter implements AutoCloseable {

    public static final Charset DEFAULT_CHARSET = forName("UTF-8");
    public static final Duration DEFAULT_MAX_BUFFER_LIFETIME = new Duration("10s");

    private static final Logger LOG = LoggerFactory.getLogger(CarbonWriter.class);

    protected static final int BUFFER_SIZE = 1024;
    protected static final RetryForSpecifiedCountStrategy<Void> RETRYING_STRATEGY = new RetryForSpecifiedCountStrategy<Void>(2) { @Override protected boolean isExceptionThatForceRetry(@Nonnull Throwable e) {
        return e instanceof IOException;
    }};

    private final BlockingDeque<MeasurePoint> _messageQueue = new LinkedBlockingDeque<>(1000);
    private List<ByteBuffer> _bufferQueue = new ArrayList<>();
    private ByteBuffer _buffer = allocate(BUFFER_SIZE);

    private final Lock _lock = new ReentrantLock();
    private final Condition _condition = _lock.newCondition();

    private volatile InetSocketAddress _address;
    private volatile Charset _charset = DEFAULT_CHARSET;
    private volatile Duration _maxBufferLifetime = DEFAULT_MAX_BUFFER_LIFETIME;

    private Thread _convertingThread;
    private Thread _writingThread;

    private Socket _socket;

    public InetSocketAddress getAddress() {
        return _address;
    }

    public void setAddress(InetSocketAddress address) {
        _address = address;
        _lock.lock();
        try {
            if (_writingThread != null) {
                _writingThread.setName(toString() + ".Writer");
            }
            if (_convertingThread != null) {
                _convertingThread.setName(toString() + ".Converter");
            }
            if (_socket != null) {
                try {
                    closeQuietly(_socket);
                } finally {
                    _socket = null;
                }
            }
        } finally {
            _lock.unlock();
        }
    }

    @Nonnull
    public Duration getMaxBufferLifetime() {
        return _maxBufferLifetime;
    }

    public void setMaxBufferLifetime(@Nonnull Duration maxBufferLifetime) {
        _maxBufferLifetime = maxBufferLifetime;
    }

    @Nonnull
    public Charset getCharset() {
        return _charset;
    }

    public void setCharset(@Nonnull Charset charset) {
        _charset = charset;
    }

    public void write(@Nonnull MeasurePoint... measurePoints) throws InterruptedException {
        write(asList(measurePoints));
    }

    public void write(@Nonnull Iterable<MeasurePoint> measurePoints) throws InterruptedException {
        for (MeasurePoint measurePoint : measurePoints) {
            write(measurePoint);
        }
    }

    @Nonnull
    public MeasurePoint write(@Nonnull String path, @Nonnull Number value) throws InterruptedException {
        final MeasurePoint point = new MeasurePoint(path, value);
        write(point);
        return point;
    }

    @Nonnull
    public MeasurePoint write(@Nonnull String path, @Nonnull Date timestamp, @Nonnull Number value) throws InterruptedException {
        final MeasurePoint point = new MeasurePoint(path, timestamp, value);
        write(point);
        return point;
    }

    public void write(@Nonnull MeasurePoint measurePoint) throws InterruptedException {
        if (_writingThread != null || _convertingThread != null) {
            _messageQueue.put(measurePoint);
        }
    }

    @PostConstruct
    public void init() throws Exception {
        _lock.lock();
        try {
            boolean success = false;
            try {
                _writingThread = new Thread(new Writer(), toString() + ".Writer");
                _writingThread.setDaemon(true);
                _writingThread.start();
                _convertingThread = new Thread(new Converter(), toString() + ".Converter");
                _convertingThread.setDaemon(true);
                _convertingThread.start();
                success = true;
            } finally {
                if (!success) {
                    close();
                }
            }
        } finally {
            _lock.unlock();
        }
    }

    @Override
    @PreDestroy
    public void close() throws Exception {
        try {
            stop(_convertingThread);
            stop(_writingThread);
        } finally {
            _lock.lock();
            try {
                try {
                    try {
                        try {
                            stop(_convertingThread);
                        } finally {
                            _convertingThread = null;
                        }
                    } finally {
                        try {
                            stop(_writingThread);
                        } finally {
                            _writingThread = null;
                        }
                    }
                } finally {
                    try {
                        closeQuietly(_socket);
                    } finally {
                        _socket = null;
                    }
                }
            } finally {
                _lock.unlock();
            }
        }
    }

    protected void convertAndPutIntoQueue(@Nonnull MeasurePoint measurePoint) {
        final byte[] message = convert(measurePoint);
        _lock.lock();
        try {
            if (message.length > _buffer.remaining()) {
                if (_bufferQueue.size() > 1000) {
                    LOG.warn("The queue seems to be full. Current size is " + _bufferQueue.size() + ". Is the converting thread dead?");
                }
                _bufferQueue.add(_buffer);
                _buffer = allocate(message.length < BUFFER_SIZE ? BUFFER_SIZE : message.length);
            }
            _buffer.put(message);
            _condition.signalAll();
        } finally {
            _lock.unlock();
        }
    }

    @Nonnull
    protected byte[] convert(@Nonnull MeasurePoint measurePoint) {
        final String messageAsString = formatPath(measurePoint) + " " + formatValue(measurePoint) + " " + toUnixTimestamp(measurePoint) + "\n";
        return messageAsString.getBytes(_charset);
    }

    @Nonnull
    protected String formatPath(@Nonnull MeasurePoint measurePoint) {
        final StringBuilder sb = new StringBuilder();
        for (char c : measurePoint.getPath().toCharArray()) {
            if (isLetterOrDigit(c) || c == '-' || c == '_' || c == '.') {
                sb.append(c);
            } else if (isWhitespace(c)) {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    @Nonnull
    protected Number formatValue(@Nonnull MeasurePoint measurePoint) {
        return measurePoint.getValue();
    }

    @Nonnegative
    protected long toUnixTimestamp(@Nonnull MeasurePoint measurePoint) {
        return MILLISECONDS.toSeconds(measurePoint.getTimestamp().getTime());
    }

    @Nonnull
    protected List<ByteBuffer> getNextForWrite(boolean force) throws InterruptedException {
        final List<ByteBuffer> result = new ArrayList<>();
        if (force) {
            _lock.lock();
        } else {
            _lock.lockInterruptibly();
        }
        try {
            if (force || !_condition.await(_maxBufferLifetime.toMilliSeconds(), MILLISECONDS)) {
                result.add(_buffer);
                _buffer = allocate(BUFFER_SIZE);
            }
            result.addAll(_bufferQueue);
            _bufferQueue = new ArrayList<>();
        } finally {
            _lock.unlock();
        }
        return result;
    }

    public void flush() throws IOException {
        try {
            final List<ByteBuffer> buffers = getNextForWrite(true);
            writeMessages(buffers);
        } catch (InterruptedException e) {
            currentThread().interrupt();
            throw new RuntimeException("Could not flush.", e);
        }
    }

    protected void writeMessages(@Nonnull Iterable<ByteBuffer> buffers) throws InterruptedException, IOException {
        for (final ByteBuffer buffer : buffers) {
            executeWithRetry(new Callable<Void>() { @Override public Void call() throws IOException, InterruptedException {
                writeMessage(buffer);
                return null;
            }}, RETRYING_STRATEGY, IOException.class);
        }
    }

    protected void writeMessage(@Nonnull ByteBuffer buffer) throws IOException, InterruptedException {
        boolean success = false;
        final Socket socket = getSocket();
        if (socket != null) {
            try {
                final OutputStream os = socket.getOutputStream();
                try {
                    os.write(buffer.array(), 0, buffer.position());
                    success = true;
                } finally {
                    if (success) {
                        os.flush();
                    } else {
                        closeQuietly(os);
                    }
                }
            } finally {
                if (!success) {
                    closeQuietly(socket);
                }
            }
        }
    }

    @Nullable
    protected Socket getSocket() throws IOException, InterruptedException {
        _lock.lockInterruptibly();
        try {
            if (_socket == null || !_socket.isConnected() || _socket.isClosed()) {
                final InetSocketAddress address = _address;
                if (address != null) {
                    _socket = new Socket();
                    _socket.connect(address);
                } else {
                    _socket = null;
                }
            }
            return _socket;
        } finally {
            _lock.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof CarbonWriter)) {
            result = false;
        } else {
            final CarbonWriter that = (CarbonWriter) o;
            result = _address != null ? _address.equals(that._address) : that._address == null;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return _address != null ? _address.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + _address + "}";
    }

    protected class Converter implements Runnable {
        @Override
        public void run() {
            try {
                while (!currentThread().isInterrupted()) {
                    final MeasurePoint next = _messageQueue.take();
                    convertAndPutIntoQueue(next);
                }
            } catch (InterruptedException ignored) {
                currentThread().interrupt();
            }
        }
    }

    protected class Writer implements Runnable {
        @Override
        public void run() {
            try {
                while (!currentThread().isInterrupted()) {
                    final List<ByteBuffer> buffers = getNextForWrite(false);
                    try {
                        writeMessages(buffers);
                    } catch (ConnectException e) {
                        _lock.lockInterruptibly();
                        try {
                            if (_bufferQueue.size() < 100000) {
                                LOG.warn("Could not reach " + _address + " reschedule messages.", e);
                                _bufferQueue.addAll(buffers);
                            } else {
                                LOG.warn("Could not reach " + _address + " . The messages are lost.", e);
                            }
                        } finally {
                            _lock.unlock();
                        }
                    } catch (IOException e) {
                        LOG.warn("Could not write message to " + _address + ". The messages are lost.", e);
                    }
                }
            } catch (InterruptedException ignored) {
                currentThread().interrupt();
            }
        }
    }
}
