package com.dumbster.pop;

import com.dumbster.smtp.*;
import com.dumbster.smtp.mailstores.FixedSizeMailStore;
import com.dumbster.util.Config;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

public class ThreadingTest {
    private static final int PORT_POP = 1082;
    private static final int PORT_SMTP = 1081;

    private POPServer _pserver;
    private SmtpServer _sserver;
    private int _originalPOPThreads;
    private int _originalSMTPThreads;
    private int _originalPOPPort;
    private int _originalSMTPPort;
    private MailStore _originalMailStore;

    @Before
    public void setup() {
        final Config cfg = Config.getConfig();
        _originalPOPThreads = cfg.getNumPOPThreads();
        _originalSMTPThreads = cfg.getNumSMTPThreads();
        _originalPOPPort = cfg.getPOP3Port();
        _originalSMTPPort = cfg.getSMTPPort();
        _originalMailStore = cfg.getMailStore();

        cfg.setNumPOPThreads(2);
        cfg.setPOP3Port(PORT_POP);
        cfg.setMailStore(new FixedSizeMailStore(10));
        cfg.setSMTPPort(PORT_SMTP);
        cfg.setNumSMTPThreads(2);

        _pserver = POPServerFactory.startServer();

        _sserver = SmtpServerFactory.startServer();
    }

    @After
    public void shutdown() {
        _pserver.stop();
        _sserver.stop();
        final Config cfg = Config.getConfig();
        cfg.setNumPOPThreads(_originalPOPThreads);
        cfg.setNumSMTPThreads(_originalSMTPThreads);
        cfg.setPOP3Port(_originalPOPPort);
        cfg.setSMTPPort(_originalSMTPPort);
        cfg.setMailStore(_originalMailStore);
    }

    @Test
    public void checkNonBlockingAccess() throws MessagingException {
        // start up a POP session
        Store pop = connect();
        // start an SMTP session
        Session smtp = connectSMTP();
        Transport smtpTrans = null;
        try {
            // send a message -- if we didn't do the servers right, this could deadlock
            MimeMessage msg = new MimeMessage(smtp);
            msg.setFrom(new InternetAddress("tester@localhost"));
            msg.setSubject("simple message");
            msg.setSentDate(new Date());
            msg.setText("body of a message");
            final String address = "dumbster@localhost";
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(address));
            smtpTrans = smtp.getTransport("smtp");
            smtpTrans.connect("localhost", PORT_SMTP, "dumbster", "");
            smtpTrans.sendMessage(msg, InternetAddress.parse(address));

            // check for message -- if the mail store isn't shared, this won't show any new messages
            Folder inbox = pop.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            int messageCount = inbox.getMessageCount();
            Assert.assertEquals("mail store seems not to be shared", 1, messageCount);

        } finally {
            // close SMTP session -- clean up afterward
            if (smtpTrans != null) {
                smtpTrans.close();
            }
            // close POP session -- clean up afterward
            pop.close();
        }
    }

    private Store connect() throws MessagingException {
        Properties props = new Properties();

        String host = "localhost";
        String username = "userName";
        String password = "mypassword";
        String provider = "pop3";

        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore(provider);
        store.connect(host, PORT_POP, username, password);
        return store;
    }

    private Session connectSMTP() {
        Properties mailProps = new Properties();
        mailProps.setProperty("mail.smtp.host", "localhost");
        mailProps.setProperty("mail.smtp.port", "" + PORT_SMTP);
        mailProps.setProperty("mail.smtp.sendpartial", "true");
        return Session.getDefaultInstance(mailProps, null);
    }
}
