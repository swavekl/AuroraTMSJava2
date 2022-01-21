package com.auroratms.draw.generation;

import com.auroratms.event.DrawMethod;
import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.doubles.DoublesPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractDoublesDrawsGeneratorTest extends AbstractDrawsGeneratorTest {

    protected TournamentEvent makeTournamentEventEntity(int numTeamsToSeed) {
        TournamentEvent tournamentEvent = new TournamentEvent();
        tournamentEvent.setId(56L);
        tournamentEvent.setName("Open Doubles");
        tournamentEvent.setDoubles(true);
        tournamentEvent.setPlayersPerGroup(4);
        tournamentEvent.setPlayersToSeed(numTeamsToSeed);
        tournamentEvent.setPlayersToAdvance(1);
        tournamentEvent.setDrawMethod(DrawMethod.SNAKE);
        return tournamentEvent;
    }

    protected Map<Long, PlayerDrawInfo> makeDoublesPlayerDrawInfos() {
        Map<Long, PlayerDrawInfo> map = new HashMap<>();
//        map.put(485L, makePlayerDrawInfo("00uy334yi8zirhriQ0h7", "Alguetti, Adar", 2552, 0L, "NJ"));
        map.put(486L, makePlayerDrawInfo("00uy334yjg9921xbS0h7", "Alguetti, Gal", 2622, 0L, "NJ"));
        map.put(487L, makePlayerDrawInfo("00uy334ylbSMiaD2u0h7", "Alguetti, Sharon", 2666, 0L, "IN"));
        map.put(518L, makePlayerDrawInfo("00uy33505fJvfuMMT0h7", "Chandra, Vinay", 2200, 0L, "CO"));
        map.put(523L, makePlayerDrawInfo("00uy335093X2LygKv0h7", "Chu, Stephen", 2301, 0L, "NJ"));
        map.put(555L, makePlayerDrawInfo("00uy3351shfaKRxsU0h7", "Friend, Chance", 2515, 0L, "MN"));
        map.put(558L, makePlayerDrawInfo("00uy3351uvvPB1Gqy0h7", "Gacki, Slawomir", 1941, 0L, "OH"));
        map.put(605L, makePlayerDrawInfo("00uy3354hthOsreAw0h7", "Kini, Vivek", 2102, 0L, "IL"));
        map.put(629L, makePlayerDrawInfo("00uy335519iYRhvqC0h7", "Liu, Dan", 2593, 0L, "CA"));
        map.put(659L, makePlayerDrawInfo("00uy33581gJQMU4XU0h7", "Naresh, Nandan", 2329, 0L, "IL"));
        map.put(660L, makePlayerDrawInfo("00uy33582464B1RjK0h7", "Naresh, Sid", 2485, 0L, "IL"));
//        map.put(667L, makePlayerDrawInfo("00uy3358zxep7XMXH0h7", "Olasoji, Yinka", 2577, 0L, ""));
        map.put(730L, makePlayerDrawInfo("00uy335bueGaupupe0h7", "Sosis, Eliel", 2305, 0L, "MA"));
        map.put(752L, makePlayerDrawInfo("00uy335cpe9DjhJnV0h7", "Tian, Ye", 2524, 0L, "MA"));
        map.put(775L, makePlayerDrawInfo("00uy335d9wyYX6gau0h7", "Wang, Chen", 2476, 0L, "NY"));
        map.put(778L, makePlayerDrawInfo("00uy335dcrsBhr0VU0h7", "Wang, Rachel", 2073, 0L, "NC"));
        map.put(794L, makePlayerDrawInfo("00uy335ehz6Dd0KE10h7", "Zhang, Yichi", 2639, 0L, "MS"));
        map.put(796L, makePlayerDrawInfo("00uy335ejfQJulxwJ0h7", "Zhong, Zongqi Henry", 2520, 0L, "NJ"));
        map.put(797L, makePlayerDrawInfo("00uy335ek70iq6cdB0h7", "Zhu, Sabrina", 2265, 0L, "GA"));
        map.put(722L, makePlayerDrawInfo("00uy335bivPV8AE4C0h7", "Slomski, Adam", 1841, 0L, "OH"));
        map.put(653L, makePlayerDrawInfo("00uy3357wpbBIsg0P0h7", "Monteiro, Thiago Farias", 2768, 0L, "FN"));
        map.put(634L, makePlayerDrawInfo("00uy3357gpIJzWdoN0h7", "Lu, Changbo", 2065, 0L, "NC"));
        map.put(692L, makePlayerDrawInfo("00uy3359lz1zwCSh10h7", "Reyes, Sebastian", 1837, 0L, ""));
        map.put(768L, makePlayerDrawInfo("00uy335d4st04F4GZ0h7", "Wahab, Wale", 2428, 0L, ""));
        map.put(793L, makePlayerDrawInfo("00uy335egeZrxkwFb0h7", "Zhang, Tianrui", 2589, 0L, ""));
//        map.put(822L, makePlayerDrawInfo("00uwbdqm3ded2Crth0h7", "Lorenc, Julia", 0, 0L, "null"));
//        map.put(830L, makePlayerDrawInfo("00ux7bip39TYR1mGm0h7", "Lorenc, Tony", 1761, 0L, "null"));
        map.put(578L, makePlayerDrawInfo("00uy33533t98vOX640h7", "Higuera, Mauricio Reyes", 1790, 0L, "IL"));
        map.put(589L, makePlayerDrawInfo("00uy3353d8zRr9dAb0h7", "Jiang, Kai", 2421, 0L, "IL"));
        map.put(765L, makePlayerDrawInfo("00uy335d1vWbvs1zM0h7", "Ventura Dos Anj, Bruno", 2535, 0L, "IL"));
        map.put(788L, makePlayerDrawInfo("00uy335ecnW0TKdeM0h7", "Xi, Sheng", 2750, 0L, "IL"));
        map.put(795L, makePlayerDrawInfo("00uy335eiqdhiWLPx0h7", "Zhao, Daming", 2573, 0L, "IL"));
        return map;
    }

    protected List<TournamentEventEntry> makeDoublesTournamentEntriesList(long tournamentFk, long eventFk) {
        List<TournamentEventEntry> eventEntries = new ArrayList<>();
//        eventEntries.add(makeTournamentEventEntry(1411L, tournamentFk, eventFk, 485L));
        eventEntries.add(makeTournamentEventEntry(1413L, tournamentFk, eventFk, 486L));
        eventEntries.add(makeTournamentEventEntry(1415L, tournamentFk, eventFk, 487L));
        eventEntries.add(makeTournamentEventEntry(1492L, tournamentFk, eventFk, 518L));
        eventEntries.add(makeTournamentEventEntry(1507L, tournamentFk, eventFk, 523L));
        eventEntries.add(makeTournamentEventEntry(1603L, tournamentFk, eventFk, 555L));
        eventEntries.add(makeTournamentEventEntry(1612L, tournamentFk, eventFk, 558L));
        eventEntries.add(makeTournamentEventEntry(1668L, tournamentFk, eventFk, 578L));
        eventEntries.add(makeTournamentEventEntry(1698L, tournamentFk, eventFk, 589L));
        eventEntries.add(makeTournamentEventEntry(1735L, tournamentFk, eventFk, 605L));
        eventEntries.add(makeTournamentEventEntry(1793L, tournamentFk, eventFk, 629L));
        eventEntries.add(makeTournamentEventEntry(1817L, tournamentFk, eventFk, 634L));
        eventEntries.add(makeTournamentEventEntry(1876L, tournamentFk, eventFk, 653L));
        eventEntries.add(makeTournamentEventEntry(1904L, tournamentFk, eventFk, 659L));
        eventEntries.add(makeTournamentEventEntry(1908L, tournamentFk, eventFk, 660L));
//        eventEntries.add(makeTournamentEventEntry(1928L, tournamentFk, eventFk, 667L));
        eventEntries.add(makeTournamentEventEntry(1984L, tournamentFk, eventFk, 692L));
        eventEntries.add(makeTournamentEventEntry(2061L, tournamentFk, eventFk, 722L));
        eventEntries.add(makeTournamentEventEntry(2091L, tournamentFk, eventFk, 730L));
        eventEntries.add(makeTournamentEventEntry(2153L, tournamentFk, eventFk, 752L));
        eventEntries.add(makeTournamentEventEntry(2181L, tournamentFk, eventFk, 765L));
        eventEntries.add(makeTournamentEventEntry(2187L, tournamentFk, eventFk, 768L));
        eventEntries.add(makeTournamentEventEntry(2207L, tournamentFk, eventFk, 775L));
        eventEntries.add(makeTournamentEventEntry(2219L, tournamentFk, eventFk, 778L));
        eventEntries.add(makeTournamentEventEntry(2250L, tournamentFk, eventFk, 788L));
        eventEntries.add(makeTournamentEventEntry(2264L, tournamentFk, eventFk, 793L));
        eventEntries.add(makeTournamentEventEntry(2266L, tournamentFk, eventFk, 794L));
        eventEntries.add(makeTournamentEventEntry(2268L, tournamentFk, eventFk, 795L));
        eventEntries.add(makeTournamentEventEntry(2270L, tournamentFk, eventFk, 796L));
        eventEntries.add(makeTournamentEventEntry(2276L, tournamentFk, eventFk, 797L));
//        eventEntries.add(makeTournamentEventEntry(2378L, tournamentFk, eventFk, 822L));
//        eventEntries.add(makeTournamentEventEntry(2387L, tournamentFk, eventFk, 830L));
        return eventEntries;
    }

    protected List<DoublesPair> makeDoublesPairs(TournamentEvent tournamentEvent) {
        List<DoublesPair> doublesPairList = new ArrayList<>();
        doublesPairList.add(makeDoublesPair (51, 1876, 2181, 5289, 5303, tournamentEvent.getId()));
        doublesPairList.add(makeDoublesPair (52, 1413, 1415, 5273, 5288, tournamentEvent.getId()));
        doublesPairList.add(makeDoublesPair (53, 2250, 2207, 5208, 5226, tournamentEvent.getId()));
        doublesPairList.add(makeDoublesPair (54, 2264, 1793, 5187, 5182, tournamentEvent.getId()));
//        doublesPairList.add(makeDoublesPair (55, 1411, 1928, 5112, 5129, tournamentEventEntity.getId()));
        doublesPairList.add(makeDoublesPair (56, 1908, 2266, 5124, 5124, tournamentEvent.getId()));
        doublesPairList.add(makeDoublesPair (57, 2268, 2153, 5134, 5097, tournamentEvent.getId()));
        doublesPairList.add(makeDoublesPair (58, 1603, 2187, 4939, 4943, tournamentEvent.getId()));
        doublesPairList.add(makeDoublesPair (59, 2270, 1507, 4829, 4821, tournamentEvent.getId()));
        doublesPairList.add(makeDoublesPair (60, 1698, 1904, 4750, 4750, tournamentEvent.getId()));
        doublesPairList.add(makeDoublesPair (61, 2091, 2276, 4570, 4570, tournamentEvent.getId()));
        doublesPairList.add(makeDoublesPair (62, 1492, 1735, 4270, 4302, tournamentEvent.getId()));
        doublesPairList.add(makeDoublesPair (63, 2219, 1817, 4138, 4138, tournamentEvent.getId()));
        doublesPairList.add(makeDoublesPair (64, 2061, 1612, 3764, 3782, tournamentEvent.getId()));
        doublesPairList.add(makeDoublesPair (91, 1668, 1984, 3627, 3627, tournamentEvent.getId()));
//        doublesPairList.add(makeDoublesPair (92, 1761, 2378, 2387, 1761, tournamentEventEntity.getId()));
        return doublesPairList;
    }

    private DoublesPair makeDoublesPair(long id, int playerAEventEntryFk, int playerBEventEntryFk, int eligibilityRating, int seedRating, long tournamentEventFk) {
        DoublesPair doublesPair = new DoublesPair();
        doublesPair.setId(id);
        doublesPair.setEligibilityRating(eligibilityRating);
        doublesPair.setSeedRating(seedRating);
        doublesPair.setPlayerAEventEntryFk(playerAEventEntryFk);
        doublesPair.setPlayerBEventEntryFk(playerBEventEntryFk);
        doublesPair.setTournamentEventFk(tournamentEventFk);
        return doublesPair;
    }

}
