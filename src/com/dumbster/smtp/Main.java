package com.dumbster.smtp;

import com.dumbster.pop.POPServerFactory;
import com.dumbster.util.Config;

public class Main {
    public static void main(String[] args) {
        if (shouldShowHelp(args)) {
            showHelp();
            System.exit(1);
        }
        Config config = Config.getConfig();
        if (!config.getIsValid()) {
            showHelp();
            System.exit(1);
        }

        SmtpServerFactory.startServer();
        POPServerFactory.startServer();
    }

    private static boolean shouldShowHelp(String[] args) {
        if (args.length == 0)
            return false;
        for (String arg : args) {
            if ("--help".equalsIgnoreCase(arg) || "-h".equalsIgnoreCase(arg))
                return true;
        }
        return false;
    }

    private static void showHelp() {
        System.out.println();
        System.out.println("Dumbster Fake SMTP Server");
        System.out.println("usage: java -jar dumbster.jar");
        System.out.println("Starts the SMTP and POP3 servers on the default ports (25 and 110).");
        System.out.println();
        System.out.println("To override defaults, define properties at invocation time or provide a");
        System.out.println("file called 'dumbster.properties' in the classpath.");
        System.out.println("To override logging behavior, provide a 'log4j.properties' file in the classpath.");
    }

}
