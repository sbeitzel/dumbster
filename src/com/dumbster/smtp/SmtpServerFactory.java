package com.dumbster.smtp;

import com.dumbster.util.Config;
import org.apache.log4j.Logger;

/**
 * User: rj
 * Date: Aug 28, 2011
 * Time: 6:48:14 AM
 */
public class SmtpServerFactory {
    private static final Logger __l = Logger.getLogger(SmtpServerFactory.class);

    public static SmtpServer startServer() {
        SmtpServer server = new SmtpServer();
        wrapInShutdownHook(server);
        startServerThread(server);
        __l.info("Dumbster SMTP Server started on port " + Config.getConfig().getSMTPPort() + ".\n");
        return server;
    }

    private static void wrapInShutdownHook(final SmtpServer server) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                server.stop();
                __l.info("\nDumbster SMTP Server stopped");
                __l.info("\tTotal messages received: " + server.getEmailCount());
            }
        });
    }

    private static void startServerThread(SmtpServer server) {
        new Thread(server).start();
        int timeout = 1000;
        while (!server.isReady()) {
            try {
                Thread.sleep(1);
                timeout--;
                if (timeout < 1) {
                    throw new RuntimeException("Server could not be started.");
                }
            } catch (InterruptedException ignored) {
            }
        }
    }
}
