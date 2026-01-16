package com.auroratms.draw.generation.teams;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.generation.PlayerDrawInfo;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventRound;
import com.auroratms.event.TournamentEventRoundDivision;
import com.auroratms.event.TournamentRoundsConfiguration;
import com.auroratms.team.Team;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TeamsDivisionDrawsGeneratorTest extends AbstractTeamsDrawsGeneratorTest {

    @Test
    public void test() {

        TournamentEvent tournamentEvent = this.makeTournamentEventEntity();
        makeAllInfo();
        Map<Long, Team> teamIdToTeamMap = this.getTeamMap();
        List<Team> teams = teamIdToTeamMap.values().stream().toList();
        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = this.getPlayerProfileIdMap();

        TournamentRoundsConfiguration roundsConfiguration = tournamentEvent.getRoundsConfiguration();
        List<TournamentEventRound> rounds = roundsConfiguration.getRounds();
        TournamentEventRound tournamentEventRound = rounds.get(0);
        List<TournamentEventRoundDivision> divisions = tournamentEventRound.getDivisions();
        TournamentEventRoundDivision tournamentEventRoundDivision = divisions.get(0);
        TeamsDivisionDrawsGenerator teamsDrawsGenerator = new TeamsDivisionDrawsGenerator(tournamentEvent,
                tournamentEventRound, tournamentEventRoundDivision, teams);

        List<TournamentEventEntry> eventEntries = new ArrayList<>();
        List<DrawItem> existingDraws = new ArrayList<>();

        List<DrawItem> drawItems = teamsDrawsGenerator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDraws);
        assertEquals(drawItems.size(), 36);

        String[] expectedResults = {
                "1 / 1  MDTTC Beginner Class (2015)",
                "1 / 2  Team Mavericks (2151)",
                "1 / 3  BROWARD TTC 2 (2021)",
                "1 / 4  STADIUM 2 (1660)",
                "1 / 5  Pho King Chopsticks (1827)",
                "1 / 6  Table Titans (1643)",

                "2 / 1  CITTA-North (1821)",
                "2 / 2  Ping Pong Boys (1777)",
                "2 / 3  Trim Team (1877)",
                "2 / 4  Team KS (2003)",
                "2 / 5  DC (1897)",
                "2 / 6  Mix It Up (1600)",

                "3 / 1  Alpha Go (2343)",
                "3 / 2  AM2 (2246)",
                "3 / 3  The Underdogs (2154)",
                "3 / 4  Exceptionally Overrated (2150)",
                "3 / 5  Loop Killaz (2105)",
                "3 / 6  Turkish Coffee (2101)",

                "4 / 1  Ohio Slicers (2028)",
                "4 / 2  Apex TT (1990)",
                "4 / 3  Looping Leprechauns (1890)",
                "4 / 4  Bigsea Table Tennis Academy (1871)",
                "4 / 5  AM1 (1847)",
                "4 / 6  Rising Smashers (1808)",

                "5 / 1  MDTTC boys (1805)",
                "5 / 2  Coastal Cactus (1786)",
                "5 / 3  NC Hurricane (1755)",
                "5 / 4  Scared hitless (1753)",
                "5 / 5  NC Devils (1533)",
                "5 / 6  ZJ's Club (1505)",

                "6 / 1  LYTTC Paddle Panthers (1491)",
                "6 / 2  Side Spinners (1253)",
                "6 / 3  CITTA RC (1122)",
                "6 / 4  Virginia Beach Table Tennis (966)",
                "6 / 5  Racket Ninjas (933)",
                "6 / 6  WAK TTS (0)"
        };
        Set<Integer> uniqueGroups = new HashSet<>();
        int index = 0;
        for (DrawItem drawItem : drawItems) {
            uniqueGroups.add(drawItem.getGroupNum());
            String actualResult = "%d / %d  %s (%d)".formatted(
                    drawItem.getGroupNum(), drawItem.getPlaceInGroup(), drawItem.getTeamName(), drawItem.getRating());
            String expectedResult = expectedResults[index];
            index++;
//            System.out.println(actualResult);
            assertEquals(expectedResult, actualResult, "wrong paring");
        }
//        assertEquals(expectedGroups, uniqueGroups.size(), "wrong number of groups");

    }
}
