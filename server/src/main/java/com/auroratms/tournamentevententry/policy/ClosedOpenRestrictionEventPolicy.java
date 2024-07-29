package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEvent;
import com.auroratms.tournament.EligibilityRestriction;
import com.auroratms.tournamentevententry.AvailabilityStatus;
import com.auroratms.utils.USRegionsInfo;
import org.apache.commons.lang3.StringUtils;

import static com.auroratms.tournamentevententry.AvailabilityStatus.AVAILABLE_FOR_ENTRY;

public class ClosedOpenRestrictionEventPolicy implements IEventPolicy{

    // state where the venue is located
    private final String tournamentState;

    private final String tournamentCountryCode;

    // state of residence of a player
    private final String playerState;

    private final String playerCountryCode;

    private AvailabilityStatus availabilityStatus = AVAILABLE_FOR_ENTRY;

    public ClosedOpenRestrictionEventPolicy(String tournamentState, String playerState,
                                            String tournamentCountryCode, String playerCountryCode) {
        this.tournamentState = tournamentState;
        this.playerState = playerState;
        this.tournamentCountryCode = tournamentCountryCode;
        this.playerCountryCode = playerCountryCode;
    }

    @Override
    public boolean isEntryDenied(TournamentEvent event) {
        boolean isEntryDenied = false;
        if (event.getEligibilityRestriction() == EligibilityRestriction.CLOSED_STATE) {
            isEntryDenied = !StringUtils.equals(tournamentState, playerState);
            if (isEntryDenied) {
                this.availabilityStatus = AvailabilityStatus.DISQUALIFIED_BY_STATE;
            }
        } else if (event.getEligibilityRestriction() == EligibilityRestriction.CLOSED_REGIONAL) {
            String tournamentRegion = USRegionsInfo.lookupRegionFromState(this.tournamentState);
            String playerRegion = USRegionsInfo.lookupRegionFromState(playerState);
            isEntryDenied = !StringUtils.equals(tournamentRegion, playerRegion);
            if (isEntryDenied) {
                this.availabilityStatus = AvailabilityStatus.DISQUALIFIED_BY_REGION;
            }
        } else if (event.getEligibilityRestriction() == EligibilityRestriction.CLOSED_NATIONAL) {
            isEntryDenied = !StringUtils.equals(tournamentCountryCode, playerCountryCode);
            if (isEntryDenied) {
                this.availabilityStatus = AvailabilityStatus.DISQUALIFIED_BY_NATION;
            }
        }
        return isEntryDenied;
    }

    @Override
    public AvailabilityStatus getStatus() {
        return availabilityStatus;
    }
}
