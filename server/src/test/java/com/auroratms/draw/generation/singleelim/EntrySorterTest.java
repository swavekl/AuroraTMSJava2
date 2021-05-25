package com.auroratms.draw.generation.singleelim;

import com.auroratms.draw.generation.PlayerDrawInfo;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EntrySorterTest {

    @Test
    public void test1() {
        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = makeEntryToPDIMap();
        List<TournamentEventEntry> entries = makeEntries();
        EntrySorter sorter = new EntrySorter(entryIdToPlayerDrawInfo);
        int requiredByes = 2;
        List<TournamentEventEntry> sortedEntries = sorter.sortEntries(entries, requiredByes);
        assertEquals("sorting failed", 16, sortedEntries.size());

        Long[] expectedEntriesOrder = {640L, 662L, 656L, 582L, 558L, 644L, 657L, 740L, 508L, 690L, 493L, 532L, 614L, 526L, 722L, 631L};
        int index = 0;
        for (TournamentEventEntry sortedEntry : sortedEntries) {
            Long tournamentEntryId = sortedEntry.getTournamentEntryFk();
//            System.out.print("L, " + tournamentEntryId);
            assertEquals("", expectedEntriesOrder[index], tournamentEntryId);
            index++;
            PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(tournamentEntryId);
            if (playerDrawInfo != null) {
//                System.out.println(
//                        String.format("\"%s\",\t \"%s\",\t \"%s\",\t %d,\t \"%s\", %d",
//                        playerDrawInfo.getClubName(), playerDrawInfo.getCity(),
//                                playerDrawInfo.getState(), playerDrawInfo.getRating(),
//                                playerDrawInfo.getPlayerName(), tournamentEntryId));
            }
        }
    }

    private Map<Long, PlayerDrawInfo> makeEntryToPDIMap() {
        Map<Long, PlayerDrawInfo> map = new HashMap<>();
        map.put(640L, makePlayerDrawInfo("Meredith, Scott", null, null, "MO", 1968));
        map.put(662L, makePlayerDrawInfo("Ngeunjuntr, Brian", "Experior TTC", null, "IL", 1959));
        map.put(582L, makePlayerDrawInfo("Imbo, Sam Oluoch", "MN TTC", null, "MN", 1957));
        map.put(656L, makePlayerDrawInfo("Muhammad, James", "Farmington Hills TTC", null, "MI", 1956));
        map.put(558L, makePlayerDrawInfo("Gacki, Slawomir", null, null, "OH", 1941));
        map.put(657L, makePlayerDrawInfo("Nabity, Kevin", "Des Moines TTC", null, "IA", 1941));
        map.put(740L, makePlayerDrawInfo("Syed, Zayd", "Brian Bae TTC", null, "IL", 1937));
        map.put(644L, makePlayerDrawInfo("Miklowcic, Jerred", null, null, "NC", 1910));
        map.put(631L, makePlayerDrawInfo("Loganathan, Aarthi", "Table Tennis Minnesota", null, "MN", 1907));
        map.put(614L, makePlayerDrawInfo("Kuppurajah, Omprakash", "Fox Valley TTC", null, "IL", 1904));
        map.put(508L, makePlayerDrawInfo("Boghikian, Razmig", "Farmington Hills TTC", null, "MI", 1896));
        map.put(526L, makePlayerDrawInfo("Cottrell, Blake", "Samson Dubina Table Tennis Academy", null, "OH", 1881));
        map.put(690L, makePlayerDrawInfo("Raza, Mushahid", "Farmington Hills TTC", null, "MI", 1861));
        map.put(493L, makePlayerDrawInfo("Augspurger, Jon", "Des Moines TTC", null, "IA", 1851));
        map.put(532L, makePlayerDrawInfo("Dave, Pavana", "Experior TTC", null, "IL", 1844));
        map.put(722L, makePlayerDrawInfo("Slomski, Adam", "Strongsville TTC", null, "OH", 1841));
        return map;
    }

    private PlayerDrawInfo makePlayerDrawInfo(String fullName, String clubName, String city, String state, int rating) {
        PlayerDrawInfo playerDrawInfo = new PlayerDrawInfo();
        playerDrawInfo.setPlayerName(fullName);
        playerDrawInfo.setClubName(clubName);
        playerDrawInfo.setCity(city);
        playerDrawInfo.setState(state);
        playerDrawInfo.setRating(rating);
        return playerDrawInfo;
    }

    private List<TournamentEventEntry> makeEntries() {
        List<TournamentEventEntry> entries = new ArrayList<>();
        entries.add(makeTournamentEventEntry(1831, 54, 640));
        entries.add(makeTournamentEventEntry(1911, 54, 662));
        entries.add(makeTournamentEventEntry(1673, 54, 582));
        entries.add(makeTournamentEventEntry(1884, 54, 656));
        entries.add(makeTournamentEventEntry(1610, 54, 558));
        entries.add(makeTournamentEventEntry(1890, 54, 657));
        entries.add(makeTournamentEventEntry(2114, 54, 740));
        entries.add(makeTournamentEventEntry(1845, 54, 644));
        entries.add(makeTournamentEventEntry(1797, 54, 631));
        entries.add(makeTournamentEventEntry(1757, 54, 614));
        entries.add(makeTournamentEventEntry(1461, 54, 508));
        entries.add(makeTournamentEventEntry(1516, 54, 526));
        entries.add(makeTournamentEventEntry(1978, 54, 690));
        entries.add(makeTournamentEventEntry(1425, 54, 493));
        entries.add(makeTournamentEventEntry(1534, 54, 532));
        entries.add(makeTournamentEventEntry(2059, 54, 722));
        return entries;
    }

    private TournamentEventEntry makeTournamentEventEntry(long eventEntryId, long eventFk, long tournamenEntryFk) {
        TournamentEventEntry eventEntry = new TournamentEventEntry();
        eventEntry.setId(eventEntryId);
        eventEntry.setTournamentEventFk(eventFk);
        eventEntry.setTournamentEntryFk(tournamenEntryFk);
        return eventEntry;

    }

}
