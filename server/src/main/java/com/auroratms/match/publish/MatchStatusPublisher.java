package com.auroratms.match.publish;

import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.publish.message.MonitorMessageType;
import com.auroratms.match.publish.message.MonitorMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for publishing match status so it can be published to a monitor showing the score
 */
@Service
public class MatchStatusPublisher {

    private final SimpMessagingTemplate template;

    public MatchStatusPublisher(SimpMessagingTemplate template) {
        this.template = template;
    }

    public MonitorMessage publishMatchUpdate(MatchCard matchCard, Match match, boolean timeoutStarted, String timeoutRequester, boolean warmupStarted) {
        MonitorMessage monitorMessage = new MonitorMessage();
        monitorMessage.setMatch(match);
        monitorMessage.setNumberOfGames(matchCard.getNumberOfGames());


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
