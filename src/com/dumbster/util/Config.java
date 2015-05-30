package com.dumbster.util;
/**
 * File copyright 8/8/12 by Stephen Beitzel
 */

import java.lang.reflect.Constructor;

import com.dumbster.smtp.MailStore;
import com.dumbster.smtp.mailstores.NullMailStore;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central class to hold all the configuration of the server.
 *
 * @author Stephen Beitzel &lt;sbeitzel@pobox.com&gt;
 */
public class Config {
    private static final Logger __l = LoggerFactory.getLogger(Config.class);

    private static final Config CURRENT_CONFIG = new Config();

    public static final int SERVER_SOCKET_TIMEOUT = 5000;
    public static final int MAX_THREADS = 10;
    public static final int DEFAULT_SMTP_PORT = 25;
    public static final int DEFAULT_POP3_PORT = 110;

    public static final String PROP_NUM_SMTP_THREADS = "dumbster.SMTP.numThreads";
    public static final String PROP_NUM_POP3_THREADS = "dumbster.POP3.numThreads";
    public static final String PROP_SMTP_PORT = "dumbster.SMTP.port";
    public static final String PROP_POP3_PORT = "dumbster.POP3.port";
    public static final String PROP_MAILSTORE_CLASS = "dumbster.mailstore.impl";

    private static final int DEFAULT_THREADS = 1; // as implemented by rjo

    private MailStore _mailStore;
    private CompositeConfiguration _config;
    private boolean _isValid = false;

    private Config() {
        _config = new CompositeConfiguration();
        // Order of adding children is important. The first added is the first polled.
        // The order we want is: System, default file
        _config.addConfiguration(new SystemConfiguration());
        try {
            _config.addConfiguration(new PropertiesConfiguration("dumbster.properties"));
        } catch (ConfigurationException e) {
            __l.warn("dumbster.properties not loaded");
        }

        // create the mail store
        try {
            // first, see if there is a constructor that takes an AbstractConfiguration
            Constructor ctor;
            try {
                ctor = (Class.forName(_config.getString(PROP_MAILSTORE_CLASS))).getDeclaredConstructor(AbstractConfiguration.class);
                ctor.setAccessible(true);
                _mailStore = (MailStore) ctor.newInstance(_config);
            } catch (Exception e) {
                __l.info("Unable to find constructor that takes a configuration; trying no-arg constructor");
                _mailStore = (MailStore) Class.forName(_config.getString(PROP_MAILSTORE_CLASS)).newInstance();
            }
            _isValid = true;
        } catch (Exception e) {
            __l.error("Unable to set up mail store", e);
            _mailStore = new NullMailStore();
        }
    }
    
    public static Config getConfig() { return CURRENT_CONFIG; }

    public boolean getIsValid() {
        return _isValid;
    }

    public int getNumSMTPThreads() {
        int threadCount = _config.getInt(PROP_NUM_SMTP_THREADS, DEFAULT_THREADS);
        threadCount = Math.max(threadCount, 1);
        if (threadCount > MAX_THREADS) {
            threadCount = MAX_THREADS;
        }
        return threadCount;
    }

    public int getNumPOPThreads() {
        int threadCount = _config.getInt(PROP_NUM_POP3_THREADS, DEFAULT_THREADS);
        threadCount = Math.max(threadCount, 1);
        if (threadCount > MAX_THREADS) {
            threadCount = MAX_THREADS;
        }
        return threadCount;
    }
    
    public void setNumSMTPThreads(int count) {
        _config.setProperty(PROP_NUM_SMTP_THREADS, String.valueOf(count));
    }

    public void setNumPOPThreads(int count) {
        _config.setProperty(PROP_NUM_POP3_THREADS, String.valueOf(count));
    }

    public MailStore getMailStore() {
        return _mailStore;
    }

    /**
     * Really so that unit tests can drop in a particular mailstore
     *
     * @param store the store
     */
    public void setMailStore(MailStore store) {
        _mailStore = store;
    }

    public int getSMTPPort() {
        return _config.getInt(PROP_SMTP_PORT, DEFAULT_SMTP_PORT);
    }

    public void setSMTPPort(int port) {
        _config.setProperty(PROP_SMTP_PORT, String.valueOf(port));
    }

    public int getPOP3Port() {
        return _config.getInt(PROP_POP3_PORT, DEFAULT_POP3_PORT);
    }

    public void setPOP3Port(int port) {
        _config.setProperty(PROP_POP3_PORT, String.valueOf(port));
    }
}
