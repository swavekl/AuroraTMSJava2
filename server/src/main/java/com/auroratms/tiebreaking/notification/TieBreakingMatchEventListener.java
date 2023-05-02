package com.auroratms.tiebreaking.notification;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.match.MatchCardStatus;
import com.auroratms.match.notification.event.MatchUpdateEvent;
import com.auroratms.notification.SystemPrincipalExecutor;
import com.auroratms.tiebreaking.TieBreakingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * Match update event processor for performing tie breaking, especially when using phone entry
 */
@Component
@Slf4j
@Transactional
public class TieBreakingMatchEventListener {

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private TieBreakingService tieBreakingService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
     * @param matchUpdateEvent
     */
    private void processEvent(MatchUpdateEvent matchUpdateEvent) {
        Match matchBefore = matchUpdateEvent.getMatchBefore();
        long matchCardId = matchBefore.getMatchCard().getId();
        log.info("Begin processing match update event in TieBreakingMatchEventListener for match card " + matchCardId);
        try {
            MatchCard matchCardWithMatches = matchCardService.getMatchCard(matchCardId);
            long eventFk = matchCardWithMatches.getEventFk();
            TournamentEvent tournamentEvent = tournamentEventEntityService.get(eventFk);
            int pointsPerGame = tournamentEvent.getPointsPerGame();
            List<Match> matches = matchCardWithMatches.getMatches();
            int numberOfGames = matchCardWithMatches.getNumberOfGames();
            int completedMatches = getNumCompletedMatches(matches, numberOfGames, pointsPerGame);
            int totalMatches = matches.size();
            boolean allCompleted = (totalMatches == completedMatches);
            boolean updateMatchCard = false;
            if (allCompleted) {
                tieBreakingService.rankAndAdvancePlayers(matchCardId);
            } else {
                // clear player rankings if some match card which was cleared
                if (StringUtils.isNotEmpty(matchCardWithMatches.getPlayerRankings())) {
                    for (Match match : matches) {
                        if(match.getId() == matchBefore.getId()) {
                            if (match.getGamesOnlyResult(numberOfGames, pointsPerGame).isMatchCleared()) {
                                matchCardWithMatches.setPlayerRankings(null);
                                updateMatchCard = true;
                            }
                            break;
                        }
                    }
                }
            }

            if (allCompleted || completedMatches == 0) {
                if (completedMatches == 0) {
                    matchCardWithMatches.setStatus(MatchCardStatus.STARTED);  // in case they clear the whole match
                } else {
                    matchCardWithMatches.setStatus(MatchCardStatus.COMPLETED);
                }
                updateMatchCard = true;
            }
            if (updateMatchCard) {
                matchCardService.save(matchCardWithMatches);
            }

            log.info("Finished processing match update event for match card with id " + matchCardId);
        } catch (Exception e) {
            log.error("Unable to perform tie breaking for match card with id " + matchCardId, e);
        }
    }
    /**
     *
     * @param matches
     * @param numberOfGames
     * @param pointsPerGame
     * @return
     */
    private int getNumCompletedMatches(List<Match> matches, int numberOfGames, int pointsPerGame) {
        int numCompleted = 0;
        for (Match match : matches) {
            numCompleted += match.isMatchFinished(numberOfGames, pointsPerGame) ? 1 : 0;
        }
        return numCompleted;
    }
}
