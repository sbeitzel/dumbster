package com.dumbster.pop.action;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.dumbster.pop.POPState;
import com.dumbster.pop.Response;
import com.dumbster.smtp.MailStore;

public class Connect implements Action {
    @Override
    public String getCommand() {
        return "";
    }

    @Override
    public Response response(POPState popState, MailStore mailStore) {
        if (popState == POPState.AUTHORIZATION) {
            StringBuilder sb = new StringBuilder("Dumbster POP3 server ready ");
            sb.append(getAPOPGreeting());
            sb.append("\r\n");
            return new Response(Response.OK, sb.toString(), popState);
        }
        return new Invalid("").response(popState, mailStore);
    }
    
    public String getAPOPGreeting() {
        StringBuilder sb = new StringBuilder("<");
        // string of the form "<sessionID.timestamp@host>"
        // If we were writing a server that actually implemented multi-mailbox and user security
        // then we'd care more about this. The one thing we should care about is that this string
        // is different for each request.
        sb.append(Thread.currentThread().getName());
        sb.append(".").append(System.currentTimeMillis()).append("@");
        try {
            InetAddress addr = InetAddress.getLocalHost();
            sb.append(addr.getHostName());
        } catch (UnknownHostException ex) {
            sb.append("dumbster");
        }
        sb.append(">");
        return sb.toString();
    }
}
