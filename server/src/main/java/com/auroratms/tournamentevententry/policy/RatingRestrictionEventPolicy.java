package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.EventEntryStatus;

public class RatingRestrictionEventPolicy implements IEventPolicy {

    private int playerRating = 0;

    private EventEntryStatus status = EventEntryStatus.NOT_ENTERED;

    public RatingRestrictionEventPolicy(int playerRating) {
        this.playerRating = playerRating;
    }

    @Override
    public boolean isEntryDenied(TournamentEventEntity event) {
        boolean isDenied = false;
        if (event.getMinPlayerRating() > 0 && event.getMaxPlayerRating() > 0) {
            if (playerRating < event.getMinPlayerRating() || playerRating > event.getMaxPlayerRating()) {
                status = EventEntryStatus.DISQUALIFIED_RATING;
                isDenied = true;
            }
        } else if (event.getMaxPlayerRating() > 0 && playerRating > event.getMaxPlayerRating()) {
            // most common case
            status = EventEntryStatus.DISQUALIFIED_RATING;
            isDenied = true;
        } else if (event.getMinPlayerRating() > 0 && playerRating < event.getMinPlayerRating()) {
            status = EventEntryStatus.DISQUALIFIED_RATING;
            isDenied = true;
        }
        return isDenied;
    }

    @Override
    public EventEntryStatus getStatus() {
        return status;
    }
}
