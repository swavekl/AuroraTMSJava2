package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.AvailabilityStatus;
import org.junit.Test;

import static org.junit.Assert.*;

public class FullEventPolicyTest {

    @Test
    public void testNotFull() {
        FullEventPolicy policy = new FullEventPolicy();
        TournamentEvent event = new TournamentEvent();
        event.setNumEntries(30);
        event.setMaxEntries(32);
        boolean entryDenied = policy.isEntryDenied(event);
        assertFalse("entry should not be denied", entryDenied);

        assertEquals("status is wrong", AvailabilityStatus.AVAILABLE_FOR_ENTRY, policy.getStatus());
    }

    @Test
    public void testFull() {
        FullEventPolicy policy = new FullEventPolicy();
        TournamentEvent event = new TournamentEvent();
        event.setNumEntries(32);
        event.setMaxEntries(32);
        boolean entryDenied = policy.isEntryDenied(event);
        assertTrue("entry should be denied", entryDenied);

        assertEquals("status is wrong", AvailabilityStatus.EVENT_FULL, policy.getStatus());
    }
}
