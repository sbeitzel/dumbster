package com.dumbster.smtp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class RollingMailStoreTest extends AbstractMailStoreTest {

    @Before
    @Override
    public void setup() {
        mailStore = new RollingMailStore();
    }

    @Test
    public void testGettingMailFromEmptyMailStoreThrowsIndexOutOfBounds() {
        try {
            mailStore.getMessage(0);
            fail("Should have raised exception.");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void testGettingMail0FromMailStoreWithAnItemWorks() {
        addAMessage();
        assertNotNull(mailStore.getMessage(0));
    }

    @Test
    public void testMailRollsOff() {
        MailMessage firstMessage = new MailMessageImpl();
        firstMessage.appendBody("First Post!");
        mailStore.addMessage(firstMessage);

        assertEquals("First Post!", mailStore.getMessage(0).getBody());
        for (int i = 0; i < 100; i++) {
            addAMessage();
        }

        assertEquals(100, mailStore.getEmailCount());
        assertEquals("", mailStore.getMessage(0).getBody());
    }
}
