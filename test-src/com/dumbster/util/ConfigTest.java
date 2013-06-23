package com.dumbster.util;
/**
 * File copyright 8/8/12 by Stephen Beitzel
 */

import junit.framework.Assert;
import org.junit.Test;

/**
 * Unit test to exercise the Config class. Since Config is really a collection of constants and a wrapper
 * for org.apache.commons.configuration.* these are likely not to have a whole lot of meat to them.
 *
 * @author Stephen Beitzel &lt;sbeitzel@pobox.com&gt;
 */
public class ConfigTest {

    @Test
    public void testSetAndGetThreads() {
        int numSMTP = Config.getConfig().getNumSMTPThreads();
        int numPOP = Config.getConfig().getNumPOPThreads();
        
        try {
            Config.getConfig().setNumSMTPThreads(numSMTP + 7);
            Assert.assertEquals("setNumSMTPThreads didn't do what we thought", numSMTP+7, Config.getConfig().getNumSMTPThreads());

            Config.getConfig().setNumPOPThreads(numPOP + 5);
            Assert.assertEquals("setNumPOPThreads didn't do what we thought", numPOP+5, Config.getConfig().getNumPOPThreads());
        } finally {
            // restore the settings
            Config.getConfig().setNumPOPThreads(numPOP);
            Config.getConfig().setNumSMTPThreads(numSMTP);
        }
    }
}
