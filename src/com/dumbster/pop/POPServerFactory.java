package com.dumbster.pop;

import java.util.concurrent.Executors;

import com.dumbster.util.Config;
import org.apache.log4j.Logger;

public class POPServerFactory {
    private static final Logger __l = Logger.getLogger(POPServerFactory.class);

    public static POPServer startServer() {
        POPServer server = new POPServer();

        wrapInShutdownHook(server);
        __l.info("Dumbster POP3 Server started on port " + Config.getConfig().getPOP3Port() + ".\n");
        Executors.newSingleThreadExecutor().execute(server);
        return whenReady(server);
    }

    private static POPServer whenReady(POPServer server) {
        while (!server.isReady()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
