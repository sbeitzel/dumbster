package com.dumbster.pop;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import java.util.Properties;

import com.dumbster.smtp.MailMessage;
import com.dumbster.smtp.MailMessageImpl;
import com.dumbster.smtp.mailstores.FixedSizeMailStore;
import com.dumbster.util.Config;
import net.jcip.annotations.NotThreadSafe;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for the POP API to dumbster.
 */
@NotThreadSafe
public class POPServerTest {
    private static final int PORT = 1082;

    private static POPServer _server;
    
    @BeforeClass
    public static void setup() {
        Config config = Config.getConfig();
        config.setPOP3Port(PORT);
        config.setMailStore(new FixedSizeMailStore(10));
        _server = POPServerFactory.startServer();
    }

    @AfterClass
    public static void shutdown() {
        _server.stop();
    }

    @Test
    public void loginAndQuit() throws Exception {
        Store store = connect(false);
    
        Folder inbox = store.getFolder("INBOX");
        if (inbox == null) {
          Assert.fail("POP should have an inbox");
        }
        inbox.open(Folder.READ_ONLY);
    
        inbox.close(false);
        store.close();

    }
    
    @Test
    public void testAPOP() throws Exception {
        Store store = connect(true);
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
        Store store = connect(false);
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
        Store store = connect(false);
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
    public void testUIDLSingle() throws Exception {
        _server.getMailstore().addMessage(createMessage("recipient@destination.net",
                "tester@sender.org",
                "test message",
                "Message body with multiple\r\nlines of text.\r\n.6 percent\r\n"));
        _server.getMailstore().addMessage(createMessage("recipient@destination.net",
                "tester@sender.org",
                "second test message",
                "More email!\r\n"));
        Store store = connect(false);
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        Message[] messages = inbox.getMessages();
        com.sun.mail.pop3.POP3Folder pf =
                    (com.sun.mail.pop3.POP3Folder)inbox;
        pf.getUID(messages[0]);

        inbox.close(true);
        store.close();
        _server.getMailstore().clearMessages();
    }

    @Test
    public void testUIDLAll() throws Exception {
        _server.getMailstore().addMessage(createMessage("recipient@destination.net",
                "tester@sender.org",
                "test message",
                "Message body with multiple\r\nlines of text.\r\n.6 percent\r\n"));
        _server.getMailstore().addMessage(createMessage("recipient@destination.net",
                "tester@sender.org",
                "second test message",
                "More email!\r\n"));
        Store store = connect(false);
        Folder inbox = store.getFolder("INBOX");

        com.sun.mail.pop3.POP3Folder pf =
                (com.sun.mail.pop3.POP3Folder)inbox;

        pf.open(Folder.READ_WRITE);
        FetchProfile fp = new FetchProfile();
        fp.add(UIDFolder.FetchProfileItem.UID);
        pf.fetch(pf.getMessages(), fp);

        inbox.close(true);
        store.close();
        _server.getMailstore().clearMessages();
    }

    @Test
    public void testList() throws Exception {
        _server.getMailstore().addMessage(createMessage("recipient@destination.net",
                "tester@sender.org",
                "test message",
                "Message body with multiple\r\nlines of text.\r\n.6 percent\r\n"));
        _server.getMailstore().addMessage(createMessage("recipient@destination.net",
                "tester@sender.org",
                "second test message",
                "More email!\r\n"));
        Store store = connect(false);
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        com.sun.mail.pop3.POP3Folder pf = (com.sun.mail.pop3.POP3Folder)inbox;

        int [] sizes = pf.getSizes();
        Assert.assertEquals("LIST did not return the expected number of records", 2, sizes.length);

        inbox.close(true);
        store.close();
        _server.getMailstore().clearMessages();
    }
    
    private Store connect(boolean useApop) throws MessagingException {
        Properties props = new Properties();
        props.setProperty("mail.pop3.apop.enable", String.valueOf(useApop));

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
