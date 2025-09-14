package com.auroratms.tableusage.notification;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.match.MatchCardStatus;
import com.auroratms.match.notification.event.MatchUpdateEvent;
import com.auroratms.notification.SystemPrincipalExecutor;
import com.auroratms.tableusage.TableStatus;
import com.auroratms.tableusage.TableUsage;
import com.auroratms.tableusage.TableUsageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;
import java.util.List;

/**
 * Reacts to match card updates, so we know the percentage of completed matches in real time and can update table usage
 */
@Component
@Slf4j
@Transactional(propagation = Propagation.REQUIRES_NEW)  // place @Transactional here to avoid Lazy loading exception when accessing matches collection
public class TableUsageMatchEventListener {

    @Autowired
    private TableUsageService tableUsageService;

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Async
    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handleEvent(MatchUpdateEvent matchUpdateEvent) {
        // run this task as system principal so we have access to various services
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
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
    public void processEvent(MatchUpdateEvent matchUpdateEvent) {
        Match beforeMatch = matchUpdateEvent.getMatchBefore();
        MatchCard matchCard = beforeMatch.getMatchCard();
        long matchCardId = matchCard.getId();
        log.info("Begin processing match update event in TableUsageMatchEventListener " + matchCardId);
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
            List<TableUsage> tableUsageList = tableUsageService.findAllByMatchCardFk(matchCardId);
            for (TableUsage tableUsage : tableUsageList) {
                if (allCompleted) {
                    tableUsage.setTableStatus(TableStatus.Free);
                    tableUsage.setMatchCardFk(0);
                    tableUsage.setMatchStartTime(null);
                    tableUsage.setCompletedMatches((byte) 0);
                    tableUsage.setTotalMatches((byte) 0);
                } else {
                    tableUsage.setCompletedMatches((byte) completedMatches);
                    tableUsage.setTotalMatches((byte) totalMatches);
                }
            }
            tableUsageService.updateAll(tableUsageList);
            log.info("Finished processing match update event in TableUsageMatchEventListener " + matchCardId);
        } catch (Exception e) {
            log.error("Unable to update table usage for match card " + matchCardId, e);
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
            boolean completedOrDefaulted = match.isMatchDoubleDefaulted() ||
                    match.isMatchFinished(numberOfGames, pointsPerGame);
            numCompleted += completedOrDefaulted ? 1 : 0;
        }
        return numCompleted;
    }
}
