package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.EventEntryStatus;
import org.junit.Test;

import static org.junit.Assert.*;

public class FullEventPolicyTest {

    @Test
    public void testNotFull() {
        FullEventPolicy policy = new FullEventPolicy();
        TournamentEventEntity event = new TournamentEventEntity();
        event.setNumEntries(30);
        event.setMaxEntries(32);
        boolean entryDenied = policy.isEntryDenied(event);
        assertFalse("entry should not be denied", entryDenied);

        assertEquals("status is wrong", EventEntryStatus.NOT_ENTERED, policy.getStatus());
    }

    @Test
    public void testFull() {
        FullEventPolicy policy = new FullEventPolicy();
        TournamentEventEntity event = new TournamentEventEntity();
        event.setNumEntries(32);
        event.setMaxEntries(32);
        boolean entryDenied = policy.isEntryDenied(event);
        assertTrue("entry should be denied", entryDenied);

        assertEquals("status is wrong", EventEntryStatus.WAITING_LIST, policy.getStatus());
    }
}
