package com.dumbster.smtp;

import java.util.Iterator;
import java.util.UUID;

/**
 * Abstract base mail message class that implements the required POP3 behaviors
 *
 * @author Stephen Beitzel &lt;sbeitzel@pobox.com&gt;
 */
public abstract class AbstractMailMessage implements MailMessage {
    private final UUID uuid = UUID.randomUUID();

    @Override
    public String getUID() {
        return uuid.toString();
    }

    @Override
    public String byteStuff() {
        StringBuilder sb = new StringBuilder();
        Iterator<String> names = getHeaderNames();
        while (names.hasNext()) {
            String name = names.next();
            sb.append(name).append(": ");
            String [] values = getHeaderValues(name);
            for (int i=0; i<values.length; i++) {
                sb.append(values[i]);
                if (i<values.length-1) {
                    sb.append(", ");
                }
            }
            sb.append("\r\n");
        }
        sb.append("\r\n");
        String body = getBody();

        // headers are done, now it's time to build the body.
//        POP3 says we're supposed to "byte stuff" any termination sequence (CRLF.CRLF) that appears in the message
//        but when we do that then Apple's Mail doesn't un-stuff the dots. It may be that Mail is broken, but
//        since that's what I'm using on my test system, I'm not bothered. I would LOVE if someone could point
//        me to a comprehensible explanation of how this is really supposed to work.
        sb.append(body.replaceAll("\\r\\n\\.(.)", "\r\n..$1"));
        // finally, the termination sequence
        sb.append("\r\n.\r\n");
        return sb.toString();
    }

}
