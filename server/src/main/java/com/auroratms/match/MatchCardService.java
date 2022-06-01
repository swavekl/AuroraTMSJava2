package com.auroratms.match;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawService;
import com.auroratms.draw.DrawType;
import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import org.apache.commons.collections.list.TreeList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing march cards which contain individual matches
 */
@Service
@Transactional
public class MatchCardService {

    @Autowired
    private MatchCardRepository matchCardRepository;

    @Autowired
    private MatchService matchService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private DrawService drawService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private UsattDataService usattDataService;

    @Autowired
    private UserProfileService userProfileService;

    public MatchCardService() {
    }

    /**
     * Generates match cards and matches for all groups in this event
     *
     * @param eventId  event id
     * @param drawType draw type - Round Robin or single elimination
     */
    public void generateMatchCardsForEvent(long eventId, DrawType drawType) {
        List<DrawItem> eventDrawItems = drawService.list(eventId, drawType);
        TournamentEvent tournamentEvent = tournamentEventEntityService.get(eventId);
        List<MatchCard> matchCardList = new ArrayList<>();
        if (drawType == DrawType.ROUND_ROBIN) {
            generateRoundRobinMatchCards(eventDrawItems, tournamentEvent, matchCardList);
        } else if (drawType == DrawType.SINGLE_ELIMINATION) {
            generateSingleEliminationCards(eventDrawItems, tournamentEvent, matchCardList);
        }
        if (matchCardList != null) {
//            System.out.println("saving generated matchCardList of size = " + matchCardList.size());
//            for (MatchCard matchCard : matchCardList) {
//                System.out.println("matchCard = " + matchCard);
//            }
            matchCardRepository.saveAll(matchCardList);
//            System.out.println("saved match cards");
//            for (MatchCard matchCard : matchCardList) {
//                System.out.println("matchCard.id " + matchCard.getId());
//            }
            // update flag indicating if changing draws is allowed
            tournamentEvent.setMatchScoresEntered(false);
            tournamentEventEntityService.update(tournamentEvent);
        }
    }

    /**
     * Generates round robin phase match cards
     *  @param eventDrawItems
     * @param tournamentEvent
     * @param matchCardList
     */
    private void generateRoundRobinMatchCards(List<DrawItem> eventDrawItems,
                                              TournamentEvent tournamentEvent,
                                              List<MatchCard> matchCardList) {
        int currentGroupNum = 0;
        // separate group draws into their own lists
        List<DrawItem> groupDrawItems = new ArrayList<>();
        Map<Integer, List<DrawItem>> groupNumDrawItemsMap = new HashMap<>();
        for (DrawItem drawItem : eventDrawItems) {
            if (currentGroupNum != drawItem.getGroupNum()) {
                currentGroupNum = drawItem.getGroupNum();
                groupDrawItems = new ArrayList<>();
                groupNumDrawItemsMap.put(currentGroupNum, groupDrawItems);
            }
            groupDrawItems.add(drawItem);
        }

        /**
         * Generate matches for each group
         */
        int playersToAdvance = tournamentEvent.getPlayersToAdvance();

        Map<Character, String> mapPlayerCodeToProfileId = new HashMap<>();
        for (Integer groupNumber : groupNumDrawItemsMap.keySet()) {
            groupDrawItems = groupNumDrawItemsMap.get(groupNumber);
            int playersDrawnIntoGroup = groupDrawItems.size();
            mapPlayerCodeToProfileId.clear();
            // if this group consists only of a seeded player then don't create score sheets
            if (playersDrawnIntoGroup > 1) {
                List<MatchOpponents> matchesInOrder = MatchOrderGenerator.generateOrderOfMatches(playersDrawnIntoGroup, playersToAdvance);

                // collect players information
                Map<Character, Integer> playerCodeToRating = new HashMap<>();
                for (DrawItem drawItem : groupDrawItems) {
                    int placeInGroup = drawItem.getPlaceInGroup();
                    Character playerCode = (char) ('A' + (placeInGroup - 1));
                    mapPlayerCodeToProfileId.put(playerCode, drawItem.getPlayerId());
                    playerCodeToRating.put(playerCode, drawItem.getRating());
                }

                // convert match order into matches
                MatchCard matchCard = new MatchCard();
                matchCard.setEventFk(tournamentEvent.getId());
                matchCard.setGroupNum(groupNumber);
                matchCard.setPlayerAGroupNum(0);
                matchCard.setPlayerBGroupNum(0);
                matchCard.setDrawType(DrawType.ROUND_ROBIN);
                matchCard.setRound(0);
                matchCard.setDay(tournamentEvent.getDay());
                matchCard.setStartTime(tournamentEvent.getStartTime());
                matchCard.setNumberOfGames(tournamentEvent.getNumberOfGames());
                List<Match> matches = new ArrayList<>();

                int matchNumber = 0;
                for (MatchOpponents matchOpponents : matchesInOrder) {
                    if (matchOpponents.playerALetter != MatchOrderGenerator.BYE &&
                            matchOpponents.playerBLetter != MatchOrderGenerator.BYE) {
                        String playerAProfileId = mapPlayerCodeToProfileId.get(matchOpponents.playerALetter);
                        String playerBProfileId = mapPlayerCodeToProfileId.get(matchOpponents.playerBLetter);
                        Integer playerARating = playerCodeToRating.get(matchOpponents.playerALetter);
                        Integer playerBRating = playerCodeToRating.get(matchOpponents.playerBLetter);
                        Match match = new Match();
                        match.setMatchCard(matchCard);
                        match.setMatchNum(++matchNumber);
                        match.setPlayerAProfileId(playerAProfileId);
                        match.setPlayerBProfileId(playerBProfileId);
                        match.setSideADefaulted(false);
                        match.setSideBDefaulted(false);
                        match.setPlayerALetter(matchOpponents.playerALetter);
                        match.setPlayerBLetter(matchOpponents.playerBLetter);
                        match.setPlayerARating(playerARating);
                        match.setPlayerBRating(playerBRating);
                        matches.add(match);
                    }
                }
                matchCard.setMatches(matches);
                matchCardList.add(matchCard);
            }
        }
    }

    /**
     * @param eventDrawItems
     * @param tournamentEvent
     * @return
     */
    private void generateSingleEliminationCards(List<DrawItem> eventDrawItems,
                                                           TournamentEvent tournamentEvent,
                                                           List<MatchCard> matchCardList) {
        // round of is a number of players in a round e.g. 64, 32, etc.
        int roundOf = 0; // eventDrawItems.size();
        for (DrawItem eventDrawItem : eventDrawItems) {
            roundOf = Math.max(roundOf, eventDrawItem.getRound());
        }
        // remove items not for this round
        List<DrawItem> drawItemsForThisRound = new ArrayList<>();
        for (DrawItem eventDrawItem : eventDrawItems) {
            if (eventDrawItem.getRound() == roundOf) {
                drawItemsForThisRound.add(eventDrawItem);
            }
        }

        // in round of 64 players there will be 32 matches to play
        // draw items are for each player so in S.E. phase we need to pair them up to create a match
        int matchesToPlay = roundOf / 2;

        int numberOfGames = getNumberOfGames(tournamentEvent, roundOf);
        for (int matchNum = 0; matchNum < matchesToPlay; matchNum++) {
            int startIndex = matchNum * 2;
            DrawItem playerADrawItem = drawItemsForThisRound.get(startIndex);
            DrawItem playerBDrawItem = drawItemsForThisRound.get(startIndex + 1);
            String playerAProfileId = playerADrawItem.getPlayerId();
            String playerBProfileId = playerBDrawItem.getPlayerId();
            int playerARating = playerADrawItem.getRating();
            int playerBRating = playerBDrawItem.getRating();
            int playerAGroup = playerADrawItem.getGroupNum();
            int playerBGroup = playerBDrawItem.getGroupNum();

            if (!StringUtils.isEmpty(playerAProfileId) && !StringUtils.isEmpty(playerBProfileId)) {

                // create match card with one match
                MatchCard matchCard = new MatchCard();
                matchCard.setEventFk(tournamentEvent.getId());
                matchCard.setGroupNum(matchNum + 1);
                matchCard.setPlayerAGroupNum(playerAGroup);
                matchCard.setPlayerBGroupNum(playerBGroup);
                matchCard.setNumberOfGames(numberOfGames);
                matchCard.setDrawType(DrawType.SINGLE_ELIMINATION);
                matchCard.setRound(roundOf);
                matchCard.setDay(tournamentEvent.getDay());
                matchCard.setStartTime(tournamentEvent.getStartTime());

                List<Match> matches = new ArrayList<>();
                Match match = new Match();
                match.setMatchCard(matchCard);
                match.setMatchNum(matchNum + 1);
                match.setPlayerAProfileId(playerAProfileId);
                match.setPlayerBProfileId(playerBProfileId);
                match.setSideADefaulted(false);
                match.setSideBDefaulted(false);
                match.setPlayerALetter('A');
                match.setPlayerBLetter('B');
                match.setPlayerARating(playerARating);
                match.setPlayerBRating(playerBRating);

                matches.add(match);
                matchCard.setMatches(matches);
                matchCardList.add(matchCard);
            }
        }

        generateRemainingSERoundMatchCards(tournamentEvent, roundOf, matchCardList, eventDrawItems);
    }

    /**
     * @param tournamentEvent
     * @param firstSERoundParticipants
     * @param matchCardList
     * @param eventDrawItems
     */
    private void generateRemainingSERoundMatchCards(TournamentEvent tournamentEvent,
                                                    int firstSERoundParticipants,
                                                    List<MatchCard> matchCardList,
                                                    List<DrawItem> eventDrawItems) {
        // how many players in this round
        int roundOf = firstSERoundParticipants / 2;
        int remainingRounds = (int) Math.ceil(Math.log(roundOf) / Math.log(2));
        for (int round = 0; round < remainingRounds; round++) {
            int numGamesInRound = getNumberOfGames(tournamentEvent, roundOf);
            int numMatchesInRound = roundOf / 2;
            for (int matchNum = 0; matchNum < numMatchesInRound; matchNum++) {
                makeSERoundMatchCard(matchNum, roundOf, tournamentEvent,
                        numGamesInRound, matchCardList, eventDrawItems);
            }
            // last round and play for 3 & 4th place ?
            if ((round + 1) == remainingRounds && tournamentEvent.isPlay3rd4thPlace()) {
                makeSERoundMatchCard(1, roundOf, tournamentEvent,
                        numGamesInRound, matchCardList, eventDrawItems);
            }
            roundOf = roundOf / 2;
        }
    }

    /**
     * @param matchNum
     * @param roundOf
     * @param tournamentEvent
     * @param numberOfGames
     * @param matchCardList
     * @param eventDrawItems
     * @return
     */
    private void makeSERoundMatchCard(int matchNum,
                                      int roundOf,
                                      TournamentEvent tournamentEvent,
                                      int numberOfGames,
                                      List<MatchCard> matchCardList,
                                      List<DrawItem> eventDrawItems) {
        DrawItem advancedPlayerADrawItem = null;
        DrawItem advancedPlayerBDrawItem = null;
        // find the draw items corresponding to this match card
        for (DrawItem drawItem : eventDrawItems) {
            if (drawItem.getRound() == roundOf) {
                // previous round group line 1 & 2 go to match #1, lines 3 & 4 to match # 2 and so on
                int matchNumToGoTo = (int) Math.ceil((double)drawItem.getSingleElimLineNum() / 2);
                if (matchNum + 1 == matchNumToGoTo) {
                    // odd numbers => A player
                    if (drawItem.getSingleElimLineNum() % 2 == 1) {
                        advancedPlayerADrawItem = drawItem;
                    }
                    // even number => B player
                    if (drawItem.getSingleElimLineNum() % 2 == 0) {
                        advancedPlayerBDrawItem = drawItem;
                    }
                }
            }
        }

        String playerAProfileId = (advancedPlayerADrawItem != null) ? advancedPlayerADrawItem.getPlayerId() : DrawItem.TBD_PROFILE_ID;
        String playerBProfileId = (advancedPlayerBDrawItem != null) ? advancedPlayerBDrawItem.getPlayerId() : DrawItem.TBD_PROFILE_ID;
        int playerARating = (advancedPlayerADrawItem != null) ? advancedPlayerADrawItem.getRating() : 0;
        int playerBRating = (advancedPlayerBDrawItem != null) ? advancedPlayerBDrawItem.getRating() : 0;
        int playerAGroup = (advancedPlayerADrawItem != null) ? advancedPlayerADrawItem.getGroupNum() : 0;
        int playerBGroup = (advancedPlayerBDrawItem != null) ? advancedPlayerBDrawItem.getGroupNum() : 0;

        MatchCard matchCard = new MatchCard();
        matchCard.setEventFk(tournamentEvent.getId());
        matchCard.setGroupNum(matchNum + 1);
        matchCard.setPlayerAGroupNum(playerAGroup);
        matchCard.setPlayerBGroupNum(playerBGroup);
        matchCard.setNumberOfGames(numberOfGames);
        matchCard.setDrawType(DrawType.SINGLE_ELIMINATION);
        matchCard.setRound(roundOf);
        matchCard.setDay(tournamentEvent.getDay());
        matchCard.setStartTime(tournamentEvent.getStartTime());
        matchCard.setDuration(30);

        List<Match> matches = new ArrayList<>();
        Match match = new Match();
        match.setMatchCard(matchCard);
        match.setMatchNum(matchNum + 1);
        match.setPlayerAProfileId(playerAProfileId);
        match.setPlayerBProfileId(playerBProfileId);
        match.setSideADefaulted(false);
        match.setSideBDefaulted(false);
        match.setPlayerALetter('A');
        match.setPlayerBLetter('B');
        match.setPlayerARating(playerARating);
        match.setPlayerBRating(playerBRating);

        matches.add(match);
        matchCard.setMatches(matches);
        matchCardList.add(matchCard);
    }

    /**
     * Gets number of games to play in a given round as configured in event
     *
     * @param tournamentEvent event configuration
     * @param roundOf               round of 64, 32 etc.
     * @return
     */
    private int getNumberOfGames(TournamentEvent tournamentEvent, int roundOf) {
        int numGames = 5;
        switch (roundOf) {
            // finals & 3rd/4th place
            case 2:
                numGames = tournamentEvent.getNumberOfGamesSEFinals();
                break;
            case 4:
                numGames = tournamentEvent.getNumberOfGamesSESemiFinals();
                break;
            case 8:
                numGames = tournamentEvent.getNumberOfGamesSEQuarterFinals();
                break;
            default:
                numGames = tournamentEvent.getNumberOfGamesSEPlayoffs();
                break;
        }
        // for uninitialized
        if (numGames == 0) {
            numGames = 5;
        }
        return numGames;
    }

    /**
     * Updates match cards keeping already entered match results
     *
     * @param eventId
     * @param drawType
     * @param updatedDrawItems
     */
    public void updateMatchCardsForEvent(long eventId, DrawType drawType, List<DrawItem> updatedDrawItems) {
        List<DrawItem> eventDrawItems = drawService.list(eventId, drawType);
        TournamentEvent tournamentEvent = tournamentEventEntityService.get(eventId);

        List<MatchCard> existingMatchCards = this.findAllForEventAndDrawType(eventId, drawType);

        // regenerate the match cards based on updated draws.
        List<MatchCard> matchCardList = new ArrayList<>(existingMatchCards.size());
        if (drawType == DrawType.ROUND_ROBIN) {
            generateRoundRobinMatchCards(eventDrawItems, tournamentEvent, matchCardList);
        } else if (drawType == DrawType.SINGLE_ELIMINATION) {
            generateSingleEliminationCards(eventDrawItems, tournamentEvent, matchCardList);
        }

        for (DrawItem updatedDrawItem : updatedDrawItems) {
            int groupNum = (drawType == DrawType.ROUND_ROBIN)
                    ? updatedDrawItem.getGroupNum()
                    : (int) Math.ceil((double)updatedDrawItem.getSingleElimLineNum() / 2);
            int roundOf = updatedDrawItem.getRound();
//            System.out.println("MatchCardService.updateMatchCardsForEvent looking for match cards roundOf = " + roundOf + " groupNum = " + groupNum);
            MatchCard existingMatchCard = findMatchCardForRoundGroup(roundOf, groupNum, existingMatchCards);
            MatchCard updatedMatchCard  = findMatchCardForRoundGroup(roundOf, groupNum, matchCardList);
            if (existingMatchCard != null && updatedMatchCard != null) {
//                System.out.println("existingMatchCard.id = " + existingMatchCard.getId());
                // get all players ids to see if they changed
                Set<String> oldPlayerProfileIds = getPlayerIdsForMatchCard(existingMatchCard);
                Set<String> newPlayerProfileIds = getPlayerIdsForMatchCard(updatedMatchCard);
//                System.out.println("oldPlayerProfileIds = " + oldPlayerProfileIds);
//                System.out.println("newPlayerProfileIds = " + newPlayerProfileIds);
                if (!oldPlayerProfileIds.equals(newPlayerProfileIds)) {
//                    System.out.println("players changed - updating");
                    // transfer matches to preserve everything else - id, assigned tables etc.
                    List<Match> oldMatches = existingMatchCard.getMatches();
                    oldMatches.clear();
                    List<Match> newMatches = updatedMatchCard.getMatches();
                    existingMatchCard.getMatches().addAll(newMatches);
                    for (Match match : newMatches) {
                        match.setMatchCard(existingMatchCard);
                    }
                    // clear player rankings since the matches will be changed
                    existingMatchCard.setPlayerRankings(null);

                    matchCardRepository.save(existingMatchCard);
//                    System.out.println("match card saved");
                }
            }
        }

        // update flag indicating if changing draws is allowed
        tournamentEvent.setMatchScoresEntered(false);
        tournamentEventEntityService.update(tournamentEvent);
    }

    /**
     *
     * @param roundOf
     * @param groupNum
     * @param matchCards
     * @return
     */
    private MatchCard findMatchCardForRoundGroup(int roundOf, int groupNum, List<MatchCard> matchCards) {
        MatchCard foundMatchCard = null;
        for (MatchCard matchCard : matchCards) {
            if (matchCard.getRound() == roundOf && matchCard.getGroupNum() == groupNum) {
                foundMatchCard = matchCard;
                break;
            }
        }
        return foundMatchCard;
    }

    /**
     *
     * @param matchCard
     * @return
     */
    private Set<String> getPlayerIdsForMatchCard(MatchCard matchCard) {
        Set<String> playerProfileIds = new TreeSet<>();
        List<Match> matches = matchCard.getMatches();
        for (Match match : matches) {
            playerProfileIds.add(match.getPlayerAProfileId());
            playerProfileIds.add(match.getPlayerBProfileId());
        }
        return playerProfileIds;
    }

    public MatchCard getMatchCard(long eventId, int round, int groupNum) {
        return this.matchCardRepository.findMatchCardByEventFkAndRoundAndGroupNum(eventId, round, groupNum)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find match card"));
    }

    public boolean existsMatchCard(long eventId, int round, int groupNum) {
        return this.matchCardRepository.findMatchCardByEventFkAndRoundAndGroupNum(eventId, round, groupNum).isPresent();
    }

    public MatchCard getMatchCard(long id) {
        return this.matchCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find match card"));
    }

    /**
     * Gets all match cards for given day of the tournament
     *
     * @param tournamentId
     * @param day
     * @return
     */
    public List<MatchCard> findAllForTournamentAndDay(long tournamentId, int day) {
        List<Long> eventIds = getEventIdsForTheDay(tournamentId);
        return this.matchCardRepository.findMatchCardByEventFkInAndDayOrderByEventFkAscStartTimeAscGroupNumAsc(eventIds, day);
    }

    public void fillPlayerIdToNameMapForAllMatches (List<MatchCard> matchCards) {
        Set<Long> eventsForMatchCards = new HashSet<>();
        for (MatchCard matchCard : matchCards) {
            eventsForMatchCards.add(matchCard.getEventFk());
        }

        // get all matches in one query for all match cards passed in
        // otherwise Hibernate would execute a query to get matches for each match card separately
        List<Match> allMatches = this.matchService.findAllByMatchCardIn(matchCards);
        // optimize retrieval of profile id to player full names map by grouping match cards for each event together
        for (Long eventFk : eventsForMatchCards) {
            List<Match> matchesForThisEvent = new ArrayList<>();
            // collect matches from all match cards for this event
            for (MatchCard matchCard : matchCards) {
                if (matchCard.getEventFk() == eventFk) {
                    long matchCardId = matchCard.getId();
                    for (Match match : allMatches) {
                        if (match.getMatchCard().getId() == matchCardId) {
                            matchesForThisEvent.add(match);
                        }
                    }
                }
            }
            // produce the map for all matches from these matches
            Map<String, String> profileIdToNameMapForEvent = buildProfileIdToNameMap(matchesForThisEvent);
            // split the map by match card
            for (MatchCard matchCard : matchCards) {
                if (matchCard.getEventFk() == eventFk) {
                    long matchCardId = matchCard.getId();
                    // build profile to name map for all matches for this match card
                    Map<String, String> profileIdNameMap = new HashMap<>();
                    for (Match match : allMatches) {
                        if (match.getMatchCard().getId() == matchCardId) {
                            String playerAProfileId = match.getPlayerAProfileId();
                            String playerBProfileId = match.getPlayerBProfileId();
                            if (playerAProfileId != null && !playerAProfileId.equals(DrawItem.TBD_PROFILE_ID)) {
                                String playerAFullName = profileIdToNameMapForEvent.get(playerAProfileId);
                                if (playerAFullName != null) {
                                    profileIdNameMap.put(playerAProfileId, playerAFullName);
                                }
                            }
                            if (playerBProfileId != null && !playerBProfileId.equals(DrawItem.TBD_PROFILE_ID)) {
                                String playerBFullName = profileIdToNameMapForEvent.get(playerBProfileId);
                                if (playerBFullName != null) {
                                    profileIdNameMap.put(playerBProfileId, playerBFullName);
                                }
                            }
                        }
                    }
                    // save to match card
                    matchCard.setProfileIdToNameMap(profileIdNameMap);
                }
            }
        }
    }

    public List<MatchCard> findAllForTournamentAndDayAndAssignedTable(long tournamentId, int day, int tableNumber) {
        List<Long> eventIds = getEventIdsForTheDay(tournamentId);
        String assignedTableContains = "" + tableNumber;
        return matchCardRepository.findMatchCardByEventFkInAndDayAndAssignedTablesContainsOrderByEventFkAscStartTimeAsc(eventIds, day, assignedTableContains);
    }

    private List<Long> getEventIdsForTheDay(long tournamentId) {
        // find match cards generated for the events held on this day.
        // In larger tournaments single elimination round matches may be scheduled for later days
        // so grab all of them and let repository filter out those on different day
        Collection<TournamentEvent> allTournamentEvents = this.tournamentEventEntityService.list(tournamentId, Pageable.unpaged());
        List<Long> eventIds = new ArrayList<>();
        for (TournamentEvent event : allTournamentEvents) {
            eventIds.add(event.getId());
        }
        return eventIds;
    }

    /**
     * Get one specified match card
     *
     * @param matchCardId
     * @return
     */
    public MatchCard getMatchCardWithPlayerProfiles(long matchCardId) {
        MatchCard matchChard = this.matchCardRepository.findById(matchCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find match card"));

        // get the names of players for all matches in this card
        Map<String, String> profileIdToNameMap = buildProfileIdToNameMap(matchChard.getMatches());
        matchChard.setProfileIdToNameMap(profileIdToNameMap);

        return matchChard;
    }

    /**
     * @param matches
     * @return
     */
    private Map<String, String> buildProfileIdToNameMap(List<Match> matches) {
        boolean doublesEvent = false;
        Set<String> playerProfileIds = new HashSet<String>();
        for (Match match : matches) {
            String playerAProfileId = match.getPlayerAProfileId();
            String playerBProfileId = match.getPlayerBProfileId();
            // if this is doubles event then 2 profiles are encoded in the string
            if (playerAProfileId.indexOf(";") > 0 && playerBProfileId.indexOf(";") > 0) {
                doublesEvent = true;
                String[] teamAProfiles = playerAProfileId.split(";");
                String[] teamBProfiles = playerBProfileId.split(";");
                playerProfileIds.addAll(Arrays.asList(teamAProfiles));
                playerProfileIds.addAll(Arrays.asList(teamBProfiles));
            } else {
                playerProfileIds.add(playerAProfileId);
                playerProfileIds.add(playerBProfileId);
            }
        }

        // using profile service - slower but more accurate
//        Collection<UserProfile> userProfiles = this.userProfileService.listByProfileIds(uniquePlayerProfiles);
//        for (UserProfile userProfile : userProfiles) {
//            String fullName = userProfile.getLastName() + ", " + userProfile.getFirstName();
//            profileIdToNameMap.put(userProfile.getUserId(), fullName);
//        }

        // get all player names - as if event was an individual event, not doubles event
        List<String> uniquePlayerProfiles = new ArrayList<>(playerProfileIds);
        Map<String, UserProfileExt> userProfileExtMap = this.userProfileExtService.findByProfileIds(uniquePlayerProfiles);
        Map<Long, String> membershipIdToProfileIdMap = new HashMap<>();
        for (UserProfileExt userProfileExt : userProfileExtMap.values()) {
            membershipIdToProfileIdMap.put(userProfileExt.getMembershipId(), userProfileExt.getProfileId());
        }
        List<Long> membershipIds = new ArrayList<>(membershipIdToProfileIdMap.keySet());
        List<UsattPlayerRecord> usattPlayerRecordList = this.usattDataService.findAllByMembershipIdIn(membershipIds);

        Map<String, String> profileIdToNameMap = new HashMap<>();
        for (UsattPlayerRecord usattPlayerRecord : usattPlayerRecordList) {
            Long membershipId = usattPlayerRecord.getMembershipId();
            String profileId = membershipIdToProfileIdMap.get(membershipId);
            if (profileId != null) {
                String fullName = usattPlayerRecord.getLastName() + ", " + usattPlayerRecord.getFirstName();
                profileIdToNameMap.put(profileId, fullName);
            }
        }

        if (!doublesEvent) {
            return profileIdToNameMap;
        } else {
            // build doubles team member names like so team player 1 / team player 2
            Map<String, String> doublesTeamsProfileIdToNameMap = new HashMap<>();
            for (Match match : matches) {
                String playerAProfileId = match.getPlayerAProfileId();
                String teamAPlayersNames = getDoublesTeamPlayerNames(profileIdToNameMap, playerAProfileId);
                doublesTeamsProfileIdToNameMap.put(playerAProfileId, teamAPlayersNames);

                String playerBProfileId = match.getPlayerBProfileId();
                String teamBPlayersNames = getDoublesTeamPlayerNames(profileIdToNameMap, playerBProfileId);
                doublesTeamsProfileIdToNameMap.put(playerBProfileId, teamBPlayersNames);
            }
            return doublesTeamsProfileIdToNameMap;
        }
    }

    /**
     *
     * @param playerRankingsMap
     * @return
     */
    public Map<String, String> convertPlayerRankingsToPlayerNamesMap(Map<Integer, String> playerRankingsMap) {
        boolean doublesEvent = false;
        // collect all unique player profile ids
        Set<String> uniquePlayerProfiles = new HashSet<>();
        for (String playerProfileId : playerRankingsMap.values()) {
            if (playerProfileId.indexOf(";") > 0) {
                doublesEvent = true;
                String[] teamProfiles = playerProfileId.split(";");
                uniquePlayerProfiles.addAll(Arrays.asList(teamProfiles));
            } else {
                uniquePlayerProfiles.add(playerProfileId);
            }
        }
        // convert the set into a list and get them all at once
        List<String> uniquePlayerProfilesList = new ArrayList<>(uniquePlayerProfiles);
        Collection<UserProfile> userProfiles = this.userProfileService.listByProfileIds(uniquePlayerProfilesList);
        // now build the map of profile id to full name
        Map<String, String> profileIdToNameMap = new TreeMap<>();
        for (UserProfile userProfile : userProfiles) {
            String fullName = userProfile.getLastName() + ", " + userProfile.getFirstName();
            profileIdToNameMap.put(userProfile.getUserId(), fullName);
        }
        if (!doublesEvent) {
            return profileIdToNameMap;
        } else {
            // build doubles team member names like so team player 1 / team player 2
            Map<String, String> doublesTeamsProfileIdToNameMap = new HashMap<>();
            for (String playerProfileId : playerRankingsMap.values()) {
                String teamPlayersNames = getDoublesTeamPlayerNames(profileIdToNameMap, playerProfileId);
                doublesTeamsProfileIdToNameMap.put(playerProfileId, teamPlayersNames);
            }
            return doublesTeamsProfileIdToNameMap;
        }
    }


    /**
     * Splits doubles team player profiles separated by ; and combines their full names
     *
     * @param profileIdToNameMap
     * @param teamPlayerProfileIds
     * @return
     */
    private String getDoublesTeamPlayerNames(Map<String, String> profileIdToNameMap, String teamPlayerProfileIds) {
        String[] teamProfilesIds = teamPlayerProfileIds.split(";");
        String teamPlayersNames = null;
        for (String playerProfileId : teamProfilesIds) {
            String playerFullName = profileIdToNameMap.get(playerProfileId);
            teamPlayersNames = (teamPlayersNames == null) ? playerFullName : (teamPlayersNames + " / " + playerFullName);
        }
        return teamPlayersNames;
    }

    /**
     * @param matchCard
     * @return
     */
    public long save(MatchCard matchCard) {
        MatchCard saved = this.matchCardRepository.save(matchCard);
        return saved.getId();
    }

    public List<MatchCard> findAllForEvent(long eventId) {
        return this.matchCardRepository.findMatchCardByEventFkOrderByDrawTypeDescGroupNumAsc(eventId);
    }

    public List<MatchCard> findAllForEventAndDrawType(long eventId, DrawType drawType) {
        return this.matchCardRepository.findMatchCardByEventFkAndDrawTypeOrderByRoundDescGroupNumAsc(eventId, drawType);
    }

    public List<MatchCard> findAllForEventAndDrawTypeWithPlayerMap(long eventId, DrawType drawType) {
        List<MatchCard> matchCards = this.matchCardRepository.findMatchCardByEventFkAndDrawTypeOrderByRoundDescGroupNumAsc(eventId, drawType);
        for (MatchCard matchCard : matchCards) {
            Map<String, String> profileIdToNameMap = buildProfileIdToNameMap(matchCard.getMatches());
            matchCard.setProfileIdToNameMap(profileIdToNameMap);
        }

        return matchCards;
    }

    public List<MatchCard> findAllForEventAndDrawTypeAndRound(long eventId, DrawType drawType, int round) {
        return this.matchCardRepository.findMatchCardByEventFkAndDrawTypeAndRound(eventId, drawType, round);
    }

    public void deleteAllForEventAndDrawType(long eventId, DrawType drawType) {
        // retrieve all and delete all so when this is part of same transaction they are deleted individually perhaps
        // by id and avoid deleting those which are created later (by eventId and drawType)
        List<MatchCard> allForEventAndDrawType = findAllForEventAndDrawType(eventId, drawType);
        this.matchCardRepository.deleteAll(allForEventAndDrawType);
//        this.matchCardRepository.deleteAllByEventFkAndDrawType(eventId, drawType);

        // update flag indicating if changing draws is allowed
        TournamentEvent tournamentEvent = tournamentEventEntityService.get(eventId);
        tournamentEvent.setMatchScoresEntered(false);
        tournamentEventEntityService.update(tournamentEvent);
    }

    public void delete(long eventId, DrawType drawType, int groupNum) {
        MatchCard matchCard = new MatchCard();
        matchCard.setEventFk(eventId);
        matchCard.setGroupNum(groupNum);
        matchCard.setDrawType(drawType);
        this.matchCardRepository.delete(matchCard);
    }

}
