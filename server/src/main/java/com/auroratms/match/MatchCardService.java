package com.auroratms.match;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawService;
import com.auroratms.draw.DrawType;
import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
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
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private DrawService drawService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private UsattDataService usattDataService;

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
        TournamentEventEntity tournamentEventEntity = tournamentEventEntityService.get(eventId);
        if (drawType == DrawType.ROUND_ROBIN) {
            generateRoundRobinMatchCards(eventDrawItems, tournamentEventEntity);
        } else if (drawType == DrawType.SINGLE_ELIMINATION) {
            generateSingleEliminationCards(eventDrawItems, tournamentEventEntity);
        }
    }

    /**
     * Generates round robin phase match cards
     *
     * @param eventDrawItems
     * @param tournamentEventEntity
     */
    private void generateRoundRobinMatchCards(List<DrawItem> eventDrawItems, TournamentEventEntity tournamentEventEntity) {
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
        int playersToAdvance = tournamentEventEntity.getPlayersToAdvance();

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
                matchCard.setEventFk(tournamentEventEntity.getId());
                matchCard.setGroupNum(groupNumber);
                matchCard.setDrawType(DrawType.ROUND_ROBIN);
                matchCard.setRound(0);
                matchCard.setDay(tournamentEventEntity.getDay());
                matchCard.setStartTime(tournamentEventEntity.getStartTime());
                matchCard.setNumberOfGames(tournamentEventEntity.getNumberOfGames());
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
                this.matchCardRepository.save(matchCard);
            }
        }
    }

    /**
     * @param eventDrawItems
     * @param tournamentEventEntity
     */
    private void generateSingleEliminationCards(List<DrawItem> eventDrawItems, TournamentEventEntity tournamentEventEntity) {
        // round of is a number of players in a round e.g. 64, 32, etc.
        int roundOf = eventDrawItems.size();
        // in round of 64 players there will be 32 matches to play
        // draw items are for each player so in S.E. phase we need to pair them up to create a match
        int matchesToPlay = roundOf / 2;

        int numberOfGames = getNumberOfGames(tournamentEventEntity, roundOf);
        for (int matchNum = 0; matchNum < matchesToPlay; matchNum++) {
            int startIndex = matchNum * 2;
            DrawItem playerADrawItem = eventDrawItems.get(startIndex);
            DrawItem playerBDrawItem = eventDrawItems.get(startIndex + 1);
            String playerAProfileId = playerADrawItem.getPlayerId();
            String playerBProfileId = playerBDrawItem.getPlayerId();
            int playerARating = playerADrawItem.getRating();
            int playerBRating = playerBDrawItem.getRating();

            if (!StringUtils.isEmpty(playerAProfileId) && !StringUtils.isEmpty(playerBProfileId)) {

                // create match card with one match
                MatchCard matchCard = new MatchCard();
                matchCard.setEventFk(tournamentEventEntity.getId());
                matchCard.setGroupNum(matchNum + 1);
                matchCard.setNumberOfGames(numberOfGames);
                matchCard.setDrawType(DrawType.SINGLE_ELIMINATION);
                matchCard.setRound(roundOf);
                matchCard.setDay(tournamentEventEntity.getDay());
                matchCard.setStartTime(tournamentEventEntity.getStartTime());

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
                this.matchCardRepository.save(matchCard);
            }
        }
        generateRemainingSERoundMatchCards(tournamentEventEntity, roundOf);
    }

    /**
     *
     * @param tournamentEventEntity
     * @param firstSERoundParticipants
     */
    private void generateRemainingSERoundMatchCards(TournamentEventEntity tournamentEventEntity, int firstSERoundParticipants) {
        // how many players in this round
        int roundOf = firstSERoundParticipants / 2;
        int remainingRounds = (int) Math.ceil(Math.log(roundOf) / Math.log(2));
        for (int round = 0; round < remainingRounds; round++) {
            int numGamesInRound = getNumberOfGames(tournamentEventEntity, roundOf);
            int numMatchesInRound = roundOf / 2;
            for (int matchNum = 0; matchNum < numMatchesInRound; matchNum++) {
                makeSERoundMatchCard(matchNum, roundOf, tournamentEventEntity, numGamesInRound);
            }
            // last round and play for 3 & 4th place ?
            if ((round + 1) == remainingRounds && tournamentEventEntity.isPlay3rd4thPlace()) {
                makeSERoundMatchCard(1, roundOf, tournamentEventEntity, numGamesInRound);
            }
            roundOf = roundOf / 2;
        }
    }

    /**
     *
     * @param matchNum
     * @param roundOf
     * @param tournamentEventEntity
     * @param numberOfGames
     */
    private void makeSERoundMatchCard (int matchNum, int roundOf, TournamentEventEntity tournamentEventEntity, int numberOfGames) {
        MatchCard matchCard = new MatchCard();
        matchCard.setEventFk(tournamentEventEntity.getId());
        matchCard.setGroupNum(matchNum + 1);
        matchCard.setNumberOfGames(numberOfGames);
        matchCard.setDrawType(DrawType.SINGLE_ELIMINATION);
        matchCard.setRound(roundOf);
        matchCard.setDay(tournamentEventEntity.getDay());
        matchCard.setStartTime(tournamentEventEntity.getStartTime());
        matchCard.setDuration(30);

        List<Match> matches = new ArrayList<>();
        Match match = new Match();
        match.setMatchCard(matchCard);
        match.setMatchNum(matchNum + 1);
        match.setPlayerAProfileId("TBD");
        match.setPlayerBProfileId("TBD");
        match.setSideADefaulted(false);
        match.setSideBDefaulted(false);
        match.setPlayerALetter('A');
        match.setPlayerBLetter('B');
        match.setPlayerARating(0);
        match.setPlayerBRating(0);

        matches.add(match);
        matchCard.setMatches(matches);
        this.matchCardRepository.save(matchCard);
    }

    /**
     * Gets number of games to play in a given round as configured in event
     *
     * @param tournamentEventEntity event configuration
     * @param roundOf               round of 64, 32 etc.
     * @return
     */
    private int getNumberOfGames(TournamentEventEntity tournamentEventEntity, int roundOf) {
        int numGames = 5;
        switch (roundOf) {
            // finals & 3rd/4th place
            case 2:
                numGames = tournamentEventEntity.getNumberOfGamesSEFinals();
                break;
            case 4:
                numGames = tournamentEventEntity.getNumberOfGamesSESemiFinals();
                break;
            case 8:
                numGames = tournamentEventEntity.getNumberOfGamesSEQuarterFinals();
                break;
            default:
                numGames = tournamentEventEntity.getNumberOfGamesSEPlayoffs();
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
     */
    public void updateMatchCardsForEvent(long eventId, DrawType drawType) {
        List<DrawItem> eventDrawItems = drawService.list(eventId, drawType);
        TournamentEventEntity tournamentEventEntity = tournamentEventEntityService.get(eventId);

        List<MatchCard> existingMatchCards = this.findAllForEventAndDrawType(eventId, drawType);

        deleteAllForEventAndDrawType(eventId, drawType);
        if (drawType == DrawType.ROUND_ROBIN) {
            generateRoundRobinMatchCards(eventDrawItems, tournamentEventEntity);
        } else if (drawType == DrawType.SINGLE_ELIMINATION) {
            generateSingleEliminationCards(eventDrawItems, tournamentEventEntity);
        }

        // transfer start times, assigned table numbers etc. to the updated match cards
        List<MatchCard> updatedMatchCards = this.findAllForEventAndDrawType(eventId, drawType);
        for (MatchCard existingMatchCard : existingMatchCards) {
            for (MatchCard updatedMatchCard : updatedMatchCards) {
                if (existingMatchCard.getGroupNum() == updatedMatchCard.getGroupNum()) {
                    updatedMatchCard.setAssignedTables(existingMatchCard.getAssignedTables());
                    updatedMatchCard.setDuration(existingMatchCard.getDuration());
                    updatedMatchCard.setStartTime(existingMatchCard.getStartTime());
                }
            }
        }
        matchCardRepository.saveAll(updatedMatchCards);
    }

    public MatchCard getMatchCard(long eventId, int groupNum) {
        return this.matchCardRepository.findMatchCardByEventFkAndGroupNum(eventId, groupNum)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find match card"));
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
        // find match cards generated for the events held on this day.
        // In larger tournaments single elimination round matches may be scheduled for later days
        // so grab all of them and let repository filter out those on different day
        Collection<TournamentEventEntity> allTournamentEvents = this.tournamentEventEntityService.list(tournamentId, Pageable.unpaged());
        List<Long> eventIds = new ArrayList<>();
        for (TournamentEventEntity event : allTournamentEvents) {
            eventIds.add(event.getId());
        }

        // now get all of them in one shot
        return this.matchCardRepository.findMatchCardByEventFkInAndDayOrderByEventFkAscStartTimeAsc(eventIds, day);
    }

    /**
     * Get one specified match card
     * @param matchCardId
     * @return
     */
    public MatchCard get(long matchCardId) {
        MatchCard matchChard = this.matchCardRepository.findById(matchCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find match card"));

        // get the names of players for all matches in this card
        Map<String, String> profileIdToNameMap = buildProfileIdToNameMap(matchChard.getMatches());
        matchChard.setProfileIdToNameMap(profileIdToNameMap);

        return matchChard;
    }

    /**
     *
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
     * Splits doubles team player profiles separated by ; and combines their full names
     * @param profileIdToNameMap
     * @param teamPlayerProfileIds
     * @return
     */
    private String getDoublesTeamPlayerNames(Map<String, String> profileIdToNameMap, String teamPlayerProfileIds) {
        String[] teamProfilesIds = teamPlayerProfileIds.split(";");
        String teamPlayersNames = null;
        for (String teamAPlayerProfile : teamProfilesIds) {
            String playerFullName = profileIdToNameMap.get(teamAPlayerProfile);
            teamPlayersNames = (teamPlayersNames == null) ? playerFullName : (teamPlayersNames + " / " + playerFullName);
        }
        return teamPlayersNames;
    }

    /**
     *
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

    public void deleteAllForEventAndDrawType(long eventId, DrawType drawType) {
        this.matchCardRepository.deleteAllByEventFkAndDrawType(eventId, drawType);
    }

    public void delete(long eventId, DrawType drawType, int groupNum) {
        MatchCard matchCard = new MatchCard();
        matchCard.setEventFk(eventId);
        matchCard.setGroupNum(groupNum);
        matchCard.setDrawType(drawType);
        this.matchCardRepository.delete(matchCard);
    }

}
