package com.auroratms.tournamententry.notification.event;

import java.util.HashMap;
import java.util.Map;

/**
 * Event for removing orphaned tournament entries for deleted team members
 * so we don't have to wait for once per hour cleanup job
 */
public class TeamMembersTournamentEntriesChangedEvent {

    private Map<Long, Boolean> tournamentEntryIdsToActionMap = new HashMap<>();

    public void addDroppedMember(long tournamentEntryFk) {
        tournamentEntryIdsToActionMap.put(tournamentEntryFk, Boolean.TRUE);
    }

    public void addJoinedMember(long tournamentEntryFk) {
        tournamentEntryIdsToActionMap.put(tournamentEntryFk, Boolean.FALSE);
    }

    public Map<Long, Boolean> getTournamentEntryIdsToActionMap() {
        return tournamentEntryIdsToActionMap;
    }
}
