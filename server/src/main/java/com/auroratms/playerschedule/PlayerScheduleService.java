package com.auroratms.playerschedule;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawService;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.playerschedule.model.PlayerDetail;
import com.auroratms.playerschedule.model.PlayerScheduleItem;
import com.auroratms.status.PlayerStatus;
import com.auroratms.status.PlayerStatusService;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Service for retrieving schedule and status information for one player
 */
@Service
@Transactional
public class PlayerScheduleService {

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @Autowired
    private DrawService drawService;

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private PlayerStatusService playerStatusService;

    /**
     * Gets player schedule for all days of the tournament he/she entered
     *
     * @param tournamentEntryId tournament entry id
     * @param playerProfileId   player profile id
     * @return
     */
    public List<PlayerScheduleItem> getPlayerSchedule(Long tournamentEntryId,
                                                      String playerProfileId) {

        // get this player's entry and its event entries
        List<TournamentEventEntry> tournamentEventEntries = this.tournamentEventEntryService.listAllForTournamentEntry(tournamentEntryId);
        List<Long> enteredEventIds = new ArrayList<>(tournamentEventEntries.size());
        for (TournamentEventEntry tournamentEventEntry : tournamentEventEntries) {
            enteredEventIds.add(tournamentEventEntry.getTournamentEventFk());
        }

        // get entered events so we can get their names
        List<TournamentEventEntity> enteredEventEntities = tournamentEventEntityService.findAllById(enteredEventIds);

        // get all draws for this player so we know which groups he/she is in each event
        // in round robin and single elimination rounds
        List<DrawItem> drawItems = drawService.listByProfileIdAndEventFkIn(playerProfileId, enteredEventIds);
        List<PlayerScheduleItem> playerScheduleItems = new ArrayList<>(drawItems.size());
        for (DrawItem drawItem : drawItems) {
            MatchCard matchCard = this.matchCardService.getMatchCard(drawItem.getEventFk(), drawItem.getRound(), drawItem.getGroupNum());
            for (TournamentEventEntity eventEntity : enteredEventEntities) {
                if (eventEntity.getId().equals(matchCard.getEventFk())) {
                    PlayerScheduleItem playerScheduleItem = toPlayerScheduleItem(matchCard, eventEntity.getName(),
                            false, eventEntity.getTournamentFk(), eventEntity.getDay());
                    playerScheduleItems.add(playerScheduleItem);
                }
            }
        }

        // sort by day and time within day
        playerScheduleItems.sort(new Comparator<PlayerScheduleItem>() {
            @Override
            public int compare(PlayerScheduleItem o1, PlayerScheduleItem o2) {
                if (o1.getDay() == o2.getDay()) {
                    return Double.compare(o1.getStartTime(), o2.getStartTime());
                } else {
                    return Integer.compare(o1.getDay(), o2.getDay());
                }
            }
        });

        return playerScheduleItems;
    }

    /**
     * Gets all scheduling and status details for one event or match specified by matchcardid
     *
     * @param matchCardId match card id
     * @return
     */
    public PlayerScheduleItem getPlayerSchedule(Long matchCardId) {
        MatchCard matchCard = this.matchCardService.get(matchCardId);
        TournamentEventEntity eventEntity = this.tournamentEventEntityService.get(matchCard.getEventFk());
        return toPlayerScheduleItem(matchCard, eventEntity.getName(),
                true, eventEntity.getTournamentFk(), eventEntity.getDay());
    }

    /**
     * Converts match card into PlayerScheduleItem and optionally fills in all player details
     *
     * @param matchCard
     * @param eventName
     * @param fetchDetails
     * @param tournamentId
     * @param tournamentDay
     * @return
     */
    private PlayerScheduleItem toPlayerScheduleItem(MatchCard matchCard,
                                                    String eventName,
                                                    boolean fetchDetails,
                                                    long tournamentId,
                                                    int tournamentDay) {
        PlayerScheduleItem playerScheduleItem = new PlayerScheduleItem();

        playerScheduleItem.setEventName(eventName);
        playerScheduleItem.setMatchCardId(matchCard.getId());
        playerScheduleItem.setEventId(matchCard.getEventFk());
        playerScheduleItem.setAssignedTables(matchCard.getAssignedTables());
        playerScheduleItem.setDay(matchCard.getDay());
        playerScheduleItem.setGroup(matchCard.getGroupNum());
        playerScheduleItem.setRound(matchCard.getRound());
        playerScheduleItem.setStartTime(matchCard.getStartTime());
        if (fetchDetails) {
            List<PlayerDetail> playerDetails = makePlayerDetails(matchCard, tournamentId, tournamentDay);
            playerScheduleItem.setPlayerDetails(playerDetails);
        }
        return playerScheduleItem;
    }

    /**
     * Makes player details showing each player status and other information
     *
     * @param matchCard     match card
     * @param tournamentId  tournament id
     * @param tournamentDay tournament day
     * @return player details
     */
    private List<PlayerDetail> makePlayerDetails(MatchCard matchCard, long tournamentId, int tournamentDay) {
        List<PlayerDetail> playerDetailsList = new ArrayList<>();
        Map<String, String> profileIdToNameMap = matchCard.getProfileIdToNameMap();
        List<Match> matches = matchCard.getMatches();
        List<String> playerProfileIds = new ArrayList<>(profileIdToNameMap.keySet());
        List<PlayerStatus> playerStatuses = this.playerStatusService.listPlayersByIds(playerProfileIds, tournamentId, tournamentDay);
        for (Map.Entry<String, String> entry : profileIdToNameMap.entrySet()) {
            String playerProfileId = entry.getKey();
            String playerName = entry.getValue();
            PlayerDetail playerDetail = new PlayerDetail();
            playerDetailsList.add(playerDetail);
            playerDetail.setPlayerFullName(playerName);
            for (PlayerStatus playerStatus : playerStatuses) {
                if (playerStatus.getPlayerProfileId().equals(playerProfileId)) {
                    playerDetail.setStatusCode(playerStatus.getEventStatusCode());
                    playerDetail.setEstimatedArrivalTime(playerStatus.getEstimatedArrivalTime());
                    break;
                }
            }
            for (Match match : matches) {
                if (match.getPlayerAProfileId().equals(playerProfileId)) {
                    playerDetail.setPlayerCode(match.getPlayerALetter());
                    playerDetail.setRating(match.getPlayerARating());
                    playerDetail.setEstimated(false);
                    break;
                } else if (match.getPlayerBProfileId().equals(playerProfileId)) {
                    playerDetail.setPlayerCode(match.getPlayerBLetter());
                    playerDetail.setRating(match.getPlayerBRating());
                    playerDetail.setEstimated(false);
                    break;
                }
            }
        }

        // sort by code A, B, C
        playerDetailsList.sort(new Comparator<PlayerDetail>() {
            @Override
            public int compare(PlayerDetail o1, PlayerDetail o2) {
                return Character.compare(o1.getPlayerCode(), o2.getPlayerCode());
            }
        });

        return playerDetailsList;
    }
}
