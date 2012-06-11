package com.dumbster.pop;

import com.dumbster.pop.action.*;
import com.dumbster.smtp.FixedSizeMailStore;
import com.dumbster.smtp.MailStore;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Exercise the enum POPState
 */
public class POPStateTest {
    @Test
    public void testFromString() {
        POPState state = POPState.fromString("AUTHORIZATION");
        Assert.assertEquals("POPState not correctly instantiated from string", POPState.AUTHORIZATION, state);
    }

    @Test
    public void testFromInvalidString() {
        try {
            POPState state = POPState.fromString("unknown");
            Assert.fail("POPState.fromString did not throw exception for invalid state string");
        } catch (IllegalArgumentException e) {
            // this is what should happen
        }
    }

    @Test
    public void validateTransitions() {
        MailStore ms = new FixedSizeMailStore(5);
        // initial state, AUTHORIZATION -- allowed commands: USER, PASS, APOP, QUIT, CAPA
        Request req = Request.createRequest(POPState.AUTHORIZATION, "USER foo");
        assertTransition(req, ms, POPState.AUTHORIZATION);
        req = Request.createRequest(POPState.AUTHORIZATION, "PASS foo");
        assertTransition(req, ms, POPState.TRANSACTION);
        req = Request.createRequest(POPState.AUTHORIZATION, "APOP");
        assertTransition(req, ms, POPState.TRANSACTION);
        req = Request.createRequest(POPState.AUTHORIZATION, "QUIT");
        assertTransition(req, ms, null);
        req = Request.createRequest(POPState.AUTHORIZATION, "CAPA");
        assertTransition(req, ms, POPState.AUTHORIZATION);
        // invalid AUTHORIZATION state commands should not change the state
        req = Request.createRequest(POPState.AUTHORIZATION, "STAT");
        assertTransition(req, ms, POPState.AUTHORIZATION);
        req = Request.createRequest(POPState.AUTHORIZATION, "LIST");
        assertTransition(req, ms, POPState.AUTHORIZATION);
        req = Request.createRequest(POPState.AUTHORIZATION, "RETR 1");
        assertTransition(req, ms, POPState.AUTHORIZATION);
        req = Request.createRequest(POPState.AUTHORIZATION, "DELE 1");
        assertTransition(req, ms, POPState.AUTHORIZATION);
        req = Request.createRequest(POPState.AUTHORIZATION, "NOOP");
        assertTransition(req, ms, POPState.AUTHORIZATION);
        req = Request.createRequest(POPState.AUTHORIZATION, "RSET");
        assertTransition(req, ms, POPState.AUTHORIZATION);
        req = Request.createRequest(POPState.AUTHORIZATION, "UIDL");
        assertTransition(req, ms, POPState.AUTHORIZATION);

        // TRANSACTION state -- allowed commands: CAPA, STAT, LIST, RETR, DELE, NOOP, RSET, UIDL, QUIT
        req = Request.createRequest(POPState.TRANSACTION, "CAPA");
        assertTransition(req, ms, POPState.TRANSACTION);
        req = Request.createRequest(POPState.TRANSACTION, "STAT");
        assertTransition(req, ms, POPState.TRANSACTION);
        req = Request.createRequest(POPState.TRANSACTION, "LIST");
        assertTransition(req, ms, POPState.TRANSACTION);
        req = Request.createRequest(POPState.TRANSACTION, "RETR 1");
        assertTransition(req, ms, POPState.TRANSACTION);
        req = Request.createRequest(POPState.TRANSACTION, "DELE 1");
        assertTransition(req, ms, POPState.TRANSACTION);
        req = Request.createRequest(POPState.TRANSACTION, "NOOP");
        assertTransition(req, ms, POPState.TRANSACTION);
        req = Request.createRequest(POPState.TRANSACTION, "RSET");
        assertTransition(req, ms, POPState.TRANSACTION);
        req = Request.createRequest(POPState.TRANSACTION, "UIDL");
        assertTransition(req, ms, POPState.TRANSACTION);
        req = Request.createRequest(POPState.TRANSACTION, "QUIT");
        assertTransition(req, ms, null);
        // invalid TRANSACTION commands should not change the state, either
        req = Request.createRequest(POPState.TRANSACTION, "USER bar");
        assertTransition(req, ms, POPState.TRANSACTION);
        req = Request.createRequest(POPState.TRANSACTION, "PASS foo");
        assertTransition(req, ms, POPState.TRANSACTION);
        req = Request.createRequest(POPState.TRANSACTION, "APOP");
        assertTransition(req, ms, POPState.TRANSACTION);
    }

    private void assertTransition(Request req, MailStore ms, POPState expected) {
        Response resp = req.execute(ms);
        Assert.assertEquals(expected, resp.getNextState());
    }
}
