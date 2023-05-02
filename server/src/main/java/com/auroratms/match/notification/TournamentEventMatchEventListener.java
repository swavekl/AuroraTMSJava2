package com.auroratms.match.notification;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.match.MatchService;
import com.auroratms.match.notification.event.MatchUpdateEvent;
import com.auroratms.notification.SystemPrincipalExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * Listener which marks the tournament event as one which has match scores entered
 */
@Component
@Slf4j
@Transactional
public class TournamentEventMatchEventListener {

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private MatchService matchService;

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
        log.info("Begin processing match update event in TournamentEventMatchEventListener for match card " + matchCardId);

        try {
            MatchCard matchCard = matchCardService.getMatchCard(matchCardId);
            long eventFk = matchCard.getEventFk();
            TournamentEvent tournamentEvent = tournamentEventEntityService.get(eventFk);
            // get current status
            boolean matchScoresEntered = tournamentEvent.isMatchScoresEntered();
            boolean oldMatchScoresEntered = matchScoresEntered;

            Match matchAfter = matchUpdateEvent.getMatchAfter();
            boolean matchFinished = matchAfter.isMatchFinished(tournamentEvent.getNumberOfGames(), tournamentEvent.getPointsPerGame());
            boolean updateEvent = false;
            if (!matchFinished) {
                List<MatchCard> allMatchCards = matchCardService.findAllForEvent(eventFk);
                List<Match> allMatchesForEvent = matchService.findAllByMatchCardIn(allMatchCards);
                log.info("Got " + allMatchesForEvent.size() + " matches for " + allMatchCards.size() + " match cards");
                int countEnteredMatches = 0;
                for (Match match : allMatchesForEvent) {
                    if (match.isMatchFinished(tournamentEvent.getNumberOfGames(),tournamentEvent.getPointsPerGame())) {
                        countEnteredMatches++;
                    }
                }
                matchScoresEntered = countEnteredMatches > 0;
                if (countEnteredMatches < allMatchesForEvent.size()) {
                    if (tournamentEvent.getConfiguration().getFinalPlayerRankings() != null) {
                        log.info("Clearing final player rankings");
                        tournamentEvent.getConfiguration().setFinalPlayerRankings(null);
                        updateEvent = true;
                    }
                }
            } else {
                // match finished - so at least one match was entered so we can't redo the draws without asking
                matchScoresEntered = true;
            }

            log.info("oldMatchScoresEntered = " + oldMatchScoresEntered + ", matchScoresEntered = " + matchScoresEntered);
            if (oldMatchScoresEntered != matchScoresEntered) {
                log.info ("Updating matchScores entered for event to " + matchScoresEntered);
                tournamentEvent.setMatchScoresEntered(matchScoresEntered);
                updateEvent = true;
            }
            if (updateEvent) {
                tournamentEventEntityService.update(tournamentEvent);
            }

            log.info("Finished processing match update event for match card with id " + matchCardId);
        } catch (Exception e) {
            log.error("Unable to update tournament event status for match card with id " + matchCardId, e);
        }
    }

    /**
     * Checks if any match card from this list has any match that is entered
     * @param matchCards
     * @param numberOfGames
     * @param pointsPerGame
     * @return
     */
    private boolean isAnyMatchCardEntered(List<MatchCard> matchCards, int numberOfGames, int pointsPerGame) {
        boolean anyMatchCardEntered = false;
        for (MatchCard matchCard : matchCards) {
            List<Match> matches = matchCard.getMatches();
            for (Match match : matches) {
                anyMatchCardEntered = anyMatchCardEntered || match.isMatchFinished(numberOfGames, pointsPerGame);
            }
            if (anyMatchCardEntered) {
                break;
            }
        }

        return anyMatchCardEntered;
    }
}
