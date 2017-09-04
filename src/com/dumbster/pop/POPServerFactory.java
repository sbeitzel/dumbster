package com.dumbster.pop;

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class POPServerFactory {
    private static final Logger __l = LoggerFactory.getLogger(POPServerFactory.class);

    public static POPServer startServer() {
        POPServer server = new POPServer();

        wrapInShutdownHook(server);
        Executors.newSingleThreadExecutor().execute(server);
        return whenReady(server);
    }

    private static POPServer whenReady(POPServer server) {
        while (!server.isReady()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                __l.error("Interrupted while starting POP3 server", e);
            }
        }
        return server;
    }

    private static void wrapInShutdownHook(final POPServer server) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                server.stop();
                __l.info("\nDumbster POP3 Server stopped");
            }
         });
    }
}
