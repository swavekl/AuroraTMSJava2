package com.auroratms.match.publish;

import com.auroratms.draw.DrawType;
import com.auroratms.draw.notification.DrawsEventListener;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.match.publish.message.MonitorMessageType;
import com.auroratms.match.publish.message.MonitorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for publishing match status so it can be published to a monitor showing the score
 */
@Service
public class MatchStatusPublisher {

    private static final Logger logger = LoggerFactory.getLogger(MatchStatusPublisher.class);

    private final SimpMessagingTemplate template;

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    public MatchStatusPublisher(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void publishMatchUpdate(long matchCardId, Match match, boolean timeoutStarted, String timeoutRequester, boolean warmupStarted) {
        MatchCard matchCard = matchCardService.get(matchCardId);
        String assignedTables = matchCard.getAssignedTables();
        String[] tableNumbers = assignedTables.split(",");
        if (tableNumbers.length == 1) {
            try {
                int tableNumber = Integer.parseInt(tableNumbers[0]);

                // get tournament id
                TournamentEventEntity tournamentEventEntity = this.tournamentEventEntityService.get(matchCard.getEventFk());
                long tournamentFk = tournamentEventEntity.getTournamentFk();
                String destination = String.format("/topic/monitor/%d/%d", tournamentFk, tableNumber);

                MonitorMessage monitorMessage = new MonitorMessage();
                monitorMessage.setMatch(match);
                monitorMessage.setNumberOfGames(matchCard.getNumberOfGames());
                Map<String, String> profileIdToNameMap = matchCard.getProfileIdToNameMap();
                if (profileIdToNameMap != null) {
                    String playerAName = profileIdToNameMap.get(match.getPlayerAProfileId());
                    monitorMessage.setPlayerAName(playerAName);
                    String playerBName = profileIdToNameMap.get(match.getPlayerBProfileId());
                    monitorMessage.setPlayerBName(playerBName);
                }

                if (warmupStarted) {
                    monitorMessage.setMessageType(MonitorMessageType.WarmupStarted);
                } else if (timeoutStarted) {
                    monitorMessage.setMessageType(MonitorMessageType.TimeoutStarted);
                    monitorMessage.setTimeoutRequester(timeoutRequester);
                }

                if (this.template != null) {
                    this.template.convertAndSend(destination, monitorMessage);
                }
            } catch (NumberFormatException e) {
                logger.error("Unable to get table number to send to ", e);
            }
        }
    }
}
