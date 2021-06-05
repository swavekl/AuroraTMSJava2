package com.auroratms.tournamentevententry.doubles.notification.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Event to make or break doubles event pairs for a player identified by tournament entry
 */
public class MakeBreakDoublesPairsEvent {

    // players tournament entry
    private long tournamentEntryId;

    // list of entered or withdrawn doubles events
    private List<DoublesEventEntryWithdrawal> entryWithdrawalList = new ArrayList<>();

    public MakeBreakDoublesPairsEvent(long tournamentEntryId) {
        this.tournamentEntryId = tournamentEntryId;
    }

    public long getTournamentEntryId() {
        return tournamentEntryId;
    }

    public void addDeletedDoublesEvent (long eventFk, long eventEntryFk, String partnerProfileId) {
        entryWithdrawalList.add(new DoublesEventEntryWithdrawal(eventFk, eventEntryFk, true));
    }

    public void addEnteredDoublesEvent (long eventFk, long eventEntryFk, String partnerProfileId) {
        entryWithdrawalList.add(new DoublesEventEntryWithdrawal(eventFk, eventEntryFk, false));
    }

    public List<DoublesEventEntryWithdrawal> getEntryWithdrawalList() {
        return entryWithdrawalList;
    }

    /**
     * Class for recording partner entry or withdrawal from doubles event
     */
    public class DoublesEventEntryWithdrawal {
        // doubles event id
        private long eventFk;
        // player's event entry id
        private long eventEntryFk;
        // if true this is a withdrawal, if false this player is still in the event
        private boolean withdrawal;

        public DoublesEventEntryWithdrawal(long eventFk, long eventEntryFk, boolean withdrawal) {
            this.eventFk = eventFk;
            this.eventEntryFk = eventEntryFk;
            this.withdrawal = withdrawal;
        }

        public long getEventFk() {
            return eventFk;
        }

        public long getEventEntryFk() {
            return eventEntryFk;
        }

        public boolean isWithdrawal() {
            return withdrawal;
        }
    }
}
