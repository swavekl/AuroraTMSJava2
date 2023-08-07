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
        log.info("Begin processing match update event in MatchEventListener " + matchCardId);
        Match matchAfter = matchUpdateEvent.getMatchAfter();
        try {
            // record who made a change and the score after the change
            makeAuditEntry(matchUpdateEvent.getProfileId(), matchCardId, matchAfter);

            // figure out if time out was taken
            boolean sideARequestedTimeout = !matchBefore.isSideATimeoutTaken() && matchAfter.isSideATimeoutTaken();
            boolean sideBRequestedTimeout = !matchBefore.isSideBTimeoutTaken() && matchAfter.isSideBTimeoutTaken();
            boolean timeoutStarted = (sideARequestedTimeout || sideBRequestedTimeout);
            String timeoutRequester = (!timeoutStarted) ? null : (sideARequestedTimeout)
                    ? matchBefore.getPlayerAProfileId() : matchBefore.getPlayerBProfileId();

            // todo - maybe it is better to do this directly via websocket
            boolean warmupStarted = false;

            this.matchStatusPublisher.publishMatchUpdate(matchCardId, matchAfter, timeoutStarted, timeoutRequester, warmupStarted);
            log.info("Finished processing match update event for match card with id " + matchCardId);
        } catch (Exception e) {
            log.error("Unable to update match status for match card with id " + matchCardId, e);
        }
    }

    private void makeAuditEntry(String profileId, long matchCardId, Match matchAfter) {
        // Record audit of this event to prevent cheating
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.setExclusionStrategies(new ExcludeProxiedFields());
        Gson gson = builder.create();
        String detailsJSON = gson.toJson(matchAfter, Match.class);
        String eventIdentifier = String.format("%d", matchAfter.getId());

        AuditEntity auditEntity = new AuditEntity();
        auditEntity.setEventTimestamp(new Date());
        auditEntity.setEventType("MATCH_SCORE");
        auditEntity.setEventIdentifier(eventIdentifier);
        auditEntity.setProfileId(profileId);
        auditEntity.setDetailsJSON(detailsJSON);
        this.auditService.save(auditEntity);
    }
}
