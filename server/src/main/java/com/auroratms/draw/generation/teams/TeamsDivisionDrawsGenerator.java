package com.auroratms.draw.generation.teams;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.draw.generation.AbstractDrawsGenerator;
import com.auroratms.draw.generation.IDrawsGenerator;
import com.auroratms.draw.generation.PlayerDrawInfo;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventRound;
import com.auroratms.event.TournamentEventRoundDivision;
import com.auroratms.team.Team;
import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.*;

/**
 *
 */
public class TeamsDivisionDrawsGenerator extends AbstractDrawsGenerator implements IDrawsGenerator {

    private TournamentEventRound round;
    private TournamentEventRoundDivision tournamentEventRoundDivision;
    private List<Team> eventTeams;

    public TeamsDivisionDrawsGenerator(TournamentEvent tournamentEvent,
                                       TournamentEventRound round,
                                       TournamentEventRoundDivision tournamentEventRoundDivision,
                                       List<Team> eventTeams) {
        super(tournamentEvent);
        this.round = round;
        this.tournamentEventRoundDivision = tournamentEventRoundDivision;
        this.eventTeams = eventTeams;
    }

    /**
     * Generates draws based on existing draws and player information
     *
     * @param eventEntries              entries into the event for which we are generating a draw
     * @param entryIdToPlayerDrawInfo   information about players who entered the event (state, club, rating etc)
     * @param existingDrawItems             draws to other events for conflict resolution
     * @return draws for this event
     */
    public List<DrawItem> generateDraws(List<TournamentEventEntry> eventEntries,
                                        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo,
                                        List<DrawItem> existingDrawItems) {

        long eventFk = this.tournamentEvent.getId();

        // get teams for this event and sort them by team rating in descending order
        List<Team> sortedTeamsForEvent = (eventTeams != null) ?
                eventTeams.stream().sorted(Comparator.comparingInt(Team::getTeamRating).reversed()).toList()
                : Collections.emptyList();
        List<DrawItem> drawItemList = new ArrayList<>(sortedTeamsForEvent.size());
        if (this.tournamentEventRoundDivision != null) {
            // teams per group
            int teamsPerDivision = this.tournamentEventRoundDivision.getPlayersPerGroup();

            // place teams into groups
            int groupNum = 1;
            int rowNum = 1;
            int numEnteredTeams = (eventTeams != null) ? eventTeams.size() : 0;
            for (int i = 0; i < numEnteredTeams; i++) {
                Team team = sortedTeamsForEvent.get(i);
                Long teamId = team.getId();
                // we need some player to be the owner of the team - it will be team fee payer
                Long payerTournamentEntryFk = team.getPayerTournamentEntryFk();
                PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(payerTournamentEntryFk);
                if (playerDrawInfo != null) {
                    DrawItem drawItem = makeDrawItem(eventFk, groupNum, rowNum, playerDrawInfo,
                            DrawType.UP_DOWN_TEAMS, payerTournamentEntryFk);
                    drawItem.setTeamFk(teamId);
                    drawItem.setTeamName(team.getName());
                    drawItemList.add(drawItem);
                }

                // start filling a new group if this one is full
                if (rowNum == teamsPerDivision) {
                    rowNum = 1;
                    groupNum++;
                } else {
                    rowNum++;
                }
            }
        }
        return drawItemList;
    }
}
