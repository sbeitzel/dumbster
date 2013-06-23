package com.dumbster.util;
/**
 * File copyright 8/8/12 by Stephen Beitzel
 */

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

/**
 * Central class to hold all the configuration of the server.
 *
 * @author Stephen Beitzel &lt;sbeitzel@pobox.com&gt;
 */
public class Config {
    private static final Config CURRENT_CONFIG = new Config();
    public static final int DEFAULT_SMTP_PORT = 25;
    public static final int SERVER_SOCKET_TIMEOUT = 5000;
    public static final int MAX_THREADS = 10;
    public static final String PROP_NUM_THREADS = "dumbster.numThreads";

    private static final int DEFAULT_THREADS = 1; // as implemented by rjo
    
    private CompositeConfiguration _config;
    private SystemConfiguration _systemConfiguration;

    private Config() {
        _config = new CompositeConfiguration();
        _systemConfiguration = new SystemConfiguration();
        _config.addConfiguration(_systemConfiguration);
        try {
            _config.addConfiguration(new PropertiesConfiguration("dumbster.properties"));
        } catch (ConfigurationException e) {
            System.out.println("dumbster.properties not loaded");
        }
    }
    
    public static Config getConfig() { return CURRENT_CONFIG; }
    
    public int getNumSMTPThreads() {
        int threadCount = _config.getInt(PROP_NUM_THREADS, DEFAULT_THREADS);
        threadCount = Math.max(threadCount, 1);
        if (threadCount > MAX_THREADS) {
            threadCount = MAX_THREADS;
        }
        return threadCount;
    }

    public int getNumPOPThreads() {
        // the initial implementation was to use the same property for both services, so we'll not change that yet.
        return getNumSMTPThreads();
    }
    
    public void setNumSMTPThreads(int count) {
        _systemConfiguration.clearProperty(PROP_NUM_THREADS);
        _systemConfiguration.addProperty(PROP_NUM_THREADS, String.valueOf(count));
    }

    public void setNumPOPThreads(int count) {
        setNumSMTPThreads(count);
    }
}
