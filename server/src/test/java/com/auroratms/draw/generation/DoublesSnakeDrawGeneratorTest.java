package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.doubles.DoublesPair;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DoublesSnakeDrawGeneratorTest extends AbstractDoublesDrawsGeneratorTest {

//    @Test
//    public void testOpenDoublesSeed1Team() {
//        testDrawGeneration(1);
//    }

    @Test
    public void testOpenDoublesNoSeededTeams() {
        testDrawGeneration(0, 4);
    }

    private void testDrawGeneration(int numTeamsToSeed, int expectedGroups) {
        TournamentEvent tournamentEvent = makeTournamentEventEntity(numTeamsToSeed);

        List<TournamentEventEntry> eventEntries = makeDoublesTournamentEntriesList(153L, tournamentEvent.getId());

        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = makeDoublesPlayerDrawInfos();

        List<DoublesPair> doublesPairList = makeDoublesPairs(tournamentEvent);

        // first event draw
        List<DrawItem> existingDrawItems = new ArrayList<>();

        IDrawsGenerator rrRoundGenerator = DrawGeneratorFactory.makeGenerator(tournamentEvent, DrawType.ROUND_ROBIN);
        ((DoublesSnakeDrawsGenerator)rrRoundGenerator).setDoublesPairs(doublesPairList);

        List<DrawItem> drawItems = rrRoundGenerator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDrawItems);
        assertEquals(14, drawItems.size(), "wrong number of draws");

        String [] expectedResults = {
                "1 / 1  Monteiro, Thiago Farias / Ventura Dos Anj, Bruno (5303)",
                "1 / 2  Zhong, Zongqi Henry / Chu, Stephen (4821)",
                "1 / 3  Jiang, Kai / Naresh, Nandan (4750)",
                "2 / 1  Alguetti, Gal / Alguetti, Sharon (5288)",
                "2 / 2  Friend, Chance / Wahab, Wale (4943)",
                "2 / 3  Sosis, Eliel / Zhu, Sabrina (4570)",
                "3 / 1  Xi, Sheng / Wang, Chen (5226)",
                "3 / 2  Zhao, Daming / Tian, Ye (5097)",
                "3 / 3  Chandra, Vinay / Kini, Vivek (4302)",
                "3 / 4  Higuera, Mauricio Reyes / Reyes, Sebastian (3627)",
                "4 / 1  Zhang, Tianrui / Liu, Dan (5182)",
                "4 / 2  Naresh, Sid / Zhang, Yichi (5124)",
                "4 / 3  Wang, Rachel / Lu, Changbo (4138)",
                "4 / 4  Slomski, Adam / Gacki, Slawomir (3782)"
        };
        Set<Integer> uniqueGroups = new HashSet<>();
        int index = 0;
        for (DrawItem drawItem : drawItems) {
            uniqueGroups.add(drawItem.getGroupNum());
            String actualResult = "%d / %d  %s (%d)".formatted(
                    drawItem.getGroupNum(), drawItem.getPlaceInGroup(), drawItem.getPlayerName(), drawItem.getRating());
            String expectedResult = expectedResults[index];
            index++;
            //System.out.println(result);
            assertEquals(expectedResult, actualResult, "wrong paring");
        }
        assertEquals(expectedGroups, uniqueGroups.size(), "wrong number of groups");
    }

}
