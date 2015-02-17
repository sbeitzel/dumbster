package com.dumbster.smtp.mailstores;

import com.dumbster.smtp.MailMessage;
import com.dumbster.smtp.MailStore;

/**
 * Do-nothing implementation of MailStore
 */
public class NullMailStore implements MailStore {
    @Override
    public void addMessage(MailMessage message) {
    }

    @Override
    public int getEmailCount() {
        return 0;
    }

    @Override
    public MailMessage[] getMessages() {
        return new MailMessage[0];
    }

    @Override
    public MailMessage getMessage(int index) {
        return null;
    }

    @Override
    public void clearMessages() {
    }

    @Override
    public void deleteMessage(int index) {
    }
}
