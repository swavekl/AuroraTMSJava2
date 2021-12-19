package com.auroratms.match.publish;

import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.match.publish.message.MonitorMessageType;
import com.auroratms.match.publish.message.MonitorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for publishing match status so it can be published to a monitor showing the score
 */
@Service
public class MatchStatusPublisher {

    private final SimpMessagingTemplate template;

    @Autowired
    private MatchCardService matchCardService;

    public MatchStatusPublisher(SimpMessagingTemplate template) {
        this.template = template;
    }

    public MonitorMessage publishMatchUpdate(long matchCardId, Match match, boolean timeoutStarted, String timeoutRequester, boolean warmupStarted) {
        MonitorMessage monitorMessage = new MonitorMessage();
        monitorMessage.setMatch(match);
        MatchCard matchCard = matchCardService.get(matchCardId);
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
            this.template.convertAndSend("/topic/monitor", monitorMessage);
        }

        return monitorMessage;
    }
}
