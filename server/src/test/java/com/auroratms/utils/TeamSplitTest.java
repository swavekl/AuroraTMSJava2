package com.auroratms.utils;

import com.auroratms.team.Team;
import com.auroratms.team.TeamMember;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TeamSplitTest {

    @Test
    public void testExtractTeamAndMembers() throws Exception {
        Elements teamEntryDetails = new Elements();
        teamEntryDetails.add(new Element("td").text("Team Name"));
        teamEntryDetails.add(new Element("td").text("5000"));
        teamEntryDetails.add(new Element("td").html("&nbsp;&nbsp;821 - Mustafa Khanani <br>1553 - Sheza Khanani *** INVITED ***<br>1852 - Anivritt Vanaparthy *** INVITED ***<br>1749 - Ashaz Farooqui *** INVITED ***<br>1815 - Shoaib Moosa *** INVITED ***"));

        Map<String, String> playerNameToProfileMap = new HashMap<>();
        playerNameToProfileMap.put("Khanani, Mustafa", "profile1");
        playerNameToProfileMap.put("Khanani, Sheza", "profile2");
        playerNameToProfileMap.put("Vanaparthy, Anivritt", "profile3");
        playerNameToProfileMap.put("Farooqui, Ashaz", "profile4");
        playerNameToProfileMap.put("Moosa, Shoaib", "profile5");

        ImportTournamentService importTournamentService = new ImportTournamentService();
        Team team = importTournamentService.extractTeamAndMembers(playerNameToProfileMap, teamEntryDetails, 1L);
        
        System.out.println("Team members count: " + team.getTeamMembers().size());
        for (TeamMember member : team.getTeamMembers()) {
            System.out.println("Member profile ID: " + member.getProfileId() + " Rating: " + member.getPlayerRating());
        }
        
        assertEquals(5, team.getTeamMembers().size(), "Should have 5 team members");
    }
}
