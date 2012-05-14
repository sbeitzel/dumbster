package com.dumbster.pop.action;

import com.dumbster.pop.POPState;
import com.dumbster.pop.Response;
import com.dumbster.smtp.MailStore;

public class DeleteMessage implements Action {

    private Integer _messageIndex = null;

    public DeleteMessage(String params) {
        try {
            int mi = Integer.parseInt(params.trim());
            if (mi > 0)
                _messageIndex = Integer.valueOf(mi - 1);
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public String getCommand() {
        return "DELE";
    }

    @Override
    public Response response(POPState popState, MailStore mailStore) {
        if (_messageIndex != null) {
            mailStore.deleteMessage(_messageIndex.intValue());
        }
        return new Response(Response.OK, "message deleted", POPState.TRANSACTION);
    }
}
