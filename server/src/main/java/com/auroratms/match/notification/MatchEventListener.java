package com.auroratms.match.notification;

import com.auroratms.match.Match;
import com.auroratms.match.notification.event.MatchUpdateEvent;
import com.auroratms.match.publish.MatchStatusPublisher;
import com.auroratms.notification.SystemPrincipalExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener which updates live score of the match
 */
@Component
@Slf4j
public class MatchEventListener {

    @Autowired
    private MatchStatusPublisher matchStatusPublisher;

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
}
