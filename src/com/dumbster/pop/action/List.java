package com.dumbster.pop.action;

import com.dumbster.pop.POPState;
import com.dumbster.pop.Response;
import com.dumbster.smtp.MailMessage;
import com.dumbster.smtp.MailStore;

public class List implements Action{
    private Integer _message;
    
    public List(String argument) {
        try {
            _message = Integer.valueOf(argument.trim());
        } catch (Exception e) {
            _message = null;
        }
    }
    
    @Override
    public String getCommand() {
        return "LIST";
    }

    @Override
    public Response response(POPState popState, MailStore mailStore) {
        if (_message != null) {
            // construct a "scan listing" for that message
            MailMessage msg = mailStore.getMessage(_message.intValue() - 1);
            if (msg != null) {
                return new Response(Response.OK, scanListing(_message.intValue(), msg), POPState.TRANSACTION);
            } else {
                return new Response(Response.ERROR, "No such message", POPState.TRANSACTION);
            }
        } else {
            // construct a "scan listing" for each message
            MailMessage [] messages = mailStore.getMessages();
            StringBuilder sb = new StringBuilder();
            sb.append(messages.length).append(" messages\r\n");
            for (int ix = 0; ix < messages.length; ix++) {
                MailMessage msg = messages[ix];
                sb.append(scanListing(ix+1, msg));
            }
            sb.append(".\r\n");
            return new Response(Response.OK, sb.toString(), POPState.TRANSACTION);
        }
    }
    
    private String scanListing(int messageID, MailMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(messageID).append(" ");
        sb.append(msg.toString().getBytes().length).append("\r\n");
        return sb.toString();
    }
}
