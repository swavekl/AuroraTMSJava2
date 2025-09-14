package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.draw.generation.singleelim.SingleEliminationEntriesConverter;
import com.auroratms.event.DrawMethod;
import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SingleEliminationDrawsGeneratorTest extends AbstractDrawsGeneratorTest {

    @Test
    public void test6Players() {
        TournamentEvent tournamentEvent = new TournamentEvent();
        tournamentEvent.setId(55L);
        tournamentEvent.setPlayersPerGroup(4);
        tournamentEvent.setPlayersToSeed(0);
        tournamentEvent.setPlayersToAdvance(1);
        tournamentEvent.setDrawMethod(DrawMethod.SNAKE);

        // make RR round entries
        List<TournamentEventEntry> eventEntries = this.makeTournamentEntriesList();
        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = this.makePlayerDrawInfos();
        List<DrawItem> existingDrawItems = Collections.emptyList();
        IDrawsGenerator rrRoundGenerator = DrawGeneratorFactory.makeGenerator(tournamentEvent, DrawType.ROUND_ROBIN);
        List<DrawItem> rrDrawItems = rrRoundGenerator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDrawItems);
        assertEquals(23, rrDrawItems.size(), "wrong RR draws");

        // pick top n players to advance and get their entries
        List<TournamentEventEntry> seEventEntries = SingleEliminationEntriesConverter.generateSEEventEntriesFromDraws(
                rrDrawItems, eventEntries, tournamentEvent, entryIdToPlayerDrawInfo);
        SingleEliminationEntriesConverter.fillRRGroupNumberForSEPlayers(rrDrawItems, entryIdToPlayerDrawInfo, tournamentEvent);
        assertEquals(6, seEventEntries.size(), "wrong number of advancing player entries");

        // make SE draws
        IDrawsGenerator seRoundGenerator = DrawGeneratorFactory.makeGenerator(tournamentEvent, DrawType.SINGLE_ELIMINATION);
        assertNotNull(seRoundGenerator, "SE round generator is null");

        List<DrawItem> seDrawItems = seRoundGenerator.generateDraws(seEventEntries, entryIdToPlayerDrawInfo, existingDrawItems);
        assertEquals(14, seDrawItems.size(), "wrong number of SE entries");

        int i = 0;
        int[] expectedSeedNumbers = {1, 8, 6, 3, 4, 5, 7, 2};
        int[] expectedByes = {0, 1, 0, 0, 0, 0, 2, 0};
        for (DrawItem drawItem : seDrawItems) {
            int seedNumber = drawItem.getSeSeedNumber();
//            System.out.print(seedNumber + ", ");
            int expectedSeedNum = expectedSeedNumbers[i];
            assertEquals(expectedSeedNum, seedNumber, "wrong seed number");

            int expectedBye = expectedByes[i];
            if (expectedBye != 0) {
                assertEquals(expectedBye, drawItem.getByeNum(), "wrong bye ");
            }
            System.out.println("drawItem = " + drawItem.getPlayerName());

            i++;
        }
    }

    /**
     * EXAM example
     */
    @Test
    public void testExamDrawOf32() {
        long tournamentFk = 100L;
        long eventFk = 200L;

        TournamentEvent tournamentEvent = new TournamentEvent();
        tournamentEvent.setId(eventFk);
        tournamentEvent.setTournamentFk(tournamentFk);
        tournamentEvent.setPlayersPerGroup(4);
        tournamentEvent.setPlayersToSeed(0);
        tournamentEvent.setPlayersToAdvance(1);
        tournamentEvent.setDrawMethod(DrawMethod.SNAKE);
        tournamentEvent.setPlay3rd4thPlace(true);

        // make RR round entries
        List<TournamentEventEntry> seEventEntries = this.makeExamTournamentEntries(tournamentFk, eventFk);
        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = this.makeExamPlayerDrawInfos();
        List<DrawItem> existingDrawItems = Collections.emptyList();
//        IDrawsGenerator rrRoundGenerator = DrawGeneratorFactory.makeGenerator(tournamentEventEntity, DrawType.ROUND_ROBIN);
//        List<DrawItem> rrDrawItems = rrRoundGenerator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDrawItems);
//        assertEquals("wrong RR draws", eventEntries.size(), rrDrawItems.size());

        // pick top n players to advance and get their entries
//        List<TournamentEventEntry> seEventEntries = this.makeSeEntriesFromRREntries(rrDrawItems, eventEntries, tournamentEventEntity, entryIdToPlayerDrawInfo);
//        assertEquals("wrong number of advancing player entries", 32, seEventEntries.size());

        // make SE draws
//        SingleEliminationEntriesConverter.fillRRGroupNumberForSEPlayers(rrDrawItems, entryIdToPlayerDrawInfo, tournamentEventEntity);
        IDrawsGenerator seRoundGenerator = DrawGeneratorFactory.makeGenerator(tournamentEvent, DrawType.SINGLE_ELIMINATION);
        assertNotNull(seRoundGenerator, "SE round generator is null");

        List<DrawItem> seDrawItems = seRoundGenerator.generateDraws(seEventEntries, entryIdToPlayerDrawInfo, existingDrawItems);
        assertEquals(64, seDrawItems.size(), "wrong number of SE entries");

        int i = 0;
        int[] expectedSeedNumbers = {1, 8, 5, 3, 4, 6, 7, 2};
        int[] expectedByes = {
                0, 1, 0, 0, 0, 0, 5, 0,
                0, 0, 0, 0, 0, 0, 3, 0,
                0, 4, 0, 0, 0, 0, 0, 0,
                0, 6, 0, 0, 0, 0, 2, 0};
        for (DrawItem drawItem : seDrawItems) {
            int seedNumber = drawItem.getSeSeedNumber();
//            System.out.print(seedNumber + ", ");
//            int expectedSeedNum = expectedSeedNumbers[i];
//            assertEquals("wrong seed number", expectedSeedNum, seedNumber);

//            int expectedBye = expectedByes[i];
//            if (expectedBye != 0) {
//                assertEquals("wrong bye at index " + i, expectedBye, drawItem.getByeNum());
//            }
            if (drawItem.getByeNum() == 0) {
                System.out.println("%2d)\t%25s,%4d".formatted((i + 1), drawItem.getPlayerName(), drawItem.getRating()));
            } else {
                System.out.println("%2d)\t%23s %d, 0".formatted((i + 1), "Bye", drawItem.getByeNum()));
            }

            i++;
        }
    }

    private List<TournamentEventEntry> makeExamTournamentEntries(long tournamentFk, long eventFk) {
        List<TournamentEventEntry> eventEntries = new ArrayList<>();
        for (int i = 0; i < 28; i++) {
            long eventEntry = 300 + i;
            long tournamentEntryFk = 400 + i;
            eventEntries.add(makeTournamentEventEntry(eventEntry, tournamentFk, eventFk, tournamentEntryFk));
        }
        return eventEntries;
    }

    private Map<Long, PlayerDrawInfo> makeExamPlayerDrawInfos() {
        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = new HashMap<>();
        long tournamentEntryFk = 400L;
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy334yet1s48amI0h7", "Morgenroth, Kyle", 2121, 0L, "CA"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy335yet1s48amI0h7", "Nguyen, Tai", 2072, 0L, "OR"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy336yet1s48amI0h7", "Gingold, Greg B.", 2023, 0L, "FL"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy337yet1s48amI0h7", "Walla, Sonny", 2013, 0L, "CA"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy338yet1s48amI0h7", "Lehman, Christopher B.", 2008, 0L, "NJ"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy339yet1s48amI0h7", "Page, Robert", 1995, 0L, "MA"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy340yet1s48amI0h7", "Harbeck, Gary D.", 1993, 0L, "FL"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy341yet1s48amI0h7", "Gordon, Scott", 1981, 0L, "CA"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy342yet1s48amI0h7", "McQueen, Jim", 1977, 0L, "NC"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy343yet1s48amI0h7", "Shurslep, Alex", 1954, 0L, "MN"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy344yet1s48amI0h7", "Fang, Hong", 1893, 0L, "NJ"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy345yet1s48amI0h7", "Rautis, James", 1888, 0L, "TX"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy346yet1s48amI0h7", "Li, George", 1885, 0L, "MD"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy347yet1s48amI0h7", "Scott, Albert", 1885, 0L, "NY"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy348yet1s48amI0h7", "Griffin, Thomas M.", 1875, 0L, "NC"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy349yet1s48amI0h7", "Camas, Steve", 1826, 0L, "NY"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy350yet1s48amI0h7", "Stanley, Richard", 1822, 0L, "TX"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy351yet1s48amI0h7", "Chu, Sammy", 1821, 0L, "CA"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy352yet1s48amI0h7", "Moayery, Mohammad", 1816, 0L, "CA"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy353yet1s48amI0h7", "Nguyen, Aaron", 1785, 0L, "MN"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy354yet1s48amI0h7", "Wei, George", 1759, 0L, "MA"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy355yet1s48amI0h7", "Nguyen, Nha H.", 1732, 0L, "OR"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy356yet1s48amI0h7", "Babuin, Mike", 1728, 0L, "NC"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy356yet1s48amI0h7", "Helfand, Joseph S.", 1474, 0L, "MI"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy358yet1s48amI0h7", "Nguyen, Steve", 1438, 0L, "TX"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy359yet1s48amI0h7", "Chee, Ethan", 1437, 0L, "NJ"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy360yet1s48amI0h7", "Kronlage, Yvonne M.", 1427, 0L, "MD"));
        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy361yet1s48amI0h7", "Richardson, Henry", 1235, 0L, "NY"));
        return entryIdToPlayerDrawInfo;
    }
}

/*

//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy334yet1s48amI0h7", "Bishop, Shannon", 1825, 0L, "AL"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy335yet1s48amI0h7", "Brown, Adam", 1989, 0L, "AL"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy336yet1s48amI0h7", "Brown, Sean", 1822, 0L, "AL"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy337yet1s48amI0h7", "Cardenas, Eduardo", 1886, 0L, "FL"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy338yet1s48amI0h7", "Chen, John", 1703, 0L, "MD"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy339yet1s48amI0h7", "Dong, Chun", 1887, 0L, "MD"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy340yet1s48amI0h7", "Driskill, Ryan", 1847, 0L, "IL"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy341yet1s48amI0h7", "Gaidarev, Peter", 1747, 0L, "MA"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy342yet1s48amI0h7", "Gustavson, Jon", 1838, 0L, "GA"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy343yet1s48amI0h7", "Haugh, David", 1885, 0L, "MA"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy344yet1s48amI0h7", "Hong, Hyu-Joon", 1929, 0L, "CA"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy345yet1s48amI0h7", "Landry, David", 2130, 0L, "AL"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy346yet1s48amI0h7", "Laronde, Roy", 1939, 0L, "GA"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy347yet1s48amI0h7", "Liao, Chao", 2092, 0L, "AL"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy348yet1s48amI0h7", "Obrian, Chris", 1881, 0L, "NC"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy349yet1s48amI0h7", "Oh, Ken", 1922, 0L, "IL"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy350yet1s48amI0h7", "Page, Robert", 1995, 0L, "MA"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy351yet1s48amI0h7", "Radom, Mark", 1966, 0L, "MD"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy352yet1s48amI0h7", "Richardson, Henry", 1400, 0L, "NY"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy353yet1s48amI0h7", "Rivero, Carlos", 1728, 0L, "FL"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy354yet1s48amI0h7", "Shanker, Srividhya", 1776, 0L, "CA"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy355yet1s48amI0h7", "Tran, Chi", 1975, 0L, "NC"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy356yet1s48amI0h7", "Wang, Xinyang", 2102, 0L, "CA"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy356yet1s48amI0h7", "Xiano, Kun", 1708, 0L, "NY"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy358yet1s48amI0h7", "Yen, Raymond", 1943, 0L, "NY"));
//        entryIdToPlayerDrawInfo.put(tournamentEntryFk++, makePlayerDrawInfo("00uy359yet1s48amI0h7", "Zhuang, Jian", 2113, 0L, "CA"));



Landry, David	2130
Bye

Xiano, Kun	1708

Haugh, David	1885

Hong, Hyu-Joon	1929

Brown, Sean	1822

Bye

Tran, Chi	1975

Brown, Adam	1989

Rivero, Carlos	1728

Gustavson, Jon	1838

Oh, Ken	1922

Dong, Chun	1887

Gaidarev, Peter	1747

Bye

Wang, Xinyang	2102

Liao, Chao	2092

Bye

Richardson, Henry	1400*

O'Brian, Chris	1881

Cardenas, Eduardo	1886

Shanker, Srividhya	1776

Driskill, Ryan	1847

Radom, Mark	1966

Page, Robert	1995

Bye

Chen, John	1703

Laronde, Roy	1939

Yen, Raymond	1943

Bishop, Shannon	1825

Bye

Zhuang, Jian	2113





 */
