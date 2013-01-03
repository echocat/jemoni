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

import org.echocat.jomon.net.FreeTcpPortDetector;
import org.echocat.jomon.runtime.numbers.IntegerRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;
import static java.net.InetAddress.getLocalHost;
import static java.nio.charset.Charset.forName;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.echocat.jomon.runtime.concurrent.ThreadUtils.stop;

public class VirtualCarbonServer implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(VirtualCarbonServer.class);

    private final List<MeasurePoint> _recordedMeasurePoints = new ArrayList<>();

    private final InetSocketAddress _address;
    private final Acceptor _acceptor;
    private final Set<Worker> _workers = new HashSet<>();

    private Exception _fatalException;

    private volatile Charset _charset = forName("UTF-8");

    public VirtualCarbonServer(@Nonnull InetSocketAddress address) {
        final ServerSocket serverSocket;
        try {
            _address = address.getAddress().isAnyLocalAddress() ? new InetSocketAddress(getLocalHost(), address.getPort()) : address;
            serverSocket = new ServerSocket();
            serverSocket.bind(address);
        } catch (IOException e) {
            throw new RuntimeException("Could not bind to " + address + ".", e);
        }
        _acceptor = new Acceptor(serverSocket);
        _acceptor.start();
    }

    public VirtualCarbonServer(@Nonnegative int port) {
        this(new InetSocketAddress((InetAddress) null, port));
    }

    public VirtualCarbonServer(@Nullable InetAddress address, @Nullable IntegerRange portRange) {
        this(detectPort(address, portRange));
    }

    public VirtualCarbonServer(@Nullable InetAddress address) {
        this(address, null);
    }

    public VirtualCarbonServer(@Nullable IntegerRange portRange) {
        this(null, portRange);
    }

    public VirtualCarbonServer() {
        this((IntegerRange) null);
    }

    @Nonnull
    public Charset getCharset() {
        return _charset;
    }

    public void setCharset(@Nonnull Charset charset) {
        _charset = charset;
    }

    @Nonnull
    public InetSocketAddress getAddress() {
        return _address;
    }

    @Nonnull
    public List<MeasurePoint> getLastRecordedMeasurePoints() throws IOException {
        synchronized (this) {
            assertNoFatalException();
            return new ArrayList<>(_recordedMeasurePoints);
        }
    }

    public void clearRecordedMeasurePoints() throws IOException {
        synchronized (this) {
            assertNoFatalException();
            _recordedMeasurePoints.clear();
        }
    }

    protected void assertNoFatalException() throws IOException {
        if (_fatalException != null) {
            if (_fatalException instanceof RuntimeException) {
                throw (RuntimeException) _fatalException;
            } else if (_fatalException instanceof IOException) {
                throw (IOException) _fatalException;
            } else {
                throw new RuntimeException("Could not read messages from remote.", _fatalException);
            }
        }
    }

    protected void recordedMeasurePoints(@Nullable MeasurePoint measurePoint) {
        if (measurePoint != null) {
            synchronized (this) {
                _recordedMeasurePoints.add(measurePoint);
            }
        }
    }

    @Nullable
    protected MeasurePoint parseMeasurePoint(@Nonnull String plain, @Nonnull SocketAddress remote) {
        MeasurePoint measurePoint;
        final String trimmed = plain.trim();
        if (!trimmed.isEmpty()) {
            final String[] parts = trimmed.split(" ", 3);
            if (parts.length == 3) {
                try {
                    final Date date = new Date(SECONDS.toMillis(parseLong(parts[2])));
                    final double value = parseDouble(parts[1]);
                    measurePoint = new MeasurePoint(parts[0], date, value);
                } catch (NumberFormatException ignored) {
                    LOG.info("Received illegal measure point from " + remote + ": " + trimmed);
                    measurePoint = null;
                }
            } else {
                LOG.info("Received illegal measure point from " + remote + ": " + trimmed);
                measurePoint = null;
            }
        } else {
            measurePoint = null;
        }
        return measurePoint;
    }

    @Override
    public void close() throws IOException {
        stop(_workers);
        stop(_acceptor);
    }

    protected class Acceptor extends Thread implements Closeable {

        private final ServerSocket _serverSocket;

        public Acceptor(@Nonnull ServerSocket serverSocket) {
            super(VirtualCarbonServer.this.getClass().getSimpleName() + ".Acceptor(" + serverSocket.getLocalSocketAddress() + ")");
            _serverSocket = serverSocket;
        }

        @Override
        public void run() {
            try {
                while (!currentThread().isInterrupted()) {
                    final Socket remote = _serverSocket.accept();
                    synchronized (_workers) {
                        final Worker worker = new Worker(remote);
                        _workers.add(worker);
                        worker.start();
                    }
                }
            } catch (InterruptedIOException ignored) {
                currentThread().interrupt();
            } catch (Exception e) {
                if (!(e instanceof SocketException) || !_serverSocket.isClosed()) {
                    synchronized (VirtualCarbonServer.this) {
                        _fatalException = e;
                    }
                    LOG.error("Got an error from " + _serverSocket + " while accepting a connection.", e);
                }
            }
        }
        @Override
        public void close() throws IOException {
            _serverSocket.close();
            interrupt();
        }

    }

    protected class Worker extends Thread implements Closeable {

        private final Socket _socket;

        public Worker(@Nonnull Socket socket) {
            super(VirtualCarbonServer.this.getClass().getSimpleName() + ".Worker(" + socket.getRemoteSocketAddress() + ">" + socket.getLocalSocketAddress() +  ")");
            _socket = socket;
        }

        @Override
        public void run() {
            try (final InputStream is = _socket.getInputStream()) {
                try (final Reader reader = new InputStreamReader(is, _charset)) {
                    try (final BufferedReader bufferedReader = new BufferedReader(reader)) {
                        String line = bufferedReader.readLine();
                        while (!currentThread().isInterrupted() && _socket.isConnected() && line != null) {
                            final MeasurePoint measurePoint = parseMeasurePoint(line, _socket.getRemoteSocketAddress());
                            recordedMeasurePoints(measurePoint);
                            line = bufferedReader.readLine();
                        }
                    }
                }
            } catch (InterruptedIOException ignored) {
                currentThread().interrupt();
            } catch (SocketException ignored) {
            } catch (Exception e) {
                synchronized (VirtualCarbonServer.this) {
                    _fatalException = e;
                }
                LOG.error("Got an error from " + _socket + " while handle connection.", e);
            }
        }
        @Override
        public void close() throws IOException {
            _socket.close();
            interrupt();
        }

    }

    @Nonnull
    protected static InetSocketAddress detectPort(@Nullable InetAddress address, @Nullable IntegerRange portRange) {
        final int port = new FreeTcpPortDetector(address, portRange).detect();
        return new InetSocketAddress(address, port);
    }

}
