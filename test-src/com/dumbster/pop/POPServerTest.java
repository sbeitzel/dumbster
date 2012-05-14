package com.dumbster.pop;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

import com.dumbster.smtp.FixedSizeMailStore;
import com.dumbster.smtp.MailMessage;
import com.dumbster.smtp.MailMessageImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the POP API to dumbster.
 */
public class POPServerTest {
    private static final int PORT = 1082;

    private POPServer _server;
    
    @Before
    public void setup() {
        _server = POPServerFactory.startServer(PORT);
        _server.setMailStore(new FixedSizeMailStore(10));
    }

    @After
    public void shutdown() {
        _server.stop();
    }

    @Test
    public void loginAndQuit() throws Exception {
        Store store = connect();
    
        Folder inbox = store.getFolder("INBOX");
        if (inbox == null) {
          Assert.fail("POP should have an inbox");
        }
        inbox.open(Folder.READ_ONLY);
    
        inbox.close(false);
        store.close();

    }

    @Test
    public void retrieveAndDelete() throws Exception {
        _server.getMailstore().addMessage(createMessage("recipient@destination.net",
                                                        "tester@sender.org",
                                                        "test message",
                                                        "Message body with multiple\r\nlines of text.\r\n.6 percent\r\n"));
        Store store = connect();
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        int messageCount = inbox.getMessageCount();
        Message[] messages = inbox.getMessages();
        Assert.assertEquals(messageCount, messages.length);
        Assert.assertEquals(1, messageCount);
        
        messages[0].getContent();
        messages[0].setFlag(Flags.Flag.DELETED, true);
        inbox.close(true);
        store.close();
        Assert.assertEquals(0, _server.getMailstore().getEmailCount());
    }
    
    @Test
    public void validateByteStuffing() throws Exception {
        _server.getMailstore().addMessage(createMessage("recipient@destination.net",
                                                        "tester@sender.org",
                                                        "test message",
                                                        "Message body with multiple\r\nlines of text.\r\n.6 percent\r\n"));
        Store store = connect();
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        int messageCount = inbox.getMessageCount();
        Message[] messages = inbox.getMessages();
        Assert.assertEquals(messageCount, messages.length);
        Assert.assertEquals(1, messageCount);
        
        Object body = messages[0].getContent();
        String msgStr = body.toString();
        int index = msgStr.indexOf("\r\n.6 percent");
        if (index < 0) {
            Assert.fail("incorrect stuffing of a period at the beginning of a line");
        }
        inbox.close(true);
        store.close();
        _server.getMailstore().clearMessages();
    }

    @Test
    public void testUIDL() throws Exception {
        Assert.fail("Not implemented");
    }

    @Test
    public void testList() throws Exception {
        Assert.fail("Not implemented");
    }
    
    private Store connect() throws MessagingException {
        Properties props = new Properties();

        String host = "localhost";
        String username = "userName";
        String password = "mypassword";
        String provider = "pop3";

        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore(provider);
        store.connect(host, PORT, username, password);
        return store;
    }

    MailMessage createMessage(String to, String from, String subject, String body) {
        MailMessage msg = new MailMessageImpl();
        msg.addHeader("To", to);
        msg.addHeader("From", from);
        msg.addHeader("Subject", subject);
        msg.appendBody(body);
        return msg;
    }
}
