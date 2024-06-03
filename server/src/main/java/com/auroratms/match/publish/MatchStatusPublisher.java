package com.auroratms.match.publish;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.match.publish.message.MonitorMessage;
import com.auroratms.match.publish.message.MonitorMessageType;
import com.auroratms.server.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    // type of message broker to use
    @Value("${message.broker.type}")
    private String messageBroker;

//    @Autowired
//    private TopicExchange topicExchange;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    public MatchStatusPublisher(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void publishMatchUpdate(long matchCardId, Match match, boolean timeoutStarted, String timeoutRequester, boolean warmupStarted) {
        MatchCard matchCard = matchCardService.getMatchCardWithPlayerProfiles(matchCardId);
        String assignedTables = matchCard.getAssignedTables();
        if (assignedTables != null) {
            String[] tableNumbers = assignedTables.split(",");
            if (tableNumbers.length == 1) {
                try {
                    int tableNumber = Integer.parseInt(tableNumbers[0]);

                    // get tournament id
                    TournamentEvent tournamentEvent = this.tournamentEventEntityService.get(matchCard.getEventFk());
                    long tournamentFk = tournamentEvent.getTournamentFk();
                    MonitorMessage monitorMessage = new MonitorMessage();
                    monitorMessage.setMatch(match);
                    monitorMessage.setNumberOfGames(matchCard.getNumberOfGames());
                    monitorMessage.setPointsPerGame(tournamentEvent.getPointsPerGame());
                    monitorMessage.setDoubles(tournamentEvent.isDoubles());
                    Map<String, String> profileIdToNameMap = matchCard.getProfileIdToNameMap();
                    if (profileIdToNameMap != null) {
                        String playerAName = profileIdToNameMap.get(match.getPlayerAProfileId());
                        monitorMessage.setPlayerAName(playerAName);
                        String playerBName = profileIdToNameMap.get(match.getPlayerBProfileId());
                        monitorMessage.setPlayerBName(playerBName);
                        if (tournamentEvent.isDoubles()) {
                            int index = playerAName.indexOf("/");                
                            if (index != -1) {
                                String playerAPartnerName = playerAName.substring(index + 2);
                                playerAName = playerAName.substring(0, index - 1);
                                monitorMessage.setPlayerAName(playerAName);
                                monitorMessage.setPlayerAPartnerName(playerAPartnerName);
                            }
                            index = playerBName.indexOf("/");                
                            if (index != -1) {
                                String playerBPartnerName = playerBName.substring(index + 2);
                                playerBName = playerBName.substring(0, index - 1);
                                monitorMessage.setPlayerBName(playerBName);
                                monitorMessage.setPlayerBPartnerName(playerBPartnerName);
                            }
                        }
                    }

                    if (warmupStarted) {
                        monitorMessage.setMessageType(MonitorMessageType.WarmupStarted);
                    } else if (timeoutStarted) {
                        monitorMessage.setMessageType(MonitorMessageType.TimeoutStarted);
                        monitorMessage.setTimeoutRequester(timeoutRequester);
                    }
                    this.createTopicAndSend(monitorMessage, tournamentFk, tableNumber);

                } catch (Exception e) {
                    logger.error("Error while sending message to topic ", e);
                }
            }
        }
    }

    private void createTopicAndSend(MonitorMessage monitorMessage, long tournamentFk, int tableNumber) {
        if (this.messageBroker.equals("inmemory")) {
            String destination = String.format("/topic/monitor_%d_%d", tournamentFk, tableNumber);
            if (this.template != null) {
                this.template.convertAndSend(destination, monitorMessage);
            }
        } else if (messageBroker.equals("rabbitmq")) {
            String routingKey = String.format("monitor_%d_%d", tournamentFk, tableNumber);
//            this.rabbitTemplate.convertAndSend(topicExchange.getName(), routingKey, monitorMessage);
            this.rabbitTemplate.convertAndSend(RabbitMQConfig.DEFAULT_TOPIC_EXCHANGE, routingKey, monitorMessage);
        }
    }
}
