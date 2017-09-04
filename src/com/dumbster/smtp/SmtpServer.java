/*
 * Dumbster - a dummy SMTP server
 * Copyright 2004 Jason Paul Kitchen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dumbster.smtp;

import java.io.IOException;

import com.dumbster.util.AbstractSocketServer;
import com.dumbster.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy SMTP server for testing purposes.
 */
public class SmtpServer extends AbstractSocketServer {
    private static final Logger __l = LoggerFactory.getLogger(SmtpServer.class);

    SmtpServer() {
        Config cfg = Config.getConfig();
        setPort(cfg.getSMTPPort());
        setThreadCount(cfg.getNumSMTPThreads());
        initExecutor();
    }

    protected void serverLoop() throws IOException {
        __l.info("Dumbster SMTP server started on port "+getPort());
        while (!isStopped()) {
            SocketWrapper source = new SocketWrapper(clientSocket());
            ClientSession session = new ClientSession(source, getMailStore());
            getExecutor().execute(session);
        }
        setReady(false);
    }

    public MailMessage[] getMessages() {
        return getMailStore().getMessages();
    }

    public MailMessage getMessage(int i) {
        return getMailStore().getMessage(i);
    }

    public int getEmailCount() {
        return getMailStore().getEmailCount();
    }

    public void anticipateMessageCountFor(int messageCount, int ticks) {
        __l.trace("Want "+messageCount+" messages in "+ticks+" ticks");
        int tickdown = ticks;
        while (getMailStore().getEmailCount() < messageCount && tickdown > 0) {
            tickdown--;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                __l.debug("Interrupted");
                return;
            }
        }
        __l.trace("Counted down to "+tickdown);
    }

    public void clearMessages() {
        getMailStore().clearMessages();
    }

}
