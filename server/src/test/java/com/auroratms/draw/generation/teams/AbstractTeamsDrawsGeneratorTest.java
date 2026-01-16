package com.auroratms.draw.generation.teams;

import com.auroratms.draw.generation.PlayerDrawInfo;
import com.auroratms.event.*;
import com.auroratms.team.Team;
import com.auroratms.team.TeamEntryStatus;
import com.auroratms.team.TeamMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractTeamsDrawsGeneratorTest {

    Map<Long, Team> teamMap = new HashMap<>();
    Map<Long, PlayerDrawInfo> playerProfileIdMap = new HashMap<>();

    protected TournamentEvent makeTournamentEventEntity() {
        TournamentEvent tournamentEvent = new TournamentEvent();
        tournamentEvent.setId(917L);
        tournamentEvent.setTournamentFk(982L);
        tournamentEvent.setName("Teams");
        tournamentEvent.setDoubles(false);

        List<TournamentEventRound> rounds = new ArrayList<>();
        TournamentEventRound preliminaryRound = new TournamentEventRound();
        preliminaryRound.setSingleElimination(false);
        preliminaryRound.setDay(1);
        preliminaryRound.setRoundName("Preliminary");
        preliminaryRound.setStartTime(9.0d);
        preliminaryRound.setOrdinalNum(1);
        rounds.add(preliminaryRound);
        List<TournamentEventRoundDivision> divisionsList = new ArrayList<>();
        TournamentEventRoundDivision division = new TournamentEventRoundDivision();
        divisionsList.add(division);
        division.setDrawMethod(DrawMethod.DIVISION);
        division.setDivisionName("Preliminary");
        division.setPlayersPerGroup(6);
        division.setNumberOfGames(5);
        preliminaryRound.setDivisions(divisionsList);

        TournamentRoundsConfiguration roundsConfiguration = new TournamentRoundsConfiguration();
        tournamentEvent.setRoundsConfiguration(roundsConfiguration);
        roundsConfiguration.setRounds(rounds);

        return tournamentEvent;
    }


    protected void makeAllInfo () {
        this.teamMap = new HashMap<>();
        this.playerProfileIdMap = new HashMap<>();

        makeForOnePlayer (teamMap, playerProfileIdMap,197, "Alpha Go", 2343, 917, 7667, 2343, 21485, 272, "00u2j2arbyymmaZft0h8", true, 72394, "Huang, Andrew", "CA");
        makeForOnePlayer (teamMap, playerProfileIdMap,198, "AM1", 1847, 917, 7681, 1847, 21499, 273, "00u2lxfen2rhqiSuG0h8", true, 1150994, "Mouchinski, Arseni", "NJ");
        makeForOnePlayer (teamMap, playerProfileIdMap,199, "AM2", 2246, 917, 7682, 2246, 21500, 274, "00u2lxfouv1knYV1M0h8", true, 1150995, "Mouchinski, Max", "NJ");
        makeForOnePlayer (teamMap, playerProfileIdMap,200, "Apex TT", 1990, 917, 7658, 1990, 21476, 275, "00u2lxfen1zQPgVy70h8", true, 1131757, "Dreano, Daniel", "NC");
        makeForOnePlayer (teamMap, playerProfileIdMap,201, "Bigsea Table Tennis Academy", 1871, 917, 7661, 1871, 21479, 276, "00u2lxft3i4CtTuCh0h8", true, 400611, "Fatokun, Osuolale Olanrewaju", "CA");
        makeForOnePlayer (teamMap, playerProfileIdMap,202, "BROWARD TTC 2", 6217, 917, 7697, 2123, 21515, 279, "00u2lxf5q5pLCwBm10h8", false, 94586, "Rodriguez, Luis", "FL");
        makeForOnePlayer (teamMap, playerProfileIdMap,202, "BROWARD TTC 2", 6217, 917, 7673, 2073, 21491, 278, "00u2lxf5q0dLlm8oP0h8", false, 221260, "Leon, Samuel", "");
        makeForOnePlayer (teamMap, playerProfileIdMap,202, "BROWARD TTC 2", 6217, 917, 7678, 2021, 21496, 277, "00u2lxfx174qItCYA0h8", true, 222018, "Mejia, Luis", "");
        makeForOnePlayer (teamMap, playerProfileIdMap,203, "CITTA RC", 1122, 917, 7651, 1122, 21469, 280, "00u2lxfdudlqYexOA0h8", true, 1185758, "Cai, Ryan", "NC");
        makeForOnePlayer (teamMap, playerProfileIdMap,204, "CITTA-North", 5488, 917, 7668, 1821, 21486, 281, "00u2j29f5myK7DpgH0h8", true, 1179779, "Khanani, Mustafa", "");
        makeForOnePlayer (teamMap, playerProfileIdMap,205, "Coastal Cactus", 1786, 917, 7650, 1786, 21468, 282, "00u2lxfxx6xtATfDs0h8", true, 1174470, "Burch, Andrew", "");
        makeForOnePlayer (teamMap, playerProfileIdMap,206, "DC", 3686, 917, 7680, 1897, 21498, 283, "00u2izd5mztuZQpYv0h8", true, 280046, "Morse-achtenberg, Yonatan", "DC");
        makeForOnePlayer (teamMap, playerProfileIdMap,206, "DC", 3686, 917, 7677, 1789, 21495, 284, "00u2lxevfwk5wC07w0h8", false, 284702, "Mandle, Kennedy", "DC");
        makeForOnePlayer (teamMap, playerProfileIdMap,207, "Exceptionally Overrated", 2150, 917, 7654, 2150, 21472, 285, "00u24us1xomzhvZRJ0h8", true, 1173263, "Conroy, Mitch", "AL");
        makeForOnePlayer (teamMap, playerProfileIdMap,208, "Loop Killaz", 2105, 917, 7689, 2105, 21507, 286, "00u2lxg9a6kQWnWQi0h8", true, 216131, "Noorani, Kash", "CA");
        makeForOnePlayer (teamMap, playerProfileIdMap,209, "Looping Leprechauns", 1890, 917, 7659, 1890, 21477, 287, "00u2lxfzwwhtUUFXi0h8", true, 1173928, "Fan, Huafeng", "NJ");
        makeForOnePlayer (teamMap, playerProfileIdMap,210, "LYTTC Paddle Panthers", 1491, 917, 7686, 1491, 21504, 288, "00u2lxfwc4mLX6v4S0h8", true, 1184247, "Neyman, Ian", "");
        makeForOnePlayer (teamMap, playerProfileIdMap,211, "MDTTC Beginner Class", 6298, 917, 7664, 2223, 21482, 291, "00u2lxeyod7m2uFJR0h8", false, 273762, "Ghosh, Richik", "VA");
        makeForOnePlayer (teamMap, playerProfileIdMap,211, "MDTTC Beginner Class", 6298, 917, 7699, 2060, 21517, 290, "00u2lxfupfpVTFFjt0h8", false, 1163913, "Salatov, Aj", "MD");
        makeForOnePlayer (teamMap, playerProfileIdMap,211, "MDTTC Beginner Class", 6298, 917, 7700, 2015, 21518, 289, "00u2lxfwq1hw3nQ9j0h8", true, 79268, "Salatov, Konstantyn", "MD");
        makeForOnePlayer (teamMap, playerProfileIdMap,212, "MDTTC boys", 1805, 917, 7691, 1805, 21509, 292, "00u282xgh0xDm3hem0h8", true, 1172200, "Paudel, Akshan", "MD");
        makeForOnePlayer (teamMap, playerProfileIdMap,213, "Mix It Up", 3287, 917, 7663, 1687, 21481, 294, "00u2lxet6w6MWitAo0h8", false, 279434, "Ghosh, Projesh", "VA");
        makeForOnePlayer (teamMap, playerProfileIdMap,213, "Mix It Up", 3287, 917, 7708, 1600, 21526, 293, "00u2lxfxys4Rkyy9G0h8", true, 216784, "Wadkar, Sameer", "MD");
        makeForOnePlayer (teamMap, playerProfileIdMap,214, "NC Devils", 1533, 917, 7676, 1533, 21494, 295, "00u2lxfouuvTu2A5Q0h8", true, 88033, "Luo, Jeffrey", "MD");
        makeForOnePlayer (teamMap, playerProfileIdMap,215, "NC Hurricane", 1755, 917, 7688, 1755, 21506, 296, "00u2lxfh2z0Q5SwlB0h8", true, 78310, "Nie, Sen", "NC");
        makeForOnePlayer (teamMap, playerProfileIdMap,216, "Ohio Slicers", 2028, 917, 7669, 2028, 21487, 297, "00u2lxfxx79S8gpbE0h8", true, 31279, "Khandelwal, Siddharth", "OH");
        makeForOnePlayer (teamMap, playerProfileIdMap,217, "Pho King Chopsticks", 5649, 917, 7657, 1965, 21475, 299, "00u2lxfa9r4Br5NVr0h8", false, 29186, "Dewan, Rohit", "VA");
        makeForOnePlayer (teamMap, playerProfileIdMap,217, "Pho King Chopsticks", 5649, 917, 7666, 1857, 21484, 302, "00u2lxfouukS1Z9fq0h8", false, 92055, "Ho, Quan", "VA");
        makeForOnePlayer (teamMap, playerProfileIdMap,217, "Pho King Chopsticks", 5649, 917, 7690, 1827, 21508, 298, "00u2lxfx19vfjcg3i0h8", true, 96005, "Park, Brian", "NJ");
        makeForOnePlayer (teamMap, playerProfileIdMap,217, "Pho King Chopsticks", 5649, 917, 7660, 1636, 21478, 300, "00u2j2b49gsZA21IB0h8", false, 400566, "Fan, Yun", "OR");
        makeForOnePlayer (teamMap, playerProfileIdMap,217, "Pho King Chopsticks", 5649, 917, 7701, 1629, 21519, 301, "00u2j2b6vnm5IekCI0h8", false, 81397, "Shih, Paul", "OR");
        makeForOnePlayer (teamMap, playerProfileIdMap,218, "Ping Pong Boys", 5156, 917, 7695, 1777, 21513, 303, "00u2lxfwokeVWafF80h8", true, 76010, "Rane, Ashutosh", "AZ");
        makeForOnePlayer (teamMap, playerProfileIdMap,218, "Ping Pong Boys", 5156, 917, 7694, 1698, 21512, 305, "00u2lxfupfbsLjMB60h8", false, 280549, "Rane, Aarnav", "AZ");
        makeForOnePlayer (teamMap, playerProfileIdMap,218, "Ping Pong Boys", 5156, 917, 7693, 1681, 21511, 304, "00u2lxg978cNkAs9j0h8", false, 1183736, "Rane, Aarav", "");
        makeForOnePlayer (teamMap, playerProfileIdMap,219, "Racket Ninjas", 933, 917, 7698, 933, 21516, 306, "00u2lxf5q5uEsxkzG0h8", true, 1188960, "Sagar, Agneya", "");
        makeForOnePlayer (teamMap, playerProfileIdMap,220, "Rising Smashers", 1808, 917, 7648, 1808, 21466, 307, "00u2lxf5pzsL94hNP0h8", true, 272442, "Aswin, Vedanth", "");
        makeForOnePlayer (teamMap, playerProfileIdMap,221, "Scared hitless", 1753, 917, 7687, 1753, 21505, 308, "00u2lxfduhkFTWxlc0h8", true, 15334, "Nicolas, Frederick", "VA");
        makeForOnePlayer (teamMap, playerProfileIdMap,222, "Side Spinners", 1253, 917, 7684, 1253, 21502, 309, "00u2lxf5g2xm24XoZ0h8", true, 29826, "Neumann, Steven", "PA");
        makeForOnePlayer (teamMap, playerProfileIdMap,223, "STADIUM 2", 5738, 917, 7703, 2023, 21521, 313, "00u2lxfwc5mHhv8PE0h8", false, 72870, "Spesick, Tyler", "CA");
        makeForOnePlayer (teamMap, playerProfileIdMap,223, "STADIUM 2", 5738, 917, 7702, 1868, 21520, 312, "00u22j122ptnwDl4l0h8", false, 15743, "Smith, Greg", "NY");
        makeForOnePlayer (teamMap, playerProfileIdMap,223, "STADIUM 2", 5738, 917, 7665, 1847, 21483, 311, "00u2lxfdtv0PtjMat0h8", false, 214914, "Guido, Zack", "NY");
        makeForOnePlayer (teamMap, playerProfileIdMap,223, "STADIUM 2", 5738, 917, 7652, 1660, 21470, 310, "00u2lxft3hvsouAcA0h8", true, 218348, "Cha, Christine", "NY");
        makeForOnePlayer (teamMap, playerProfileIdMap,224, "Table Titans", 5622, 917, 7685, 1643, 21503, 314, "00u2lxfouv63L0Fjm0h8", true, 272025, "Newton-tanzer, Gavin", "NJ");
        makeForOnePlayer (teamMap, playerProfileIdMap,225, "Team KS", 3842, 917, 7671, 2003, 21489, 315, "00u2lxf8vkgJRaAia0h8", true, 276426, "Kodaty, Siddharth", "CA");
        makeForOnePlayer (teamMap, playerProfileIdMap,226, "Team Mavericks", 6219, 917, 7656, 2151, 21474, 316, "00u2lxg974aoyEyK10h8", true, 1179199, "Desaraju, Vihaan", "TX");
        makeForOnePlayer (teamMap, playerProfileIdMap,226, "Team Mavericks", 6219, 917, 7713, 2052, 21531, 318, "00u2lxfr48za0Rbd60h8", false, 1177302, "Zhao, Yizhou", "TX");
        makeForOnePlayer (teamMap, playerProfileIdMap,226, "Team Mavericks", 6219, 917, 7707, 2016, 21525, 317, "00u2lxf3isfdQCIEs0h8", false, 271889, "Tsai, Ivan", "TX");
        makeForOnePlayer (teamMap, playerProfileIdMap,227, "The Underdogs", 2154, 917, 7705, 2154, 21523, 319, "00u2j2b5gytSWnp2A0h8", true, 271975, "Talukdar, Jishnu", "CA");
        makeForOnePlayer (teamMap, playerProfileIdMap,228, "Trim Team", 5051, 917, 7709, 1877, 21527, 320, "00u2dl95rcmvYrKX20h8", true, 221798, "Yanaga, Edson", "NC");
        makeForOnePlayer (teamMap, playerProfileIdMap,228, "Trim Team", 5051, 917, 7711, 1785, 21529, 322, "00u2irritz7ChDlRx0h8", false, 221801, "Yanaga, Guilherme", "");
        makeForOnePlayer (teamMap, playerProfileIdMap,228, "Trim Team", 5051, 917, 7710, 1389, 21528, 321, "00u2lxfwedtXC4gLj0h8", false, 221799, "Yanaga, Felipe", "");
        makeForOnePlayer (teamMap, playerProfileIdMap,229, "Turkish Coffee", 2101, 917, 7672, 2101, 21490, 323, "00u2lxfmyzblMDHTc0h8", true, 92490, "Lawton, Sam", "NC");
        makeForOnePlayer (teamMap, playerProfileIdMap,230, "Virginia Beach Table Tennis", 966, 917, 7649, 966, 21467, 324, "00u2lxfwc1lmsWToy0h8", true, 1186636, "Baring, Halley", "");
        makeForOnePlayer (teamMap, playerProfileIdMap,231, "WAK TTS", 0, 917, 7670, 0, 21488, 325, "00u2lxft3i9i040HM0h8", true, 1185768, "Khokhar, Muhammad Anas", "");
        makeForOnePlayer (teamMap, playerProfileIdMap,232, "ZJ\'s Club", 1505, 917, 7683, 1505, 21501, 326, "00u2lxfwpvyTpdYAt0h8", true, 12141, "Nelson, Jay", "WV");
    }
    
    private void makeForOnePlayer (Map<Long, Team> teamMap,
                                   Map<Long, PlayerDrawInfo> playerDrawInfoMap,
                                   long teamId, String teamName, int teamRating, long tournamentEventId, long tournamentEntryId,
                                   int playerRating, long tournamentEventEntryId, 
                                   long teamMemberId, String playerProfileId, 
                                   boolean captain, int membership_id, String playerName, String state) {
        Team team = teamMap.get(teamId);
        if (team == null) {
            team = new Team();
            team.setId(teamId);
            team.setName(teamName);
            team.setTeamRating(teamRating);
            team.setTournamentEventFk(tournamentEventId);
            teamMap.put(teamId, team);
        }

        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(team);
        teamMember.setId(teamMemberId);
        teamMember.setProfileId(playerProfileId);
        teamMember.setCaptain(captain);
        teamMember.setPlayerRating(playerRating);
        teamMember.setPlayerName(playerName);
        teamMember.setStatus(TeamEntryStatus.CONFIRMED);
        team.addTeamMember(teamMember);
        if (captain) {
            team.setPayerTournamentEntryFk(tournamentEntryId);
        }

        PlayerDrawInfo playerDrawInfo = new PlayerDrawInfo();
        playerDrawInfo.setPlayerName(playerName);
        playerDrawInfo.setProfileId(playerProfileId);
        playerDrawInfo.setRating(playerRating);
        playerDrawInfo.setState(state);
        playerDrawInfoMap.put(tournamentEntryId, playerDrawInfo);
    }

    public Map<Long, Team> getTeamMap() {
        return teamMap;
    }

    public Map<Long, PlayerDrawInfo> getPlayerProfileIdMap() {
        return playerProfileIdMap;
    }
}


//    protected List<Team> makeTeamsForEvent() {
//        List<Team> teams = new ArrayList<>(36);
//        teams.add(makeTeam(197, 917, 2343, "Alpha Go"));
//        teams.add(makeTeam(198, 917, 1847, "AM1"));
//        teams.add(makeTeam(199, 917, 2246, "AM2"));
//        teams.add(makeTeam(200, 917, 1990, "Apex TT"));
//        teams.add(makeTeam(201, 917, 1871, "Bigsea Table Tennis Academy"));
//        teams.add(makeTeam(202, 917, 6217, "BROWARD TTC 2"));
//        teams.add(makeTeam(203, 917, 1122, "CITTA RC"));
//        teams.add(makeTeam(204, 917, 5488, "CITTA-North"));
//        teams.add(makeTeam(205, 917, 1786, "Coastal Cactus"));
//        teams.add(makeTeam(206, 917, 3686, "DC"));
//        teams.add(makeTeam(207, 917, 2150, "Exceptionally Overrated"));
//        teams.add(makeTeam(208, 917, 2105, "Loop Killaz"));
//        teams.add(makeTeam(209, 917, 1890, "Looping Leprechauns"));
//        teams.add(makeTeam(210, 917, 1491, "LYTTC Paddle Panthers"));
//        teams.add(makeTeam(211, 917, 6298, "MDTTC Beginner Class"));
//        teams.add(makeTeam(212, 917, 1805, "MDTTC boys"));
//        teams.add(makeTeam(213, 917, 3287, "Mix It Up"));
//        teams.add(makeTeam(214, 917, 1533, "NC Devils"));
//        teams.add(makeTeam(215, 917, 1755, "NC Hurricane"));
//        teams.add(makeTeam(216, 917, 2028, "Ohio Slicers"));
//        teams.add(makeTeam(217, 917, 5649, "Pho King Chopsticks"));
//        teams.add(makeTeam(218, 917, 5156, "Ping Pong Boys"));
//        teams.add(makeTeam(219, 917, 933, "Racket Ninjas"));
//        teams.add(makeTeam(220, 917, 1808, "Rising Smashers"));
//        teams.add(makeTeam(221, 917, 1753, "Scared hitless"));
//        teams.add(makeTeam(222, 917, 1253, "Side Spinners"));
//        teams.add(makeTeam(223, 917, 5738, "STADIUM 2"));
//        teams.add(makeTeam(224, 917, 5622, "Table Titans"));
//        teams.add(makeTeam(225, 917, 3842, "Team KS"));
//        teams.add(makeTeam(226, 917, 6219, "Team Mavericks"));
//        teams.add(makeTeam(227, 917, 2154, "The Underdogs"));
//        teams.add(makeTeam(228, 917, 5051, "Trim Team"));
//        teams.add(makeTeam(229, 917, 2101, "Turkish Coffee"));
//        teams.add(makeTeam(230, 917, 966, "Virginia Beach Table Tennis"));
//        teams.add(makeTeam(231, 917, 0, "WAK TTS"));
//        teams.add(makeTeam(232, 917, 1505, "ZJ\"s Club"));
//        return teams;
//    }
//
//    private Team makeTeam(long id, int teamEventFk, int teamRating, String teamName) {
//        Team team = new Team();
//        team.setId(id);
//        team.setName(teamName);
//        team.setTeamRating(teamRating);
//        team.setTournamentEventFk(teamEventFk);
//        return team;
//    }
//
//
//    protected Map<Long, PlayerDrawInfo> makeEntryIdToPlayerDrawInfo(List<TournamentEventEntry> eventEntries) {
//        Map<Long, PlayerDrawInfo> map = new HashMap<>();
//
//        addPlayerDrawInfo(map, 7709, "00u2dl95rcmvYrKX20h8", 1877, 221798, "Yanaga, Edson", "NC");
//        addPlayerDrawInfo(map, 7708, "00u2lxfxys4Rkyy9G0h8", 1600, 216784, "Wadkar, Sameer", "MD");
//        addPlayerDrawInfo(map, 7707, "00u2lxf3isfdQCIEs0h8", 2016, 271889, "Tsai, Ivan", "TX");
//        addPlayerDrawInfo(map, 7706, "00u2lxfcgh7gJRGHB0h8", 1533, 1184966, "Tran, Dimitri", "");
//        addPlayerDrawInfo(map, 7705, "00u2j2b5gytSWnp2A0h8", 2154, 271975, "Talukdar, Jishnu", "CA");
//        addPlayerDrawInfo(map, 7704, "00u1vy5go1zYwBmAV0h8", 1206, 218014, "Syed, Zayd", "IL");
//        addPlayerDrawInfo(map, 7703, "00u2lxfwc5mHhv8PE0h8", 2023, 72870, "Spesick, Tyler", "CA");
//        addPlayerDrawInfo(map, 7702, "00u22j122ptnwDl4l0h8", 1868, 15743, "Smith, Greg", "NY");
//        addPlayerDrawInfo(map, 7701, "00u2j2b6vnm5IekCI0h8", 1629, 81397, "Shih, Paul", "OR");
//        addPlayerDrawInfo(map, 7700, "00u2lxfwq1hw3nQ9j0h8", 2015, 79268, "Salatov, Konstantyn", "MD");
//        addPlayerDrawInfo(map, 7699, "00u2lxfupfpVTFFjt0h8", 2060, 1163913, "Salatov, Aj", "MD");
//        addPlayerDrawInfo(map, 7698, "00u2lxf5q5uEsxkzG0h8", 933, 1188960, "Sagar, Agneya", "");
//        addPlayerDrawInfo(map, 7697, "00u2lxf5q5pLCwBm10h8", 2123, 94586, "Rodriguez, Luis", "FL");
//        addPlayerDrawInfo(map, 7696, "00u2lxfh2zluLnAKQ0h8", 622, 285607, "Ravi, Neel", "CA");
//        addPlayerDrawInfo(map, 7695, "00u2lxfwokeVWafF80h8", 1777, 76010, "Rane, Ashutosh", "AZ");
//        addPlayerDrawInfo(map, 7694, "00u2lxfupfbsLjMB60h8", 1698, 280549, "Rane, Aarnav", "AZ");
//        addPlayerDrawInfo(map, 7693, "00u2lxg978cNkAs9j0h8", 1681, 1183736, "Rane, Aarav", "");
//        addPlayerDrawInfo(map, 7692, "00u2j13jpjrroXBjK0h8", 1773, 279089, "Pinnoju, Saketh", "TX");
//        addPlayerDrawInfo(map, 7691, "00u282xgh0xDm3hem0h8", 1805, 1172200, "Paudel, Akshan", "MD");
//        addPlayerDrawInfo(map, 7690, "00u2lxfx19vfjcg3i0h8", 1827, 96005, "Park, Brian", "NJ");
//        addPlayerDrawInfo(map, 7689, "00u2lxg9a6kQWnWQi0h8", 2105, 216131, "Noorani, Kash", "CA");
//        addPlayerDrawInfo(map, 7688, "00u2lxfh2z0Q5SwlB0h8", 1755, 78310, "Nie, Sen", "NC");
//        addPlayerDrawInfo(map, 7687, "00u2lxfduhkFTWxlc0h8", 1753, 15334, "Nicolas, Frederick", "VA");
//        addPlayerDrawInfo(map, 7686, "00u2lxfwc4mLX6v4S0h8", 1491, 1184247, "Neyman, Ian", "");
//        addPlayerDrawInfo(map, 7685, "00u2lxfouv63L0Fjm0h8", 1643, 272025, "Newton-tanzer, Gavin", "NJ");
//        addPlayerDrawInfo(map, 7684, "00u2lxf5g2xm24XoZ0h8", 1253, 29826, "Neumann, Steven", "PA");
//        addPlayerDrawInfo(map, 7683, "00u2lxfwpvyTpdYAt0h8", 1505, 12141, "Nelson, Jay", "WV");
//        addPlayerDrawInfo(map, 7682, "00u2lxfouv1knYV1M0h8", 2246, 1150995, "Mouchinski, Max", "NJ");
//        addPlayerDrawInfo(map, 7681, "00u2lxfen2rhqiSuG0h8", 1847, 1150994, "Mouchinski, Arseni", "NJ");
//        addPlayerDrawInfo(map, 7680, "00u2izd5mztuZQpYv0h8", 1897, 280046, "Morse-achtenberg, Yonatan", "DC");
//        addPlayerDrawInfo(map, 7679, "00u2bd5tsp5SeeI6W0h8", 1798, 87993, "Monwar, Maruf", "MI");
//        addPlayerDrawInfo(map, 7678, "00u2lxfx174qItCYA0h8", 2021, 222018, "Mejia, Luis", "");
//        addPlayerDrawInfo(map, 7677, "00u2lxevfwk5wC07w0h8", 1789, 284702, "Mandle, Kennedy", "DC");
//        addPlayerDrawInfo(map, 7676, "00u2lxfouuvTu2A5Q0h8", 1533, 88033, "Luo, Jeffrey", "MD");
//        addPlayerDrawInfo(map, 7675, "00u2lxg974nPx4jg80h8", 1676, 400612, "Lopez, Jahaziel", "PR");
//        addPlayerDrawInfo(map, 7674, "00u2lxet6wfFBwUUm0h8", 1078, 1177311, "Li, Leni (mu)", "");
//        addPlayerDrawInfo(map, 7673, "00u2lxf5q0dLlm8oP0h8", 2073, 221260, "Leon, Samuel", "");
//        addPlayerDrawInfo(map, 7672, "00u2lxfmyzblMDHTc0h8", 2101, 92490, "Lawton, Sam", "NC");
//        addPlayerDrawInfo(map, 7671, "00u2lxf8vkgJRaAia0h8", 2003, 276426, "Kodaty, Siddharth", "CA");
//        addPlayerDrawInfo(map, 7670, "00u2lxft3i9i040HM0h8", 0, 1185768, "Khokhar, Muhammad Anas", "");
//        addPlayerDrawInfo(map, 7669, "00u2lxfxx79S8gpbE0h8", 2028, 31279, "Khandelwal, Siddharth", "OH");
//        addPlayerDrawInfo(map, 7668, "00u2j29f5myK7DpgH0h8", 1821, 1179779, "Khanani, Mustafa", "");
//        addPlayerDrawInfo(map, 7667, "00u2j2arbyymmaZft0h8", 2343, 72394, "Huang, Andrew", "CA");
//        addPlayerDrawInfo(map, 7666, "00u2lxfouukS1Z9fq0h8", 1857, 92055, "Ho, Quan", "VA");
//        addPlayerDrawInfo(map, 7665, "00u2lxfdtv0PtjMat0h8", 1847, 214914, "Guido, Zack", "NY");
//        addPlayerDrawInfo(map, 7664, "00u2lxeyod7m2uFJR0h8", 2223, 273762, "Ghosh, Richik", "VA");
//        addPlayerDrawInfo(map, 7663, "00u2lxet6w6MWitAo0h8", 1687, 279434, "Ghosh, Projesh", "VA");
//        addPlayerDrawInfo(map, 7662, "00u2lxfktsekYjYps0h8", 1745, 274913, "Gade, Saahaj", "TX");
//        addPlayerDrawInfo(map, 7661, "00u2lxft3i4CtTuCh0h8", 1871, 400611, "Fatokun, Osuolale Olanrewaju", "CA");
//        addPlayerDrawInfo(map, 7660, "00u2j2b49gsZA21IB0h8", 1636, 400566, "Fan, Yun", "OR");
//        addPlayerDrawInfo(map, 7659, "00u2lxfzwwhtUUFXi0h8", 1890, 1173928, "Fan, Huafeng", "NJ");
//        addPlayerDrawInfo(map, 7658, "00u2lxfen1zQPgVy70h8", 1990, 1131757, "Dreano, Daniel", "NC");
//        addPlayerDrawInfo(map, 7657, "00u2lxfa9r4Br5NVr0h8", 1965, 29186, "Dewan, Rohit", "VA");
//        addPlayerDrawInfo(map, 7656, "00u2lxg974aoyEyK10h8", 2151, 1179199, "Desaraju, Vihaan", "TX");
//        addPlayerDrawInfo(map, 7655, "00u2lxg974560aCie0h8", 0, 400610, "Cummings, Kevin", "CA");
//        addPlayerDrawInfo(map, 7654, "00u24us1xomzhvZRJ0h8", 2150, 1173263, "Conroy, Mitch", "AL");
//        addPlayerDrawInfo(map, 7653, "00u2lxfdtutGzOoaD0h8", 1200, 400609, "Chubachuk, Valeri", "NC");
//        addPlayerDrawInfo(map, 7652, "00u2lxft3hvsouAcA0h8", 1660, 218348, "Cha, Christine", "NY");
//        addPlayerDrawInfo(map, 7651, "00u2lxfdudlqYexOA0h8", 1122, 1185758, "Cai, Ryan", "NC");
//        addPlayerDrawInfo(map, 7650, "00u2lxfxx6xtATfDs0h8", 1786, 1174470, "Burch, Andrew", "");
//        addPlayerDrawInfo(map, 7648, "00u2lxf5pzsL94hNP0h8", 1808, 272442, "Aswin, Vedanth", "");
//        addPlayerDrawInfo(map, 7649, "00u2lxfwc1lmsWToy0h8", 966, 1186636, "Baring, Halley", "");
//        addPlayerDrawInfo(map, 7712, "00u1vhx2f1nXykJN70h8", 1740, 223659, "Yau, Brandon", "IL");
//        addPlayerDrawInfo(map, 7710, "00u2lxfwedtXC4gLj0h8", 1389, 221799, "Yanaga, Felipe", "");
//        addPlayerDrawInfo(map, 7711, "00u2irritz7ChDlRx0h8", 1785, 221801, "Yanaga, Guilherme", "");
//        addPlayerDrawInfo(map, 7714, "00u2lxfdu0lWLrMdO0h8", 2108, 277185, "Zhou, Arthur", "CA");
//        addPlayerDrawInfo(map, 7713, "00u2lxfr48za0Rbd60h8", 2052, 1177302, "Zhao, Yizhou", "TX");
//
//        return map;
//
//    }
//
//    private void addPlayerDrawInfo (Map<Long, PlayerDrawInfo> map, long tournamententryid, String profile_id, int seed_rating, long membership_id, String playerName, String state) {
//        PlayerDrawInfo playerDrawInfo = new PlayerDrawInfo();
//        playerDrawInfo.setPlayerName(playerName);
//        playerDrawInfo.setProfileId(profile_id);
//        playerDrawInfo.setRating(seed_rating);
//        playerDrawInfo.setState(state);
//        map.put(tournamententryid, playerDrawInfo);
//    }
//
//    protected Map generateTeamsProfile (Map<Long, PlayerDrawInfo> tournamentEntryIdToPlayerDrawInfoMap) {
//        Map<Long, Team> map = new HashMap<>();
//        generateMap (map, 197, "Alpha Go", 2343, 917, "00u2j2arbyymmaZft0h8", true);
//        generateMap (map, 198, "AM1", 1847, 917, "00u2lxfen2rhqiSuG0h8", true);
//        generateMap (map, 199, "AM2", 2246, 917, "00u2lxfouv1knYV1M0h8", true);
//        generateMap (map, 200, "Apex TT", 1990, 917, "00u2lxfen1zQPgVy70h8", true);
//        generateMap (map, 201, "Bigsea Table Tennis Academy", 1871, 917, "00u2lxft3i4CtTuCh0h8", true);
//        generateMap (map, 202, "BROWARD TTC 2", 6217, 917, "00u2lxf5q0dLlm8oP0h8", false);
//        generateMap (map, 202, "BROWARD TTC 2", 6217, 917, "00u2lxf5q5pLCwBm10h8", false);
//        generateMap (map, 202, "BROWARD TTC 2", 6217, 917, "00u2lxfx174qItCYA0h8", true);
//        generateMap (map, 203, "CITTA RC", 1122, 917, "00u2lxfdudlqYexOA0h8", true);
//        generateMap (map, 204, "CITTA-North", 5488, 917, "00u2j29f5myK7DpgH0h8", true);
//        generateMap (map, 205, "Coastal Cactus", 1786, 917, "00u2lxfxx6xtATfDs0h8", true);
//        generateMap (map, 206, "DC", 3686, 917, "00u2izd5mztuZQpYv0h8", true);
//        generateMap (map, 206, "DC", 3686, 917, "00u2lxevfwk5wC07w0h8", false);
//        generateMap (map, 207, "Exceptionally Overrated", 2150, 917, "00u24us1xomzhvZRJ0h8", true);
//        generateMap (map, 208, "Loop Killaz", 2105, 917, "00u2lxg9a6kQWnWQi0h8", true);
//        generateMap (map, 209, "Looping Leprechauns", 1890, 917, "00u2lxfzwwhtUUFXi0h8", true);
//        generateMap (map, 210, "LYTTC Paddle Panthers", 1491, 917, "00u2lxfwc4mLX6v4S0h8", true);
//        generateMap (map, 211, "MDTTC Beginner Class", 6298, 917, "00u2lxeyod7m2uFJR0h8", false);
//        generateMap (map, 211, "MDTTC Beginner Class", 6298, 917, "00u2lxfupfpVTFFjt0h8", false);
//        generateMap (map, 211, "MDTTC Beginner Class", 6298, 917, "00u2lxfwq1hw3nQ9j0h8", true);
//        generateMap (map, 212, "MDTTC boys", 1805, 917, "00u282xgh0xDm3hem0h8", true);
//        generateMap (map, 213, "Mix It Up", 3287, 917, "00u2lxet6w6MWitAo0h8", false);
//        generateMap (map, 213, "Mix It Up", 3287, 917, "00u2lxfxys4Rkyy9G0h8", true);
//        generateMap (map, 214, "NC Devils", 1533, 917, "00u2lxfouuvTu2A5Q0h8", true);
//        generateMap (map, 215, "NC Hurricane", 1755, 917, "00u2lxfh2z0Q5SwlB0h8", true);
//        generateMap (map, 216, "Ohio Slicers", 2028, 917, "00u2lxfxx79S8gpbE0h8", true);
//        generateMap (map, 217, "Pho King Chopsticks", 5649, 917, "00u2j2b49gsZA21IB0h8", false);
//        generateMap (map, 217, "Pho King Chopsticks", 5649, 917, "00u2j2b6vnm5IekCI0h8", false);
//        generateMap (map, 217, "Pho King Chopsticks", 5649, 917, "00u2lxfa9r4Br5NVr0h8", false);
//        generateMap (map, 217, "Pho King Chopsticks", 5649, 917, "00u2lxfouukS1Z9fq0h8", false);
//        generateMap (map, 217, "Pho King Chopsticks", 5649, 917, "00u2lxfx19vfjcg3i0h8", true);
//        generateMap (map, 218, "Ping Pong Boys", 5156, 917, "00u2lxfupfbsLjMB60h8", false);
//        generateMap (map, 218, "Ping Pong Boys", 5156, 917, "00u2lxfwokeVWafF80h8", true);
//        generateMap (map, 218, "Ping Pong Boys", 5156, 917, "00u2lxg978cNkAs9j0h8", false);
//        generateMap (map, 219, "Racket Ninjas", 933, 917, "00u2lxf5q5uEsxkzG0h8", true);
//        generateMap (map, 220, "Rising Smashers", 1808, 917, "00u2lxf5pzsL94hNP0h8", true);
//        generateMap (map, 221, "Scared hitless", 1753, 917, "00u2lxfduhkFTWxlc0h8", true);
//        generateMap (map, 222, "Side Spinners", 1253, 917, "00u2lxf5g2xm24XoZ0h8", true);
//        generateMap (map, 223, "STADIUM 2", 5738, 917, "00u22j122ptnwDl4l0h8", false);
//        generateMap (map, 223, "STADIUM 2", 5738, 917, "00u2lxfdtv0PtjMat0h8", false);
//        generateMap (map, 223, "STADIUM 2", 5738, 917, "00u2lxft3hvsouAcA0h8", true);
//        generateMap (map, 223, "STADIUM 2", 5738, 917, "00u2lxfwc5mHhv8PE0h8", false);
//        generateMap (map, 224, "Table Titans", 5622, 917, "00u2lxfouv63L0Fjm0h8", true);
//        generateMap (map, 225, "Team KS", 3842, 917, "00u2lxf8vkgJRaAia0h8", true);
//        generateMap (map, 226, "Team Mavericks", 6219, 917, "00u2lxf3isfdQCIEs0h8", false);
//        generateMap (map, 226, "Team Mavericks", 6219, 917, "00u2lxfr48za0Rbd60h8", false);
//        generateMap (map, 226, "Team Mavericks", 6219, 917, "00u2lxg974aoyEyK10h8", true);
//        generateMap (map, 227, "The Underdogs", 2154, 917, "00u2j2b5gytSWnp2A0h8", true);
//        generateMap (map, 228, "Trim Team", 5051, 917, "00u2dl95rcmvYrKX20h8", true);
//        generateMap (map, 228, "Trim Team", 5051, 917, "00u2irritz7ChDlRx0h8", false);
//        generateMap (map, 228, "Trim Team", 5051, 917, "00u2lxfwedtXC4gLj0h8", false);
//        generateMap (map, 229, "Turkish Coffee", 2101, 917, "00u2lxfmyzblMDHTc0h8", true);
//        generateMap (map, 230, "Virginia Beach Table Tennis", 966, 917, "00u2lxfwc1lmsWToy0h8", true);
//        generateMap (map, 231, "WAK TTS", 0, 917, "00u2lxft3i9i040HM0h8", true);
//        generateMap (map, 232, "ZJ\"s Club", 1505, 917, "00u2lxfwpvyTpdYAt0h8", true);
//
//        return map;
//    }
//
//
//    protected void generateMap (Map<Long, Team> map, long teamId, String teamName, int team_rating, long tournament_event_fk, String profile_id, boolean captain) {
//        Team team = map.get(teamId);
//        if (team == null) {
//            team = new Team();
//            team.setId(teamId);
//            team.setName(teamName);
//            team.setTeamRating(team_rating);
//            team.setTournamentEventFk(tournament_event_fk);
//            map.put(teamId, team);
//        }
//
//        TeamMember teamMember = new TeamMember();
//        teamMember.setProfileId(profile_id);
//        teamMember.setCaptain(captain);
//        team.addTeamMember(teamMember);
//        if (captain) {
//            team.setPayerTournamentEntryFk(tournament_event_fk);
//        }
//    }
