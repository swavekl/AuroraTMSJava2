package com.auroratms.draw.generation;

import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractDrawsGeneratorTest {
    protected Map<Long, PlayerDrawInfo> makePlayerDrawInfos() {
        Map<Long, PlayerDrawInfo> map = new HashMap<>();
        map.put(482L, makePlayerDrawInfo("00uy334yet1s48amI0h7", "Akova, Ferit", 1571, 0L, "IN"));
        map.put(512L, makePlayerDrawInfo("00uy33500euPQasgr0h7", "Burns, Sam", 1545, 0L, "IN"));
        map.put(519L, makePlayerDrawInfo("00uy3350651h5E5lE0h7", "Chandrika, Veera", 1464, 0L, "IL"));
        map.put(520L, makePlayerDrawInfo("00uy33506vgW087QD0h7", "Chao, Madison", 1547, 0L, "WI"));
        map.put(525L, makePlayerDrawInfo("00uy3350b0emjCLBd0h7", "Coslet, Nathan", 1560, 0L, "MO"));
        map.put(541L, makePlayerDrawInfo("00uy3351eiI5ylB4n0h7", "Emilianowicz, Eryk", 1660, 0L, "IL"));
        map.put(542L, makePlayerDrawInfo("00uy3351ffeTcgUHa0h7", "Evans, J. Bryant", 1729, 0L, "IL"));
        map.put(554L, makePlayerDrawInfo("00uy3351rtKyxbSD10h7", "Freedman, Joseph", 1603, 0L, "IN"));
        map.put(564L, makePlayerDrawInfo("00uy3352rwv2VlurE0h7", "Gaysin, Arsen", 1575, 0L, "IL"));
        map.put(571L, makePlayerDrawInfo("00uy3352y14Ysf2iZ0h7", "Govindhan, Arun", 1353, 0L, "IL"));
        map.put(587L, makePlayerDrawInfo("00uy3353bjcwj8cp00h7", "Jia, Mellynda", 1705, 0L, "WI"));
        map.put(592L, makePlayerDrawInfo("00uy3353fdq5YlZ160h7", "Johnson, Percy", 1516, 0L, "BC"));
        map.put(606L, makePlayerDrawInfo("00uy3354idVyAljqc0h7", "Korneev, Vadim", 1708, 0L, "ON"));
        map.put(636L, makePlayerDrawInfo("00uy3357io9pOqnWu0h7", "Magno, Chris", 0, 0L, "IL"));
        map.put(637L, makePlayerDrawInfo("00uy3357jePgJnYar0h7", "Magno, Jose", 0, 0L, "IL"));
        map.put(648L, makePlayerDrawInfo("00uy3357t1yKomyWv0h7", "Mohammadi, Maziar", 1690, 0L, "CO"));
        map.put(651L, makePlayerDrawInfo("00uy3357vbMGHk6QG0h7", "Molenda, Ed", 1289, 0L, "IN"));
        map.put(708L, makePlayerDrawInfo("00uy335azl1HR3YOK0h7", "Shahal, Pranav", 1734, 0L, "IL"));
        map.put(737L, makePlayerDrawInfo("00uy335c4bPr8pKx10h7", "Stulce, Alex", 1507, 0L, "MO"));
        map.put(741L, makePlayerDrawInfo("00uy335c9oU3h5fbc0h7", "Szacilowski, Tomasz", 1478, 0L, "IL"));
        map.put(753L, makePlayerDrawInfo("00uy335cqn2bnJwZe0h7", "To, Eric", 1731, 0L, "IL"));
        map.put(754L, makePlayerDrawInfo("00uy335crwj3eGGH50h7", "To, Justin", 1524, 0L, "IL"));
        map.put(780L, makePlayerDrawInfo("00uy335de7q7PKvy70h7", "Willard, Keith", 1405, 0L, "MN"));

        return map;
    }

    private PlayerDrawInfo makePlayerDrawInfo(String profileId, String fullName, int seedRating, long clubId, String state) {
        PlayerDrawInfo playerDrawInfo = new PlayerDrawInfo();
        playerDrawInfo.setProfileId(profileId);
        playerDrawInfo.setPlayerName(fullName);
        playerDrawInfo.setRating(seedRating);
        playerDrawInfo.setClubId(clubId);
        playerDrawInfo.setState(state);
        return playerDrawInfo;
    }

    protected List<TournamentEventEntry> makeTournamentEntriesList() {
        long tournamentFk = 153L;
        long eventFk = 46L;
        List<TournamentEventEntry> eventEntries = new ArrayList<>();
        eventEntries.add(makeTournamentEventEntry(1404L, tournamentFk, eventFk, 482L));
        eventEntries.add(makeTournamentEventEntry(1474L, tournamentFk, eventFk, 512L));
        eventEntries.add(makeTournamentEventEntry(1495L, tournamentFk, eventFk, 519L));
        eventEntries.add(makeTournamentEventEntry(1497L, tournamentFk, eventFk, 520L));
        eventEntries.add(makeTournamentEventEntry(1513L, tournamentFk, eventFk, 525L));
        eventEntries.add(makeTournamentEventEntry(1562L, tournamentFk, eventFk, 541L));
        eventEntries.add(makeTournamentEventEntry(1567L, tournamentFk, eventFk, 542L));
        eventEntries.add(makeTournamentEventEntry(1600L, tournamentFk, eventFk, 554L));
        eventEntries.add(makeTournamentEventEntry(1625L, tournamentFk, eventFk, 564L));
        eventEntries.add(makeTournamentEventEntry(1645L, tournamentFk, eventFk, 571L));
        eventEntries.add(makeTournamentEventEntry(1693L, tournamentFk, eventFk, 587L));
        eventEntries.add(makeTournamentEventEntry(1708L, tournamentFk, eventFk, 592L));
        eventEntries.add(makeTournamentEventEntry(1740L, tournamentFk, eventFk, 606L));
        eventEntries.add(makeTournamentEventEntry(1824L, tournamentFk, eventFk, 636L));
        eventEntries.add(makeTournamentEventEntry(1825L, tournamentFk, eventFk, 637L));
        eventEntries.add(makeTournamentEventEntry(1864L, tournamentFk, eventFk, 648L));
        eventEntries.add(makeTournamentEventEntry(1873L, tournamentFk, eventFk, 651L));
        eventEntries.add(makeTournamentEventEntry(2023L, tournamentFk, eventFk, 708L));
        eventEntries.add(makeTournamentEventEntry(2110L, tournamentFk, eventFk, 737L));
        eventEntries.add(makeTournamentEventEntry(2119L, tournamentFk, eventFk, 741L));
        eventEntries.add(makeTournamentEventEntry(2156L, tournamentFk, eventFk, 753L));
        eventEntries.add(makeTournamentEventEntry(2159L, tournamentFk, eventFk, 754L));
        eventEntries.add(makeTournamentEventEntry(2226L, tournamentFk, eventFk, 780L));
        return eventEntries;
    }

    private TournamentEventEntry makeTournamentEventEntry(long id, long tournamentFk, long eventFk, long entryFk) {
        TournamentEventEntry entry = new TournamentEventEntry();
        entry.setId(id);
        entry.setTournamentEntryFk(tournamentFk);
        entry.setTournamentEventFk(eventFk);
        entry.setTournamentEntryFk(entryFk);
        return entry;
    }
}
