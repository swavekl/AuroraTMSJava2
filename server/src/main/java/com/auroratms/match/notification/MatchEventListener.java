package com.auroratms.match.notification;

import com.auroratms.audit.AuditEntity;
import com.auroratms.audit.AuditService;
import com.auroratms.audit.ExcludeProxiedFields;
import com.auroratms.match.Match;
import com.auroratms.match.notification.event.MatchUpdateEvent;
import com.auroratms.match.publish.MatchStatusPublisher;
import com.auroratms.notification.SystemPrincipalExecutor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Date;

/**
 * Listener which updates live score of the match
 */
@Component
@Slf4j
public class MatchEventListener {

    @Autowired
    private MatchStatusPublisher matchStatusPublisher;

    @Autowired
    private AuditService auditService;

    @Async
    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handleEvent(MatchUpdateEvent matchUpdateEvent) {
        // run this task as system principal so we have access to various services
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            @Transactional
            protected void taskBody() {
                processEvent(matchUpdateEvent);
            }
        };
        task.execute();
    }

    /**
     *
     * @param matchUpdateEvent
     */
    private void processEvent(MatchUpdateEvent matchUpdateEvent) {
        Match matchBefore = matchUpdateEvent.getMatchBefore();
        long matchCardId = matchBefore.getMatchCard().getId();
        Match matchAfter = matchUpdateEvent.getMatchAfter();
        log.info("Begin processing match update event in MatchEventListener " + matchCardId + " match # " + matchAfter.getMatchNum());
        try {
            // record who made a change and the score after the change
            makeAuditEntry(matchUpdateEvent.getProfileId(), matchBefore, matchAfter);

            // send match info to update monitor displays
            if (matchAfter.isMatchUmpired()) {
                this.matchStatusPublisher.publishMatchUpdate(matchCardId, matchAfter);
            }
            log.info("Finished processing match update event for match card with id " + matchCardId + " match # " + matchAfter.getMatchNum());
        } catch (Exception e) {
            log.error("Unable to update match status for match # " + matchAfter.getMatchNum() + " on match card with id " + matchCardId, e);
        }
    }

    private void makeAuditEntry(String profileId, Match matchBefore, Match matchAfter) {
        // Record audit of this event to prevent cheating
        log.info("makeAuditEntry for match id " + matchAfter.getId());
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.setExclusionStrategies(new ExcludeProxiedFields());
        Gson gson = builder.create();
        String detailsJSONBefore = gson.toJson(matchBefore, Match.class);
        String detailsJSON = gson.toJson(matchAfter, Match.class);
        // Previous or Next buttons cause save but if there is no change then don't do it.
        if (!StringUtils.equals(detailsJSONBefore, detailsJSON)) {
            log.info("Match changed. Saving audit entry for match id " + matchAfter.getId());
            String eventIdentifier = "%d".formatted(matchAfter.getId());

            AuditEntity auditEntity = new AuditEntity();
            auditEntity.setEventTimestamp(new Date());
            auditEntity.setEventType("MATCH_SCORE");
            auditEntity.setEventIdentifier(eventIdentifier);
            auditEntity.setProfileId(profileId);
            auditEntity.setDetailsJSON(detailsJSON);
            this.auditService.save(auditEntity);
        }
    }
}
