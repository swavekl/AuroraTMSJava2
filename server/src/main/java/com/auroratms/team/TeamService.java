package com.auroratms.team;

import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.team.notification.TeamChangedEventPublisher;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.usatt.RatingHistoryRecord;
import com.auroratms.usatt.RatingHistoryRecordRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private UserProfileService profileService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private RatingHistoryRecordRepository ratingHistoryRecordRepository;

    @Autowired
    private TeamChangedEventPublisher teamChangedEventPublisher;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Team save(Team team) {
        List<String> previousProfileIds = new ArrayList<>();
        if (team.getId() != null) {
            // Direct SQL bypasses Hibernate's cache entirely
            String sql = "SELECT profile_id FROM team_member WHERE team_fk = ?";
            previousProfileIds = jdbcTemplate.queryForList(sql, String.class, team.getId());

            System.out.println("previousProfileIds.size() = " + previousProfileIds.size());
        } else {
            System.out.println("New team");
        }

        Team savedTeam = teamRepository.save(team);

        // team captain already has an entry because he entered the tournament and team event
        // so there is no need to create one since it would result in double entries
        boolean isCaptain = false;
        List<TeamMember> teamMembers = savedTeam.getTeamMembers();
        if (teamMembers.size() == 1 && previousProfileIds.isEmpty()) {
            TeamMember teamMember = teamMembers.get(0);
            isCaptain = teamMember.isCaptain();
        }

        if (!isCaptain) {
            // Publish: "Here is the team now, and here is a list of who WAS in it."
            teamChangedEventPublisher.publishTeamSavedEvent(savedTeam, previousProfileIds);
        }

        return savedTeam;
    }

    public Team getTeamById(Long teamId) {
        return teamRepository.getTeamById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team " + teamId + " not found"));
    }

    /**
     * Lists all teams which entered one of the events
     *
     * @param eventIdsList
     * @return
     */
    public List<Team> listForEvents(List<Long> eventIdsList) {
        return teamRepository.findByTournamentEventFkIsIn(eventIdsList);
    }

    /**
     * Gets a player's team memberships for a specific tournament's team events.
     *
     * @param teamEventIds    The list of team event ids
     * @param profileId       The player's profile ID
     * @param tournamentId    Tournament id
     * @return List of TeamMembers with hydrated player names and initialized Team objects
     */
    public List<Team> listByEventIdsAndProfile(List<Long> teamEventIds, String profileId, long tournamentId) {
        if (teamEventIds.isEmpty()) {
            return new ArrayList<>();
        }

        Tournament tournament = this.tournamentService.getByKey(tournamentId);
        Date eligibilityDate = tournament.getConfiguration().getEligibilityDate();

        // 1. Efficiently fetch Members + Teams in one hit (Avoids N+1)
        List<TeamMember> teamMembers = teamMemberRepository.findAllByProfileIdAndEventIds(teamEventIds, profileId);
        if (teamMembers.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Collect all unique profile IDs from these teams to hydrate names
        // Since we want to show the player names for the entire team roster
        Set<String> allMemberProfileIds = teamMembers.stream()
                .map(TeamMember::getTeam)
                .flatMap(team -> team.getTeamMembers().stream())
                .map(TeamMember::getProfileId)
                .collect(Collectors.toSet());

        // 3. make a call and convert collection to a list for fast lookup
        List<String> userProfileList = allMemberProfileIds.stream().toList();
        Collection<UserProfile> userProfiles = profileService.listByProfileIds(userProfileList);
        Map<String, String> profileToNameMap = userProfiles.stream()
                .collect(Collectors.toMap(
                        UserProfile::getUserId,
                        up -> up.getLastName() + ", " + up.getFirstName(),
                        (existing, replacement) -> existing // Merge function to handle duplicate IDs if necessary
                ));

        // 4. Get eligibility ratings for these players
        Map<String, UserProfileExt> profileIdToProfileExtMap = userProfileExtService.findByProfileIds(userProfileList);
        List<@NonNull Long> membershipIds = profileIdToProfileExtMap.values().stream().map(UserProfileExt::getMembershipId).toList();
        List<RatingHistoryRecord> eligibilityRatingsList = ratingHistoryRecordRepository.getBatchPlayerRatingsAsOfDate(membershipIds, eligibilityDate);

        // 6. Hydrate all members and extract unique Teams
        List<Team> retValue = teamMembers.stream()
                .map(TeamMember::getTeam)
                .distinct() // Ensure each team appears only once in the list
                .collect(Collectors.toList());

        // now that we have properly retrieved team members, we can assign names
        // and eligibility ratings
        for (Team team : retValue) {
            List<TeamMember> teamMembers1 = team.getTeamMembers();
            teamMembers1.forEach(teamMember -> {
                teamMember.setPlayerName(profileToNameMap.get(teamMember.getProfileId()));
                UserProfileExt userProfileExt = profileIdToProfileExtMap.get(teamMember.getProfileId());
                if (userProfileExt != null) {
                    Long membershipId = userProfileExt.getMembershipId();
                    List<RatingHistoryRecord> playersRatingHistoryList = eligibilityRatingsList.stream()
                            .filter(ratingHistoryRecord -> ratingHistoryRecord.getMembershipId() == membershipId)
                            .toList();
                    if (!playersRatingHistoryList.isEmpty()) {
                        int rating = playersRatingHistoryList.get(0).getFinalRating();
                        teamMember.setPlayerRating(rating);
                    }
                }
            });
        }

        return retValue;
    }

    public void delete(Long teamId) {
        teamRepository.deleteById(teamId);
    }
}
