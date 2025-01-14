package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.AvailabilityStatus;
import com.auroratms.tournamentevententry.EventEntryStatus;
import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.List;

/**
 * Checks for scheduling conflicts
 */
public class SchedulingConflictEventPolicy implements IEventPolicy {

    // events already entered
    private final List<TournamentEventEntry> eventEntries;

    // all events
    private final List<TournamentEvent> allEvents;

    // minimum time difference in hours to avoid conflict i.e. 1 hour 30 minutes between starting times
    private final static double MIN_TIME_DIFFERENCE = 1.5;

    public SchedulingConflictEventPolicy(List<TournamentEventEntry> eventEntries,
                                         List<TournamentEvent> events) {
        this.eventEntries = eventEntries;
        this.allEvents = events;
    }

    @Override
    public boolean isEntryDenied(TournamentEvent event) {
        boolean isDenied = false;
        // get this event's starting day and time
        double eventStartTime = event.getStartTime();
        int eventDay = event.getDay();

        // see if it there will be a scheduling conflict with other events which player already entered
        for (TournamentEventEntry eventEntry : eventEntries) {
            // don't check this event against itself
            if (eventEntry.getTournamentEventFk() == event.getId()) {
                continue;
            }

            // if about to drop then this event is not blocking the time slot
            EventEntryStatus eventEntryStatus = eventEntry.getStatus();
            if (eventEntryStatus == EventEntryStatus.PENDING_DELETION) {
                continue;
            }

            // otherwise check for conflict
            if(checkForConflict(eventEntry.getTournamentEventFk(), eventDay, eventStartTime)) {
                isDenied = true;
                break;
            }
        }
        return isDenied;
    }

    /**
     * finds event and its configuration time
     * @param enteredEventId id of event that is entered
     * @param dayToCheck day of the event to be checked for conflict
     * @param startTimeToCheck start time of event to be checked for conflict
     * @return true if there is a conflict
     */
    private boolean checkForConflict(long enteredEventId, int dayToCheck, double startTimeToCheck) {
        boolean conflictFound = false;
        for (TournamentEvent event : allEvents) {
            // find entered event definition
            if (event.getId() == enteredEventId) {
                if (event.getDay() == dayToCheck) {
                    double startTime = event.getStartTime();
                    // compute time difference and see if it is less than minimum difference
                    double timeDiff = Math.abs(startTimeToCheck - startTime);
                    if (timeDiff < MIN_TIME_DIFFERENCE) {
                        conflictFound = true;
                        break;
                    }
                }
            }
        }
        return conflictFound;
    }

    @Override
    public AvailabilityStatus getStatus() {
        return AvailabilityStatus.SCHEDULING_CONFLICT;
    }
}
