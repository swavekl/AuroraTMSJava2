package com.auroratms.umpire;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.Personnel;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.users.UserRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class UmpiringService {

    @Autowired
    private UmpireWorkRepository umpireWorkRepository;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private UserProfileService userProfileService;

    /**
     * Assigns umpire to work the match
     *
     * @param umpireWork
     */
    public void assignOfficials(UmpireWork umpireWork) {
        List<String> umpireProfileIds = new ArrayList<>();
        umpireProfileIds.add(umpireWork.getUmpireProfileId());
        if (umpireWork.getAssistantUmpireProfileId() != null) {
            umpireProfileIds.add(umpireWork.getAssistantUmpireProfileId());
        }
        String umpireName = null;
        String assistantUmpireName = null;
        Collection<UserProfile> umpireProfiles = userProfileService.listByProfileIds(umpireProfileIds);
        for (UserProfile umpireProfile : umpireProfiles) {
            if (umpireProfile.getUserId().equals(umpireWork.getUmpireProfileId())) {
                umpireName = umpireProfile.getLastName() + ", " + umpireProfile.getFirstName();
            }

            if (umpireWork.getAssistantUmpireProfileId() != null &&
                    umpireProfile.getUserId().equals(umpireWork.getAssistantUmpireProfileId())) {
                assistantUmpireName = umpireProfile.getLastName() + ", " + umpireProfile.getFirstName();
            }
        }

        Match match = matchService.getMatch(umpireWork.getMatchFk());
        match.setUmpireName(umpireName);
        match.setAssistantUmpireName(assistantUmpireName);
        match.setMatchUmpired(true);
        matchService.updateMatch(match);

        // create
        umpireWorkRepository.saveAndFlush(umpireWork);
    }

    /**
     * Gets summary of matches umpired by each umpire assigned to this tournament
     *
     * @param tournamentId
     * @return
     */
    public List<UmpireWorkSummary> getSummaries(Long tournamentId) {
        // fill the map with 0 counts
        Map<String, Integer> mapUmpireToNumUmpiredMatches = new HashMap<>();
        Map<String, Integer> mapAssistantUmpireToNumAssistantUmpiredMatches = new HashMap<>();

        Tournament tournament = this.tournamentService.getByKey(tournamentId);
        List<Personnel> personnelList = tournament.getConfiguration().getPersonnelList();
        List<Personnel> umpireList = personnelList.stream().filter(personnel -> {
            return personnel.getRole().equals(UserRoles.Umpires);
        }).collect(Collectors.toList());
        for (Personnel umpire : umpireList) {
            String umpireProfileId = umpire.getProfileId();
            mapUmpireToNumUmpiredMatches.computeIfAbsent(umpireProfileId, k -> 0);
            mapAssistantUmpireToNumAssistantUmpiredMatches.computeIfAbsent(umpireProfileId, l -> 0);
        }

        // compute the counts of matches which were umpired or assistant umpired by each umpire
        List<UmpireWork> umpireWorkEntities = umpireWorkRepository.findByTournamentFk(tournamentId);
        List<Long> matchIds = new ArrayList<>();
        for (UmpireWork umpireWork : umpireWorkEntities) {
            String umpireProfileId = umpireWork.getUmpireProfileId();
            Integer countUmpiredMatches = mapUmpireToNumUmpiredMatches.get(umpireProfileId);
            countUmpiredMatches++;
            mapUmpireToNumUmpiredMatches.put(umpireProfileId, countUmpiredMatches);

            // same for matches served as assistant umpire
            String assistantUmpireProfileId = umpireWork.getAssistantUmpireProfileId();
            Integer countAssistantUmpiredMatches = mapAssistantUmpireToNumAssistantUmpiredMatches.get(assistantUmpireProfileId);
            countAssistantUmpiredMatches++;
            mapAssistantUmpireToNumAssistantUmpiredMatches.put(assistantUmpireProfileId, countAssistantUmpiredMatches);

            // collect all match ids to determine which ones are in progress
            matchIds.add(umpireWork.getMatchFk());
        }

        // get all matches in progress
        List<Match> matchList = matchService.findAllByIdIn(matchIds);
        Collection<TournamentEvent> tournamentEvents = tournamentEventEntityService.list(tournamentId, Pageable.unpaged());
        List<Long> matchesInProgress = findMatchesInProgress(matchList, tournamentEvents);
        Map<String, Boolean> umpireProfileIdToBusyFlagMap = new HashMap<>();
        for (UmpireWork umpireWork : umpireWorkEntities) {
            for (Long matchFk : matchesInProgress) {
                if (umpireWork.getMatchFk() == matchFk) {
                    umpireProfileIdToBusyFlagMap.put(umpireWork.getUmpireProfileId(), Boolean.TRUE);
                    umpireProfileIdToBusyFlagMap.put(umpireWork.getAssistantUmpireProfileId(), Boolean.TRUE);
                    break;
                }
            }
        }

        // fill summaries
        List<UmpireWorkSummary> umpireWorkSummaries = new ArrayList<>();
        for (Personnel umpire : umpireList) {
            String umpireProfileId = umpire.getProfileId();
            UmpireWorkSummary umpireWorkSummary = new UmpireWorkSummary();
            umpireWorkSummary.setProfileId(umpireProfileId);
            umpireWorkSummary.setUmpireName(umpire.getName());
            umpireWorkSummary.setNumUmpiredMatches(mapUmpireToNumUmpiredMatches.get(umpireProfileId));
            umpireWorkSummary.setNumAssistantUmpiredMatches(mapAssistantUmpireToNumAssistantUmpiredMatches.get(umpireProfileId));
            Boolean busy = umpireProfileIdToBusyFlagMap.get(umpireProfileId);
            busy = (busy != null) ? busy : Boolean.FALSE;
            umpireWorkSummary.setBusy(busy);

            umpireWorkSummaries.add(umpireWorkSummary);
        }

        return umpireWorkSummaries;
    }

    /**
     * @param matchList
     * @param tournamentEvents
     * @return
     */
    private List<Long> findMatchesInProgress(List<Match> matchList, Collection<TournamentEvent> tournamentEvents) {
        List<Long> matchesInProgress = new ArrayList<>();
        for (Match match : matchList) {
            MatchCard matchCard = match.getMatchCard();
            long eventFk = matchCard.getEventFk();
            for (TournamentEvent tournamentEvent : tournamentEvents) {
                if (tournamentEvent.getId() == eventFk) {
                    int pointsPerGame = tournamentEvent.getPointsPerGame();
                    boolean matchFinished = match.isMatchFinished(matchCard.getNumberOfGames(), pointsPerGame);
                    byte game1ScoreSideA = match.getGame1ScoreSideA();
                    byte game1ScoreSideB = match.getGame1ScoreSideB();
                    boolean matchStarted = (game1ScoreSideA > 0 || game1ScoreSideB > 0);
                    if (matchStarted && !matchFinished) {
                        matchesInProgress.add(match.getId());
                    }
                }
            }
        }
        return matchesInProgress;
    }

    /**
     * @param eventFk
     * @param tournamentEvents
     * @return
     */
    private TournamentEvent findTournamentEvent(long eventFk, Collection<TournamentEvent> tournamentEvents) {
        for (TournamentEvent tournamentEvent : tournamentEvents) {
            if (tournamentEvent.getId() == eventFk) {
                return tournamentEvent;
            }
        }
        return null;
    }

    /**
     * Gets a list of all matches at all tournament umpired by this umpire
     *
     * @param umpireProfileId
     * @return
     */
    public List<UmpiredMatchInfo> getUmpiredMatches(String umpireProfileId) {
        // find all umpired matches by this umpire (as umpire or assistant umpire)
        List<UmpireWork> umpireWorkEntities = umpireWorkRepository.findByUmpireProfileId(umpireProfileId);
        List<Long> matchIds = new ArrayList<>(umpireWorkEntities.size());
        Set<Long> uniqueTournamentIds = new HashSet<>();
        Set<Long> uniqueEventIds = new HashSet<>();
        for (UmpireWork umpireWork : umpireWorkEntities) {
            matchIds.add(umpireWork.getMatchFk());
            uniqueTournamentIds.add(umpireWork.getTournamentFk());
            uniqueEventIds.add(umpireWork.getEventFk());
        }

        // get matches
        List<Match> matchList = matchService.findAllByIdIn(matchIds);

        // get event ids, player profile ids so we can get names
        List<UmpiredMatchInfo> umpiredMatchInfos = new ArrayList<>(matchList.size());
        Set<String> uniquePlayerProfileIds = new HashSet<>();
        for (Match match : matchList) {
            String playerAProfileId = match.getPlayerAProfileId();
            if (playerAProfileId.indexOf(";") > 0) {
                String[] doublePlayerNames = playerAProfileId.split(";");
                uniquePlayerProfileIds.add(doublePlayerNames[0]);
                uniquePlayerProfileIds.add(doublePlayerNames[1]);
            } else {
                uniquePlayerProfileIds.add(playerAProfileId);
            }

            String playerBProfileId = match.getPlayerBProfileId();
            if (playerBProfileId.indexOf(";") > 0) {
                String[] doublePlayerNames = playerBProfileId.split(";");
                uniquePlayerProfileIds.add(doublePlayerNames[0]);
                uniquePlayerProfileIds.add(doublePlayerNames[1]);
            } else {
                uniquePlayerProfileIds.add(playerBProfileId);
            }
        }

        // get unique tournament ids
        List<Long> eventIdsList = new ArrayList<>(uniqueEventIds);
        Map<Long, Long> eventToTournamentIdMap = new HashMap<>();
        List<TournamentEvent> allEvents = tournamentEventEntityService.findAllById(eventIdsList);
        for (TournamentEvent tournamentEvent : allEvents) {
            long tournamentFk = tournamentEvent.getTournamentFk();
            eventToTournamentIdMap.put(tournamentEvent.getId(), tournamentFk);
        }

        // get player profiles to get their names
        List<String> profileIdList = new ArrayList<>(uniquePlayerProfileIds);
        Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(profileIdList);

        List<Long> tournamentIdsList = new ArrayList<>(uniqueTournamentIds);
        Collection<Tournament> tournaments = tournamentService.listTournamentsByIds(tournamentIdsList);
        for (Match match : matchList) {
            UmpiredMatchInfo umpiredMatchInfo = new UmpiredMatchInfo();
            MatchCard matchCard = match.getMatchCard();
            long eventFk = matchCard.getEventFk();
            Long tournamentId = eventToTournamentIdMap.get(eventFk);

            // set tournament name
            for (Tournament tournament : tournaments) {
                if (Objects.equals(tournament.getId(), tournamentId)) {
                    umpiredMatchInfo.setTournamentName(tournament.getName());
                    break;
                }
            }

            // set event name
            for (TournamentEvent tournamentEvent : allEvents) {
                if (tournamentEvent.getId() == matchCard.getEventFk()) {
                    umpiredMatchInfo.setEventName(tournamentEvent.getName());
                    break;
                }
            }

            // match description
            umpiredMatchInfo.setRoundName(matchCard.getRoundName());

            // set player names
            String playerAName = mapProfileIdsToNames(match.getPlayerAProfileId(), userProfiles);
            umpiredMatchInfo.setPlayerAName(playerAName);
            String playerBName = mapProfileIdsToNames(match.getPlayerBProfileId(), userProfiles);
            umpiredMatchInfo.setPlayerBName(playerBName);

            // set match results
            String matchScore = getCompactResult(match, allEvents, matchCard);
            umpiredMatchInfo.setMatchScore(matchScore);

            boolean wasAssistantUmpire = wasAssistantUmpire(umpireProfileId, match.getId(), umpireWorkEntities);
            umpiredMatchInfo.setAssistantUmpire(wasAssistantUmpire);

            umpiredMatchInfos.add(umpiredMatchInfo);
        }

        return umpiredMatchInfos;
    }

    /**
     *
     * @param umpireProfileId
     * @param matchId
     * @param umpireWorkEntities
     * @return
     */
    private boolean wasAssistantUmpire(String umpireProfileId, long matchId, List<UmpireWork> umpireWorkEntities) {
        for (UmpireWork umpireWorkEntity : umpireWorkEntities) {
            if (umpireWorkEntity.getMatchFk() == matchId) {
                if (umpireWorkEntity.getUmpireProfileId().equals(umpireProfileId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param match
     * @param allEvents
     * @param matchCard
     * @return
     */
    private String getCompactResult(Match match, List<TournamentEvent> allEvents, MatchCard matchCard) {
        int pointsPerGame = 11;
        for (TournamentEvent tournamentEvent : allEvents) {
            if (tournamentEvent.getId() == matchCard.getEventFk()) {
                pointsPerGame = tournamentEvent.getPointsPerGame();
                break;
            }
        }
        int numberOfGames = matchCard.getNumberOfGames();
        return match.getCompactResult(numberOfGames, pointsPerGame);
    }


    /**
     * @param playerProfileId
     * @param userProfiles
     * @return
     */
    private String mapProfileIdsToNames(String playerProfileId, Collection<UserProfile> userProfiles) {
        String playerNames = "";
        if (playerProfileId.indexOf(";") > 0) {
            String[] profileIds = playerProfileId.split(";");
            playerNames += getPlayerName(userProfiles, profileIds[0]);
            playerNames += " / ";
            playerNames += getPlayerName(userProfiles, profileIds[1]);
        } else {
            playerNames = getPlayerName(userProfiles, playerProfileId);
        }
        return playerNames;
    }

    /**
     * @param userProfiles
     * @param playerProfileId
     * @return
     */
    private String getPlayerName(Collection<UserProfile> userProfiles, String playerProfileId) {
        for (UserProfile userProfile : userProfiles) {
            if (userProfile.getUserId().equals(playerProfileId)) {
                return userProfile.getLastName() + ", " + userProfile.getFirstName();
            }
        }
        return "";
    }
}
