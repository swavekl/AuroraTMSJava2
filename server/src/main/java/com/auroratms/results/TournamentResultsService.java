package com.auroratms.results;

import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventConfiguration;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.match.MatchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@Transactional
public class TournamentResultsService {

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private MatchCardService matchCardService;

    public List<EventResultStatus> listEventResultsStatus(long tournamentId) {
        Collection<TournamentEvent> tournamentEvents =
                this.tournamentEventEntityService.list(tournamentId, Pageable.unpaged());
        List<EventResultStatus> eventResultStatusList = new ArrayList<>(tournamentEvents.size());
        for (TournamentEvent tournamentEvent : tournamentEvents) {
            TournamentEventConfiguration configuration = tournamentEvent.getConfiguration();
            Map<Integer, String> finalPlayerRankings =
                    configuration.getFinalPlayerRankings();
            EventResultStatus eventResultStatus = new EventResultStatus();
            eventResultStatus.setEventId(tournamentEvent.getId());
            eventResultStatus.setEventName(tournamentEvent.getName());
            boolean play3rd4thPlace = tournamentEvent.isPlay3rd4thPlace();
            eventResultStatus.setPlay3rd4thPlace(play3rd4thPlace);
            eventResultStatusList.add(eventResultStatus);
            boolean resultsAvailable = (finalPlayerRankings != null && !finalPlayerRankings.isEmpty());
            eventResultStatus.setResultsAvailable(resultsAvailable);
            if (resultsAvailable) {
                String firstPlacePlayer = finalPlayerRankings.get(1);
                eventResultStatus.setFirstPlacePlayer(firstPlacePlayer);
                String secondPlacePlayer = finalPlayerRankings.get(2);
                eventResultStatus.setSecondPlacePlayer(secondPlacePlayer);
                String thirdPlacePlayer = finalPlayerRankings.get(3);
                eventResultStatus.setThirdPlacePlayer(thirdPlacePlayer);
                String fourthPlacePlayer = finalPlayerRankings.get(4);
                eventResultStatus.setFourthPlacePlayer(fourthPlacePlayer);
            }
        }
        // get a list of tournament events
        return eventResultStatusList;
    }

    public List<EventResults> getEventResults(long eventId) {
        TournamentEvent tournamentEvent = this.tournamentEventEntityService.get(eventId);
        List<MatchCard> matchCards = this.matchCardService.findAllForEvent(eventId);
        this.matchCardService.fillPlayerIdToNameMapForAllMatches(matchCards);
        List<EventResults> resultsList = new ArrayList<>(matchCards.size());
        for (MatchCard matchCard : matchCards) {
            boolean singleElimination = (matchCard.getDrawType() == DrawType.SINGLE_ELIMINATION);
            EventResults eventResults = new EventResults();
            eventResults.setSingleElimination(singleElimination);
            eventResults.setGroupNumber(matchCard.getGroupNum());
            eventResults.setRound(matchCard.getRound());
            List<PlayerResults> playerResultsList = getPlayerResults(matchCard, tournamentEvent);
            eventResults.setPlayerResultsList(playerResultsList);
            resultsList.add(eventResults);
        }

        return resultsList;
    }

    private List<PlayerResults> getPlayerResults(MatchCard matchCard, TournamentEvent tournamentEvent) {
        List<PlayerResults> playerResultsList = new ArrayList<>();
        Map<String, String> profileIdToNameMap = matchCard.getProfileIdToNameMap();
        Map<Integer, String> playerRankings = matchCard.getPlayerRankingsAsMap();
        int numPlayers = profileIdToNameMap.size();
        for (Map.Entry<String, String> entry : profileIdToNameMap.entrySet()) {
            PlayerResults playerResults = new PlayerResults();
            playerResultsList.add(playerResults);
            String profileId = entry.getKey();
            String fullName = entry.getValue();
            playerResults.setProfileId(profileId);
            playerResults.setFullName(fullName);
            playerResults.setMatchResults(new ArrayList<>(numPlayers));   // +1 is for A vs A.
            for (Map.Entry<Integer, String> rankingsEntry : playerRankings.entrySet()) {
                if (rankingsEntry.getValue().equals(profileId)) {
                    playerResults.setRank(rankingsEntry.getKey());
                }
            }
        }

        List<Match> matches = matchCard.getMatches();
        for (Match match : matches) {
            String playerAProfileId = match.getPlayerAProfileId();
            String playerBProfileId = match.getPlayerBProfileId();
            Character playerALetter = match.getPlayerALetter();
            Character playerBLetter = match.getPlayerBLetter();
            int playerARating = match.getPlayerARating();
            int playerBRating = match.getPlayerBRating();
            MatchResult matchResult = match.getGamesOnlyResult(matchCard.getNumberOfGames(),
                    tournamentEvent.getPointsPerGame());
            boolean playerAisMatchWinner = match.isMatchWinner(playerAProfileId, matchCard.getNumberOfGames(), tournamentEvent.getPointsPerGame());
            boolean playerBisMatchWinner = match.isMatchWinner(playerBProfileId, matchCard.getNumberOfGames(), tournamentEvent.getPointsPerGame());

            // find the player results
            for (PlayerResults playerResults : playerResultsList) {
                List<MatchResult> matchResults = playerResults.getMatchResults();
                if (playerResults.getProfileId().equals(playerAProfileId)) {
                    playerResults.setLetterCode(playerALetter);
                    playerResults.setRating(playerARating);
                    matchResults.add(matchResult);
                    if (playerAisMatchWinner) {
                        playerResults.addNumMatchesWon();
                    } else if (playerBisMatchWinner) {
                        playerResults.addNumMatchesLost();
                    }
                } else if (playerResults.getProfileId().equals(playerBProfileId)) {
                    playerResults.setLetterCode(playerBLetter);
                    playerResults.setRating(playerBRating);
                    MatchResult reversedMatchResults = matchResult.makeReverse();
                    matchResults.add(reversedMatchResults);
                    if (playerBisMatchWinner) {
                        playerResults.addNumMatchesWon();
                    } else if (playerAisMatchWinner) {
                        playerResults.addNumMatchesLost();
                    }
                }
            }
        }

        // sort match results by opposing player letter A, B, C etc.
        for (PlayerResults playerResults : playerResultsList) {
            List<MatchResult> matchResults = playerResults.getMatchResults();
            // add empty cell for A vs A, B vs B, etc.
            MatchResult empty = new MatchResult();
            empty.setPlayerALetter(playerResults.getLetterCode());
            empty.setPlayerBLetter(playerResults.getLetterCode());
            matchResults.add(empty);
            Collections.sort(matchResults, Comparator.comparing(MatchResult::getPlayerBLetter));
        }

        // sort players results by player letter
        Collections.sort(playerResultsList, Comparator.comparing(PlayerResults::getLetterCode));

        return playerResultsList;
    }
}
