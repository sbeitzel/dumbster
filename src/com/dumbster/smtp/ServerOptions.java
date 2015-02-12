package com.dumbster.smtp;

import com.dumbster.smtp.mailstores.RollingMailStore;
import com.dumbster.util.Config;

/**
 * User: rj
 * Date: 7/18/13
 * Time: 5:35 AM
 */
public class ServerOptions {
    public int port = Config.DEFAULT_SMTP_PORT;
    public int pop3port = 0;
    public boolean threaded = true;
    public MailStore mailStore = new RollingMailStore();
    public boolean valid = true;

    public ServerOptions() {
    }

    public ServerOptions(String[] args) {
        if (args.length == 0) {
            return;
        }

        for (String argument : args) {
            if (argument.startsWith("--mailStore")) {
                String[] values = argument.split("=");
                if (values.length != 2) {
                    this.valid = false;
                    return;
                }
                try {
                    this.mailStore = (MailStore) Class.forName("com.dumbster.smtp.mailstores."+values[1]).newInstance();
                } catch (Exception e) {
                    this.valid = false;
                    return;
                }
            } else if (argument.startsWith("--threaded")) {
                this.threaded = !argument.equalsIgnoreCase("--threaded=false");
            } else if (argument.startsWith("--pop3")) {
                String[] values = argument.split("=");
                if (values.length != 2) {
                    this.valid = false;
                    return;
                }
                try {
                    this.pop3port = Integer.parseInt(values[1]);
                } catch (Exception e) {
                    this.valid=false;
                    return;
                }
            } else {
                try {
                    this.port = Integer.parseInt(argument);
                } catch (NumberFormatException e) {
                    this.valid = false;
                    break;
                }
            }
        }
    }
}
