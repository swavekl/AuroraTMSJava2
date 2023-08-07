package com.auroratms.match.notification.event;

import com.auroratms.match.Match;

public class MatchUpdateEvent {

    private Match matchBefore;
    private Match matchAfter;

    // profile id of user who made the change
    private String profileId;

    public MatchUpdateEvent(Match matchBefore, Match matchAfter, String profileId) {
        this.matchBefore = matchBefore;
        this.matchAfter = matchAfter;
        this.profileId = profileId;
    }

    public MatchUpdateEvent() {
    }

    public Match getMatchBefore() {
        return matchBefore;
    }

    public Match getMatchAfter() {
        return matchAfter;
    }

    public String getProfileId() {
        return profileId;
    }
}
