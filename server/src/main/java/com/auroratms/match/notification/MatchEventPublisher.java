package com.auroratms.match.notification;

import com.auroratms.match.Match;
import com.auroratms.match.notification.event.MatchUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class MatchEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishMatchEvent(Match matchBefore, Match matchAfter) {
        MatchUpdateEvent matchUpdateEvent = new MatchUpdateEvent(matchBefore, matchAfter);
        this.applicationEventPublisher.publishEvent(matchUpdateEvent);
    }
}
