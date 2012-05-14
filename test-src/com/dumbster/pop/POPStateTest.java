package com.dumbster.pop;

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
}
