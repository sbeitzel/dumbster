package com.dumbster.smtp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class AbstractMailStoreTest {
    protected MailStore mailStore;

    /**
     * Implementations must define this method to construct their
     * specific mailstore implementations.
     */
    @Before
    public abstract void setup();

    @Test
    public void testNewMailStoreHasNoMail() {
        assertEquals(0, mailStore.getEmailCount());
    }

    @Test
    public void testAddOneMessageLeavesOneMail() {
        addAMessage();
        assertEquals(1, mailStore.getEmailCount());
    }

    protected void addAMessage() {
        MailMessage message = new MailMessageImpl();
        mailStore.addMessage(message);
    }

    @Test
    public void testNewMailStoreHasEmptyMailList() {
        assertEquals(0, mailStore.getMessages().length);
    }

    @Test
    public void testAddOneMessageLeavesOneMailInMailMessagesArray() {
        addAMessage();
        assertEquals(1, mailStore.getMessages().length);
    }
}
