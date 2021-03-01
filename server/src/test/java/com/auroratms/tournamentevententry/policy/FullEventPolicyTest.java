package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEventEntity;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FullEventPolicyTest {

    @Test
    public void testNotFull() {
        FullEventPolicy policy = new FullEventPolicy();
        TournamentEventEntity event = new TournamentEventEntity();
        event.setNumEntries(30);
        event.setMaxEntries(32);
        boolean entryDenied = policy.isEntryDenied(event);
        assertFalse("entry should not be denied", entryDenied);
    }

    @Test
    public void testFull() {
        FullEventPolicy policy = new FullEventPolicy();
        TournamentEventEntity event = new TournamentEventEntity();
        event.setNumEntries(32);
        event.setMaxEntries(32);
        boolean entryDenied = policy.isEntryDenied(event);
        assertTrue("entry should be denied", entryDenied);
    }
}
