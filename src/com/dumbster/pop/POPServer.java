package com.dumbster.pop;

import java.io.IOException;
import java.net.Socket;

import com.dumbster.smtp.SocketWrapper;
import com.dumbster.util.AbstractSocketServer;
import com.dumbster.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class POPServer extends AbstractSocketServer {
    private static final Logger __l = LoggerFactory.getLogger(POPServer.class);

    POPServer() {
        Config cfg = Config.getConfig();
        setPort(cfg.getPOP3Port());
        setThreadCount(cfg.getNumPOPThreads());
        initExecutor();
    }

    @Override
    protected void serverLoop() throws IOException {
        __l.info("Dumbster POP3 server started on port "+getPort());
        while (!isStopped()) {
            SocketWrapper source = new SocketWrapper(clientSocket());
            ClientSession session = new ClientSession(source, getMailStore());
            getExecutor().execute(session);
        }
        setReady(false);
    }

}
