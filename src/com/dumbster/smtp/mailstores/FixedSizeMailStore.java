package com.dumbster.smtp.mailstores;

import java.util.ArrayList;
import java.util.List;

import com.dumbster.smtp.MailMessage;
import com.dumbster.smtp.MailStore;
import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A mail store with a fixed maximum number of messages it can hold and
 * a FIFO expiration policy. This implementation is intended to be safe
 * for multithreaded access while still being reasonably fast.
 */
public class FixedSizeMailStore implements MailStore {
    private static final Logger __l = LoggerFactory.getLogger(FixedSizeMailStore.class);
    private static final String PROP_MAXSIZE = "dumbster.FixedSizeMailStore.size";

    private final List<MailMessage> _messages = new ArrayList<>();
    private final int _maxSize;

    public FixedSizeMailStore(AbstractConfiguration config) {
        _maxSize = config.getInt(PROP_MAXSIZE);
    }

    public FixedSizeMailStore(int size) {
        _maxSize = size;
    }

    @Override
    public int getEmailCount() {
        synchronized (_messages) {
            return _messages.size();
        }
    }

    @Override
    public void addMessage(MailMessage message) {
        __l.info("Message added - "+message.getFirstHeaderValue("From")+" "+message.getFirstHeaderValue("Subject"));
        // We have two operations, here. First, add the new message to the list; second, remove the other end of the list iff the list is too long
        // Because there might be multiple threads accessing this store, we need to synchronize *all* access to the list. *sigh*
        synchronized (_messages) {
            _messages.add(message);
            final int size = _messages.size();
            if (size > _maxSize) {
                _messages.subList(0, size-_maxSize).clear();
            }
        }
    }

    @Override
    public MailMessage[] getMessages() {
        synchronized (_messages) {
            return _messages.toArray(new MailMessage[_messages.size()]);
        }
    }

    @Override
    public MailMessage getMessage(int index) {
        MailMessage[] messages = getMessages();
        if (index > -1 && index < messages.length) {
            return messages[index];
        }
        return null;
    }

    @Override
    public void clearMessages() {
        synchronized (_messages) {
            _messages.clear();
        }
    }

    @Override
    public void deleteMessage(int index) {
        synchronized (_messages) {
            if (index > -1 && index < _messages.size()) {
                _messages.remove(index);
            }
        }
    }
}
