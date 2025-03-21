package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.GenderRestriction;
import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.AvailabilityStatus;

public class GenderRestrictedEventPolicy implements IEventPolicy {
    // player's event
    private GenderRestriction playerGender = GenderRestriction.NONE;

    public GenderRestrictedEventPolicy(String playerGender) {
        toGenderRestriction(playerGender);
    }

    private void toGenderRestriction(String playerGender) {
        if ("MALE".equalsIgnoreCase(playerGender)) {
            this.playerGender = GenderRestriction.MALE;
        } else if ("FEMALE".equalsIgnoreCase(playerGender)) {
            this.playerGender = GenderRestriction.FEMALE;
        }
    }

    @Override
    public boolean isEntryDenied(TournamentEvent event) {
        GenderRestriction eventGenderRestriction = event.getGenderRestriction();
        if (!eventGenderRestriction.equals(GenderRestriction.NONE)) {
            return !playerGender.equals(eventGenderRestriction);
        }
        return false;
    }

    @Override
    public AvailabilityStatus getStatus() {
        return AvailabilityStatus.DISQUALIFIED_BY_GENDER;
    }

    public static boolean isEntryDenied(String playerGender, TournamentEvent tournamentEvent) {
        GenderRestrictedEventPolicy policy = new GenderRestrictedEventPolicy(playerGender);
        return policy.isEntryDenied(tournamentEvent);
    }
}
