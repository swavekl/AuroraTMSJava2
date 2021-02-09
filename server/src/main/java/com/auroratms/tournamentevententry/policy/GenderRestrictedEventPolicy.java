package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.GenderRestriction;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.EventEntryStatus;

public class GenderRestrictedEventPolicy implements IEventPolicy {
    // player's event
    private GenderRestriction playerGender = GenderRestriction.NONE;

    public GenderRestrictedEventPolicy(String playerGender) {
        if ("MALE".equalsIgnoreCase(playerGender)) {
            this.playerGender = GenderRestriction.MALE;
        } else if ("FEMALE".equalsIgnoreCase(playerGender)) {
            this.playerGender = GenderRestriction.FEMALE;
        }
    }

    @Override
    public boolean isEntryDenied(TournamentEventEntity event) {
        GenderRestriction eventGenderRestriction = event.getGenderRestriction();
        if (!eventGenderRestriction.equals(GenderRestriction.NONE)) {
            return !playerGender.equals(eventGenderRestriction);
        }
        return false;
    }

    @Override
    public EventEntryStatus getStatus() {
        return EventEntryStatus.DISQUALIFIED_GENDER;
    }
}
