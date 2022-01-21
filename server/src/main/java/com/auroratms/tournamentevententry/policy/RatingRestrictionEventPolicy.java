package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.AvailabilityStatus;

public class RatingRestrictionEventPolicy implements IEventPolicy {

    private int playerRating = 0;

    private AvailabilityStatus status = AvailabilityStatus.AVAILABLE_FOR_ENTRY;

    public RatingRestrictionEventPolicy(int playerRating) {
        this.playerRating = playerRating;
    }

    @Override
    public boolean isEntryDenied(TournamentEvent event) {
        boolean isDenied = false;
        if (event.getMinPlayerRating() > 0 && event.getMaxPlayerRating() > 0) {
            if (playerRating < event.getMinPlayerRating() || playerRating > event.getMaxPlayerRating()) {
                status = AvailabilityStatus.DISQUALIFIED_BY_RATING;
                isDenied = true;
            }
        } else if (event.getMaxPlayerRating() > 0 && playerRating > event.getMaxPlayerRating()) {
            // most common case
            status = AvailabilityStatus.DISQUALIFIED_BY_RATING;
            isDenied = true;
        } else if (event.getMinPlayerRating() > 0 && playerRating < event.getMinPlayerRating()) {
            status = AvailabilityStatus.DISQUALIFIED_BY_RATING;
            isDenied = true;
        }
        return isDenied;
    }

    @Override
    public AvailabilityStatus getStatus() {
        return status;
    }
}
