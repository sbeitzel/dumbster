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

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import com.dumbster.smtp.mailstores.RollingMailStore;
import com.dumbster.util.Config;
import net.jcip.annotations.NotThreadSafe;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NotThreadSafe
public class SmtpServerTest {
    private static final Logger __l = LoggerFactory.getLogger(SmtpServerTest.class);

    private static final int SMTP_PORT = 1081;

    private static SmtpServer server;
    private static int ORIG_PORT;
    private static MailStore ORIG_STORE;
    private static Config CONFIG;

    private final String FROM = "sender@here.com";
    private final String TO = "receiver@there.com";
    private final String SUBJECT = "Test";
    private final String BODY = "Test Body";

    private final int WAIT_TICKS = 10000;

    @BeforeClass
    public static void setup() {
        CONFIG = Config.getConfig();
        ORIG_PORT = CONFIG.getSMTPPort();
        ORIG_STORE = CONFIG.getMailStore();
        CONFIG.setSMTPPort(SMTP_PORT);
        CONFIG.setMailStore(new RollingMailStore());
        server = SmtpServerFactory.startServer();
    }

    @AfterClass
    public static void teardown() {
        server.stop();
        CONFIG.setMailStore(ORIG_STORE);
        CONFIG.setSMTPPort(ORIG_PORT);
    }

    @Before
    public void clearStore() {
        CONFIG.getMailStore().clearMessages();
    }

    @Test
    public void testNoMessageSentButWaitingDoesNotHang() {
        server.anticipateMessageCountFor(1, 10);
        assertEquals(0, server.getEmailCount());
    }

    @Test
    public void testSend() {
        sendMessage(SMTP_PORT, FROM, SUBJECT, BODY, TO);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        assertTrue(server.getEmailCount() == 1);
        MailMessage email = server.getMessage(0);
        assertEquals("Test", email.getFirstHeaderValue("Subject"));
        assertEquals("Test Body", email.getBody());
    }

    @Test
    public void testClearMessages() {
        sendMessage(SMTP_PORT, FROM, SUBJECT, BODY, TO);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        assertEquals(1, server.getEmailCount());
        sendMessage(SMTP_PORT, FROM, SUBJECT, BODY, TO);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        assertEquals(2, server.getEmailCount());
        server.clearMessages();
        assertEquals("", 0, server.getEmailCount());
    }

    @Test
    public void testSendWithLongSubject() {
        String longSubject = StringUtil.longString(500);
        sendMessage(SMTP_PORT, FROM, longSubject, BODY, TO);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        assertTrue(server.getEmailCount() == 1);
        MailMessage email = server.getMessage(0);
        assertEquals(longSubject, email.getFirstHeaderValue("Subject"));
        assertEquals(500, longSubject.length());
        assertEquals("Test Body", email.getBody());
    }

    @Test
    public void testSendWithFoldedSubject() {
        String subject = "This\r\n is a folded\r\n Subject line.";
        MailMessage email = sendMessageWithSubject(subject);
        assertEquals("This is a folded Subject line.", email.getFirstHeaderValue("Subject"));
    }

    private MailMessage sendMessageWithSubject(String subject) {
        sendMessage(SMTP_PORT, FROM, subject, BODY, TO);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        assertEquals(1, server.getEmailCount());
        return server.getMessage(0);
    }

    @Test
    public void testSendWithFoldedSubjectLooksLikeHeader() {
        String subject = "This\r\n really: looks\r\n strange.";
        MailMessage email = sendMessageWithSubject(subject);
        assertEquals("This really: looks strange.", email.getFirstHeaderValue("Subject"));
    }

    @Test
    @Ignore
    // should this work?
    public void testSendMessageWithCarriageReturn() {
        String bodyWithCR = "\r\nKeep these pesky carriage returns\r\n";
        sendMessage(SMTP_PORT, FROM, SUBJECT, bodyWithCR, TO);
        assertEquals(1, server.getEmailCount());
        MailMessage email = server.getMessage(0);
        assertEquals(bodyWithCR, email.getBody());
    }

    @Test
    public void testThreadedSend() {
        server.setThreaded(true);
        sendMessage(SMTP_PORT, FROM, SUBJECT, BODY, TO);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        assertTrue(server.getEmailCount() == 1);
        MailMessage email = server.getMessage(0);
        assertEquals("Test", email.getFirstHeaderValue("Subject"));
        assertEquals("Test Body", email.getBody());
    }

    @Test
    public void testSendTwoMessagesSameConnection() {
        try {
            MimeMessage[] mimeMessages = new MimeMessage[2];
            Properties mailProps = getMailProperties(SMTP_PORT);
            Session session = Session.getInstance(mailProps, null);

            mimeMessages[0] = createMessage(session, "sender@whatever.com", "receiver@home.com", "Doodle1", "Bug1");
            mimeMessages[1] = createMessage(session, "sender@whatever.com", "receiver@home.com", "Doodle2", "Bug2");

            Transport transport = session.getTransport("smtp");
            transport.connect("localhost", SMTP_PORT, null, null);

            for (MimeMessage mimeMessage : mimeMessages) {
                transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            }

            transport.close();
        } catch (MessagingException e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
        server.anticipateMessageCountFor(2, WAIT_TICKS);
        assertEquals(2, server.getEmailCount());
    }

    @Test
    @Ignore // this works when run in a single process but fails when junit is forked off the runner
    public void testSendingFileAttachment() throws MessagingException {
        Properties props = getMailProperties(SMTP_PORT);
        props.put("mail.smtp.host", "localhost");
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(FROM));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(TO));
        message.setSubject(SUBJECT);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(buildMessageBody());
        multipart.addBodyPart(buildFileAttachment());
        message.setContent(multipart);
        __l.debug("Sending message with attachment.");
        Transport.send(message);
        __l.debug("Sent message "+message.getMessageID());
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        assertTrue(server.getMessage(0).getBody().indexOf("Apache License") > 0);
    }

    private MimeBodyPart buildFileAttachment() throws MessagingException {
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        String fileName = "license.txt";
        javax.activation.FileDataSource source = new javax.activation.FileDataSource(fileName);
        assertTrue("Unable to read the file!", source.getFile().exists() && source.getFile().canRead());
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(fileName);
        return messageBodyPart;
    }

    private MimeBodyPart buildMessageBody() throws MessagingException {
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(BODY);
        return messageBodyPart;
    }

    @Test
    public void testSendTwoMsgsWithLogin() {
        try {

            Properties props = System.getProperties();

            Session session = Session.getDefaultInstance(props, null);
            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(FROM));

            InternetAddress.parse(TO, false);
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO, false));
            msg.setSubject(SUBJECT);

            msg.setText(BODY);
            msg.setHeader("X-Mailer", "musala");
            msg.setSentDate(new Date());
            msg.saveChanges();

            Transport transport = null;

            try {
                transport = session.getTransport("smtp");
                String SERVER = "localhost";
                transport.connect(SERVER, SMTP_PORT, "ddd", "ddd");
                __l.debug("Sending two messages with login.");
                transport.sendMessage(msg, InternetAddress.parse(TO, false));
                transport.sendMessage(msg, InternetAddress.parse("dimiter.bakardjiev@musala.com", false));
            } catch (javax.mail.MessagingException me) {
                me.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (transport != null) {
                    transport.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        server.anticipateMessageCountFor(2, WAIT_TICKS);
        assertEquals(2, server.getEmailCount());
        MailMessage email = server.getMessage(0);
        assertEquals("Test", email.getFirstHeaderValue("Subject"));
        assertEquals("Test Body", email.getBody());
    }

    private Properties getMailProperties(int port) {
        Properties mailProps = new Properties();
        mailProps.setProperty("mail.smtp.host", "localhost");
        mailProps.setProperty("mail.smtp.port", "" + port);
        mailProps.setProperty("mail.smtp.sendpartial", "true");
        return mailProps;
    }

    private void sendMessage(int port, String from, String subject, String body, String to) {
        try {
            Properties mailProps = getMailProperties(port);
            Session session = Session.getInstance(mailProps, null);

            MimeMessage msg = createMessage(session, from, to, subject, body);
            __l.debug("Sending message");
            Transport.send(msg);
            __l.debug("Sent message "+msg.getMessageID());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }

    private MimeMessage createMessage(Session session, String from, String to, String subject, String body) throws MessagingException {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setText(body);
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        return msg;
    }
}
