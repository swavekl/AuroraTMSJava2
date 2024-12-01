package com.auroratms.playerschedule;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawService;
import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.playerschedule.model.PlayerDetail;
import com.auroratms.playerschedule.model.PlayerScheduleItem;
import com.auroratms.playerschedule.model.ScheduleItemStatus;
import com.auroratms.status.PlayerStatus;
import com.auroratms.status.PlayerStatusService;
import com.auroratms.tableusage.TableStatus;
import com.auroratms.tableusage.TableUsage;
import com.auroratms.tableusage.TableUsageService;
import com.auroratms.tournament.CheckInType;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.*;

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

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TableUsageService tableUsageService;

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
        List<TournamentEvent> enteredEventEntities = tournamentEventEntityService.findAllById(enteredEventIds);

        // get all draws for this player so we know which groups he/she is in each event
        // in round robin and single elimination rounds
        List<DrawItem> drawItems = drawService.listByProfileIdAndEventFkIn(playerProfileId, enteredEventIds);
        List<PlayerScheduleItem> playerScheduleItems = new ArrayList<>(drawItems.size());
        for (DrawItem drawItem : drawItems) {
//            String message = String.format("Getting match card for (%d, %d, %d)", drawItem.getEventFk(), drawItem.getRound(), drawItem.getGroupNum());
//            System.out.println(message);
            if (drawItem.getDrawType() == DrawType.ROUND_ROBIN) {
                // player who gets a bye in the single elimination round doesn't have a match card
                if (this.matchCardService.existsMatchCard(drawItem.getEventFk(), drawItem.getRound(), drawItem.getGroupNum())) {
                    MatchCard matchCard = this.matchCardService.getMatchCard(drawItem.getEventFk(), drawItem.getRound(), drawItem.getGroupNum());
                    for (TournamentEvent eventEntity : enteredEventEntities) {
                        if (eventEntity.getId().equals(matchCard.getEventFk())) {
                            PlayerScheduleItem playerScheduleItem = toPlayerScheduleItem(matchCard, eventEntity,
                                    false);
                            determineScheduleItemStatus(matchCard, eventEntity, playerScheduleItem);
                            playerScheduleItems.add(playerScheduleItem);
                            break;
                        }
                    }
                }
            } else {
                // single elimination - need to look at match details
                List<MatchCard> singleEliminationMatchCards = matchCardService.findAllForEventAndDrawTypeAndRound(drawItem.getEventFk(), drawItem.getDrawType(), drawItem.getRound());
                boolean found = false;
                for (MatchCard matchCard : singleEliminationMatchCards) {
                    List<Match> matches = matchCard.getMatches();
                    for (Match match : matches) {
                        if (match.getPlayerAProfileId().contains(playerProfileId) ||
                                match.getPlayerBProfileId().contains(playerProfileId)) {
                            for (TournamentEvent eventEntity : enteredEventEntities) {
                                if (eventEntity.getId().equals(matchCard.getEventFk())) {
                                    PlayerScheduleItem playerScheduleItem = toPlayerScheduleItem(matchCard, eventEntity,
                                            false);
                                    determineScheduleItemStatus(matchCard, eventEntity, playerScheduleItem);
                                    playerScheduleItems.add(playerScheduleItem);
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (found) {
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
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
     * Calculates the status of a given match
     *
     * @param matchCard
     * @param tournamentEvent
     * @param playerScheduleItem
     */
    private void determineScheduleItemStatus(MatchCard matchCard, TournamentEvent tournamentEvent, PlayerScheduleItem playerScheduleItem) {
        final String TBD_PROFILE_ID = "TBD";

        boolean allPlayersDetermined = true;
        List<Match> matches = matchCard.getMatches();
        boolean anyScoreEntered = false;
        for (Match match : matches) {
            // check if all players are determined
            if (match.getPlayerAProfileId().equals(TBD_PROFILE_ID) ||
                    match.getPlayerBProfileId().equals(TBD_PROFILE_ID)) {
                allPlayersDetermined = false;
            }

            // check if any game scores were entered
            if (match.getGame1ScoreSideA() != 0 || match.getGame1ScoreSideB() != 0) {
                anyScoreEntered = true;
            }
        }

        ScheduleItemStatus scheduleItemStatus = ScheduleItemStatus.NotReady;
        boolean rankingsDetermined = !StringUtils.isEmpty(matchCard.getPlayerRankings());
        if (rankingsDetermined) {
            scheduleItemStatus = ScheduleItemStatus.Completed;
        } else if (anyScoreEntered) {
            scheduleItemStatus = ScheduleItemStatus.InProgress;
        } else if (allPlayersDetermined) {
            boolean startedOnTables = false;
            List<TableUsage> tableUsages = this.tableUsageService.findAllByMatchCardFk(matchCard.getId());
            for (TableUsage tableUsage : tableUsages) {
                if (tableUsage.getTableStatus() == TableStatus.InUse) {
                    startedOnTables = true;
                }
            }
            scheduleItemStatus = startedOnTables ? ScheduleItemStatus.Started : ScheduleItemStatus.NotStarted;
        }
        playerScheduleItem.setStatus(scheduleItemStatus);
    }

    /**
     * Gets all scheduling and status details for one event or match specified by matchcardid
     *
     * @param matchCardId match card id
     * @return
     */
    public PlayerScheduleItem getPlayerSchedule(Long matchCardId) {
        MatchCard matchCard = this.matchCardService.getMatchCardWithPlayerProfiles(matchCardId);
        TournamentEvent eventEntity = this.tournamentEventEntityService.get(matchCard.getEventFk());
        return toPlayerScheduleItem(matchCard, eventEntity,
                true);
    }

    /**
     * Converts match card into PlayerScheduleItem and optionally fills in all player details
     *
     * @param matchCard
     * @param eventEntity
     * @param fetchDetails
     * @return
     */
    private PlayerScheduleItem toPlayerScheduleItem(MatchCard matchCard,
                                                    TournamentEvent eventEntity,
                                                    boolean fetchDetails) {
        PlayerScheduleItem playerScheduleItem = new PlayerScheduleItem();

        playerScheduleItem.setEventName(eventEntity.getName());
        playerScheduleItem.setMatchCardId(matchCard.getId());
        playerScheduleItem.setEventId(matchCard.getEventFk());
        playerScheduleItem.setAssignedTables(matchCard.getAssignedTables());
        playerScheduleItem.setDay(matchCard.getDay());
        playerScheduleItem.setGroup(matchCard.getGroupNum());
        playerScheduleItem.setRound(matchCard.getRound());
        playerScheduleItem.setStartTime(matchCard.getStartTime());
        playerScheduleItem.setDoubles(eventEntity.isDoubles());
        if (fetchDetails) {
            List<PlayerDetail> playerDetails = makePlayerDetails(matchCard, eventEntity);
            playerScheduleItem.setPlayerDetails(playerDetails);
        }
        return playerScheduleItem;
    }

    /**
     * Makes player details showing each player status and other information
     *
     * @param matchCard     match card
     * @param eventEntity
     * @return player details
     */
    private List<PlayerDetail> makePlayerDetails(MatchCard matchCard, TournamentEvent eventEntity) {
        long tournamentId = eventEntity.getTournamentFk();
        int tournamentDay = eventEntity.getDay();
        List<PlayerDetail> playerDetailsList = new ArrayList<>();
        Map<String, String> profileIdToNameMap = matchCard.getProfileIdToNameMap();
        List<Match> matches = matchCard.getMatches();
        List<String> playerProfileIds = null;
        if (!eventEntity.isDoubles()) {
            playerProfileIds = new ArrayList<>(profileIdToNameMap.keySet());
        } else {
            playerProfileIds = new ArrayList<>();
            // split doubles team players to get status of each player individually
            Map<String, String> doublesProfileIdToNameMap = new HashMap<>();
            Set<String> combinedProfiles = profileIdToNameMap.keySet();
            for (String combinedProfile : combinedProfiles) {
                String combinedFullNames = profileIdToNameMap.get(combinedProfile);
                String[] playerProfiles = combinedProfile.split(";");
                playerProfileIds.add(playerProfiles[0]);
                playerProfileIds.add(playerProfiles[1]);
                String[] playerFullNames = combinedFullNames.split("/");
                doublesProfileIdToNameMap.put(playerProfiles[0], playerFullNames[0]);
                doublesProfileIdToNameMap.put(playerProfiles[1], playerFullNames[1]);
            }
            profileIdToNameMap = doublesProfileIdToNameMap;
        }
        // get status of each player - either for this day or for this event
        Tournament tournament = this.tournamentService.getByKey(tournamentId);
        long eventId = (tournament.getConfiguration().getCheckInType() == CheckInType.PEREVENT) ? eventEntity.getId() : 0;
        List<PlayerStatus> playerStatuses = this.playerStatusService.listPlayersByIds(playerProfileIds, tournamentId, tournamentDay, eventId);
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
                    playerDetail.setReason(playerStatus.getReason());
                    break;
                }
            }
            for (Match match : matches) {
                if (match.getPlayerAProfileId().contains(playerProfileId)) {
                    playerDetail.setPlayerCode(match.getPlayerALetter());
                    playerDetail.setRating(match.getPlayerARating());
                    playerDetail.setEstimated(false);
                    break;
                } else if (match.getPlayerBProfileId().contains(playerProfileId)) {
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
