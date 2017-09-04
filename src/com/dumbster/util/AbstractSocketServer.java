package com.dumbster.util;
/*
 * Copyright 9/4/17 by Stephen Beitzel
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dumbster.smtp.MailStore;
import com.dumbster.smtp.mailstores.NullMailStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for any of the protocol servers we instantiate
 *
 * @author Stephen Beitzel &lt;sbeitzel@pobox.com&gt;
 */
public abstract class AbstractSocketServer implements Runnable {
    private static final Logger __l = LoggerFactory.getLogger(AbstractSocketServer.class);

    private volatile boolean _stopped = true;
    private volatile boolean _ready = false;
    private volatile MailStore _mailStore = new NullMailStore();
    private ServerSocket _serverSocket = null;
    private int _port;
    private ThreadPoolExecutor _executor = null;
    private int _threadCount = 1;

    public AbstractSocketServer() {
        Config cfg = Config.getConfig();
        _mailStore = cfg.getMailStore();
    }

    public void setPort(int port) {
        _port = port;
    }

    public int getPort() {
        return _port;
    }

    public void setThreadCount(int numThreads) {
        _threadCount = numThreads;
        if (_executor != null) {
            if (_threadCount > 0) {
                _executor.setMaximumPoolSize(_threadCount);
                _executor.setCorePoolSize(_threadCount);
            } else {
                _executor.setMaximumPoolSize(1);
                _executor.setCorePoolSize(1);
            }
        }
    }

    public void initExecutor() {
        if (_executor == null) {
            _executor = new ThreadPoolExecutor(_threadCount, _threadCount, 5, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
        }
    }

    protected ThreadPoolExecutor getExecutor() {
        return _executor;
    }

    public MailStore getMailStore() {
        return _mailStore;
    }

    public void setMailStore(MailStore mailStore) {
        _mailStore = mailStore;
    }

    protected void initializeServerSocket() throws Exception {
        _serverSocket = new ServerSocket(_port);
        _serverSocket.setSoTimeout(Config.SERVER_SOCKET_TIMEOUT);
    }

    protected ServerSocket getServerSocket() {
        return _serverSocket;
    }

    protected void shutdownSocket() {
        if (_serverSocket != null) {
            try {
                _serverSocket.close();
                _serverSocket = null;
            } catch (IOException e) {
                __l.error("Exception closing socket", e);
            }
        }
    }

    public boolean isStopped() {
        return _stopped;
    }

    public void stop() {
        _stopped = true;
        try {
            if (_serverSocket != null) {
                _serverSocket.close();
                _serverSocket = null;
            }
        } catch (IOException ignored) {
        }
    }

    public boolean isReady() {
        return _ready;
    }

    protected final void setReady(boolean state) {
        _ready = state;
    }

    @Override
    public final void run() {
        _stopped = false;
        try {
            initializeServerSocket();
            serverLoop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            _ready = false;
            shutdownSocket();
        }
    }

    protected abstract void serverLoop() throws IOException;

    protected final Socket clientSocket() throws IOException {
        Socket socket = null;
        while (socket == null) {
            socket = accept();
        }
        return socket;
    }

    private Socket accept() {
        try {
            setReady(true);
            return getServerSocket().accept();
        } catch (Exception e) {
            return null;
        }
    }
}
