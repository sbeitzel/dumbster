package com.dumbster.smtp;

import com.dumbster.smtp.mailstores.FixedSizeMailStore;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class FixedSizeMailStoreTest extends AbstractMailStoreTest {
    @Before
    @Override
    public void setup() {
        mailStore = new FixedSizeMailStore(10);
    }

    /**
     * The MailStore API doesn't specify what should happen if you
     * call {@link MailStore#getMessage(int)} with an index that doesn't
     * identify a message (e.g. negative or otherwise out of range).
     * The <code>FixedSizeMailStore</code> implementation just returns
     * <code>null</code>.
     */
    @Test
    public void invalidIndexDoesNotThrowException() {
        addAMessage();
        assertNull("Invalid message index should have returned null", mailStore.getMessage(1));
        assertNull("Negative index should have returned null", mailStore.getMessage(-1));
        assertNull("Too large index should have returned null", mailStore.getMessage(20));
    }
}
