package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.AvailabilityStatus;
import com.auroratms.tournamentevententry.EventEntryStatus;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SchedulingConflictEventPolicyTest {

    @Test
    public void entryConfirmed_HasConflict () {
        // configure some events
        List<TournamentEvent> events = new ArrayList<>();
        configureEvent(1, 18.5, "U800", events);
        configureEvent(1, 18.5, "U1750", events);
        configureEvent(2, 9.0, "Open Singles", events);

        List<TournamentEventEntry> eventEntries = new ArrayList<>();
        // enter U800 - confirmed
        TournamentEvent u800 = findEvent(events, "U800");
        assertNotNull(u800);
        eventEntries.add(enterEvent(u800, EventEntryStatus.ENTERED));

        // check conflict with U1750 at the same time
        TournamentEvent u1750 = findEvent(events, "U1750");
        assertNotNull(u1750);

        SchedulingConflictEventPolicy policy = new SchedulingConflictEventPolicy(eventEntries, events);
        boolean entryDenied = policy.isEntryDenied(u1750);
        assertTrue("entry was not denied", entryDenied);
        AvailabilityStatus status = policy.getStatus();
        assertEquals("should be scheduling conflict", AvailabilityStatus.SCHEDULING_CONFLICT, status);
    }

    @Test
    public void entryPendingConfirmation_HasConflict () {
        // configure some events
        List<TournamentEvent> events = new ArrayList<>();
        configureEvent(1, 18.5, "U800", events);
        configureEvent(1, 18.5, "U1750", events);
        configureEvent(2, 9.0, "Open Singles", events);

        List<TournamentEventEntry> eventEntries = new ArrayList<>();
        // enter U800 - confirmed
        TournamentEvent u800 = findEvent(events, "U800");
        assertNotNull(u800);
        eventEntries.add(enterEvent(u800, EventEntryStatus.PENDING_CONFIRMATION));

        // check conflict with U1750 at the same time
        TournamentEvent u1750 = findEvent(events, "U1750");
        assertNotNull(u1750);

        SchedulingConflictEventPolicy policy = new SchedulingConflictEventPolicy(eventEntries, events);
        boolean entryDenied = policy.isEntryDenied(u1750);
        assertTrue("entry was not denied", entryDenied);
        AvailabilityStatus status = policy.getStatus();
        assertEquals("should be scheduling conflict", AvailabilityStatus.SCHEDULING_CONFLICT, status);
    }

    @Test
    public void entryPendingDeletion_NoConflict () {
        // configure some events
        List<TournamentEvent> events = new ArrayList<>();
        configureEvent(1, 18.5, "U800", events);
        configureEvent(1, 18.5, "U1750", events);
        configureEvent(2, 9.0, "Open Singles", events);

        List<TournamentEventEntry> eventEntries = new ArrayList<>();
        // enter U800 - confirmed
        TournamentEvent u800 = findEvent(events, "U800");
        assertNotNull(u800);
        eventEntries.add(enterEvent(u800, EventEntryStatus.PENDING_DELETION));

        // check conflict with U1750 at the same time
        TournamentEvent u1750 = findEvent(events, "U1750");
        assertNotNull(u1750);

        SchedulingConflictEventPolicy policy = new SchedulingConflictEventPolicy(eventEntries, events);
        boolean entryDenied = policy.isEntryDenied(u1750);
        assertFalse("entry was denied", entryDenied);
        AvailabilityStatus status = policy.getStatus();
        assertEquals("should be scheduling conflict", AvailabilityStatus.SCHEDULING_CONFLICT, status);
    }

    // ==========================================================================================================
    // helpers
    // ==========================================================================================================
    private void configureEvent(int day, double time, String name, List<TournamentEvent> events) {
        long nextId = events.size() + 1;
        TournamentEvent eventEntity = new TournamentEvent();
        eventEntity.setId(nextId);
        eventEntity.setDay(day);
        eventEntity.setStartTime(time);
        eventEntity.setName(name);
        events.add(eventEntity);
    }

    private TournamentEvent findEvent(List<TournamentEvent> events, String eventName) {
        for (TournamentEvent event : events) {
            if (event.getName().equals(eventName)) {
                return event;
            }
        }
        return null;
    }

    private TournamentEventEntry enterEvent(TournamentEvent eventEntity, EventEntryStatus status) {
        TournamentEventEntry entry = new TournamentEventEntry();
        entry.setId(1L);
        entry.setTournamentEventFk(eventEntity.getId());
        entry.setStatus(status);
        return entry;
    }
}
