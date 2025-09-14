package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.AvailabilityStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FullEventPolicyTest {

    @Test
    public void testNotFull() {
        FullEventPolicy policy = new FullEventPolicy();
        TournamentEvent event = new TournamentEvent();
        event.setNumEntries(30);
        event.setMaxEntries(32);
        boolean entryDenied = policy.isEntryDenied(event);
        assertFalse(entryDenied, "entry should not be denied");

        assertEquals(AvailabilityStatus.AVAILABLE_FOR_ENTRY, policy.getStatus(), "status is wrong");
    }

    @Test
    public void testFull() {
        FullEventPolicy policy = new FullEventPolicy();
        TournamentEvent event = new TournamentEvent();
        event.setNumEntries(32);
        event.setMaxEntries(32);
        boolean entryDenied = policy.isEntryDenied(event);
        assertTrue(entryDenied, "entry should be denied");

        assertEquals(AvailabilityStatus.EVENT_FULL, policy.getStatus(), "status is wrong");
    }
}
