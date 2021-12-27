package com.auroratms.match.notification.event;

import com.auroratms.match.Match;

public class MatchUpdateEvent {

    private Match matchBefore;
    private Match matchAfter;

    public MatchUpdateEvent(Match matchBefore, Match matchAfter) {
        this.matchBefore = matchBefore;
        this.matchAfter = matchAfter;
    }

    public MatchUpdateEvent() {
    }

    public void setMatchBefore(Match matchBefore) {
        this.matchBefore = matchBefore;
    }

    public void setMatchAfter(Match matchAfter) {
        this.matchAfter = matchAfter;
    }

    public Match getMatchBefore() {
        return matchBefore;
    }

    public Match getMatchAfter() {
        return matchAfter;
    }
}
