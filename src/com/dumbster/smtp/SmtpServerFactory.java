package com.dumbster.smtp;

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: rj
 * Date: Aug 28, 2011
 * Time: 6:48:14 AM
 */
public class SmtpServerFactory {
    private static final Logger __l = LoggerFactory.getLogger(SmtpServerFactory.class);

    public static SmtpServer startServer() {
        SmtpServer server = new SmtpServer();
        wrapInShutdownHook(server);
        Executors.newSingleThreadExecutor().execute(server);
        return whenReady(server);
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

    private static SmtpServer whenReady(SmtpServer server) {
        while (!server.isReady()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                __l.error("Interrupted while starting up SMTP server", e);
            }
        }
        return server;
    }
}
