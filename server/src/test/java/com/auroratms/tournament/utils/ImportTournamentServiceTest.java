package com.auroratms.tournament.utils;

import com.auroratms.AbstractServiceTest;
import com.auroratms.event.GenderRestriction;
import com.auroratms.tournament.Tournament;
import com.auroratms.users.UserRoles;
import com.auroratms.utils.ImportProgressInfo;
import com.auroratms.utils.ImportTournamentRequest;
import com.auroratms.utils.ImportTournamentService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
public class ImportTournamentServiceTest extends AbstractServiceTest  {

    @Autowired
    private ImportTournamentService importTournamentService;

    @Test
    @Disabled
    @WithMockUser(username = "swaveklorenc@gmail.com", authorities = {UserRoles.TournamentDirectors})
    public void testListTournaments() {
        try {
            String url = "C:\\Users\\Swavek\\AppData\\Roaming\\JetBrains\\IntelliJIdea2025.2\\scratches\\scratch_101.html";
            Path path = Path.of(url);
            String content = Files.readString(path);
            List<Map<String, String>> tournaments = importTournamentService.extractTournaments(content);
            assertEquals(121, tournaments.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Disabled
    @WithMockUser(username = "swaveklorenc@gmail.com", authorities = {UserRoles.TournamentDirectors})
    public void testImportTournamentEntries() {
        try {
            String url = "C:\\Users\\Swavek\\AppData\\Roaming\\JetBrains\\IntelliJIdea2025.2\\scratches\\scratch_97.html";
            String emailsFileRepoPath = "C:\\Users\\Swavek\\Documents\\2025 Aurora Cup\\Email campaign\\AllPlayers.csv";
            Path path = Path.of(url);
            String content = Files.readString(path);  //
            importTournamentService.importEntriesInternal(892, content, emailsFileRepoPath, new ImportProgressInfo());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Disabled
    @WithMockUser(username = "swaveklorenc@gmail.com", authorities = {UserRoles.TournamentDirectors})
    public void testImportTournamentConfiguration() {
        try {
            ImportProgressInfo importProgressInfo = new ImportProgressInfo();
            String playerListUrl = "C:\\Users\\Swavek\\AppData\\Roaming\\JetBrains\\IntelliJIdea2025.2\\scratches\\scratch_97.html";
            String content = Files.readString(Path.of(playerListUrl));  //
            Map<String, Map<String, String>> eventNamesAndCodes = new HashMap<>();
            importTournamentService.extractEventNamesAndCodes(content, eventNamesAndCodes, importProgressInfo);
            assertEquals(28, eventNamesAndCodes.size());

            String playerListbyEventUrl = "C:\\Users\\Swavek\\AppData\\Roaming\\JetBrains\\IntelliJIdea2025.2\\scratches\\scratch_100.html";
            String content2 = Files.readString(Path.of(playerListbyEventUrl));  //
            importTournamentService.extractAdditionalEventInfo(content2, eventNamesAndCodes, importProgressInfo);
            for (String eventName : eventNamesAndCodes.keySet()) {
                assertNotNull(eventName);
                Map<String, String> eventInfoMap = eventNamesAndCodes.get(eventName);
                String maxSlots = eventInfoMap.get("maxSlots");
                assertNotNull(maxSlots);
                String totalEntries = eventInfoMap.get("totalEntries");
                assertNotNull(totalEntries);
            }

            ImportTournamentRequest importTournamentRequest = new ImportTournamentRequest();
            importTournamentRequest.tournamentId = 0;
            importTournamentRequest.tournamentName = "2025 Edgeball Chicago International Open";
            importTournamentRequest.tournamentCity = "Barrington";
            importTournamentRequest.tournamentState = "Illinois";
            importTournamentRequest.tournamentStarLevel = "4-Star";
            importTournamentRequest.tournamentDirectorName = "Engelbert Solis";
            importTournamentRequest.tournamentDirectorEmail = "esolis@yahoo.com";
            importTournamentRequest.tournamentDirectorPhone = "123-454-7766";
            importTournamentRequest.tournamentDates = "10/25/25 - 10/26/25";
            importTournamentRequest.ballType = "JOOLA Prime";
            Tournament tournament = importTournamentService.convertRequestToTournament(importTournamentRequest);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy");
            Date startDate = simpleDateFormat.parse("10/25/25");
            Date endDate = simpleDateFormat.parse("10/26/25");
            assertEquals(startDate, tournament.getStartDate());
            assertEquals(endDate, tournament.getEndDate());

            importTournamentService.createUpdateEvents(tournament, eventNamesAndCodes);

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMaxPlayerRating() {
        int maxPlayerRating = importTournamentService.getMaxPlayerRating("OSNGL");
        assertEquals(0, maxPlayerRating);
        maxPlayerRating = importTournamentService.getMaxPlayerRating("U2350");
        assertEquals(2349, maxPlayerRating);
        maxPlayerRating = importTournamentService.getMaxPlayerRating("U1900");
        assertEquals(1899, maxPlayerRating);
        maxPlayerRating = importTournamentService.getMaxPlayerRating("D3800");
        assertEquals(3799, maxPlayerRating);
        maxPlayerRating = importTournamentService.getMaxPlayerRating("U0900");
        assertEquals(899, maxPlayerRating);
    }
    
    @Test
    public void testMinPlayerAgeFromCode() {
        int minAge = importTournamentService.getMinPlayerAge("OVR70");
        assertEquals(70, minAge);
        minAge = importTournamentService.getMinPlayerAge("U19B");
        assertEquals(0, minAge);
        minAge = importTournamentService.getMinPlayerAge("U17BD");
        assertEquals(0, minAge);
        minAge = importTournamentService.getMinPlayerAge("U19XD");
        assertEquals(0, minAge);
        minAge = importTournamentService.getMinPlayerAge("U1900");
        assertEquals(0, minAge);
    }

    @Test
    public void testMaxPlayerAgeFromCode() {
        int maxAge = importTournamentService.getMaxPlayerAge("OVR70");
        assertEquals(0, maxAge);
        maxAge = importTournamentService.getMaxPlayerAge("U9B");
        assertEquals(9, maxAge);
        maxAge = importTournamentService.getMaxPlayerAge("U19B");
        assertEquals(19, maxAge);
        maxAge = importTournamentService.getMaxPlayerAge("U17BD");
        assertEquals(17, maxAge);
        maxAge = importTournamentService.getMaxPlayerAge("U19XD");
        assertEquals(19, maxAge);
    }

    @Test
    public void testGenderRestrictionFromCode() {
        GenderRestriction genderRestriction = importTournamentService.genderRestriction("OVR70");
        assertEquals(GenderRestriction.NONE, genderRestriction);
        genderRestriction = importTournamentService.genderRestriction("U19XD");
        assertEquals(GenderRestriction.NONE, genderRestriction);

        // Male
        genderRestriction = importTournamentService.genderRestriction("U9B");
        assertEquals(GenderRestriction.MALE, genderRestriction);
        genderRestriction = importTournamentService.genderRestriction("U19B");
        assertEquals(GenderRestriction.MALE, genderRestriction);
        genderRestriction = importTournamentService.genderRestriction("U17BD");
        assertEquals(GenderRestriction.MALE, genderRestriction);

        // Female
        genderRestriction = importTournamentService.genderRestriction("WOMEN");
        assertEquals(GenderRestriction.FEMALE, genderRestriction);
        genderRestriction = importTournamentService.genderRestriction("U9G");
        assertEquals(GenderRestriction.FEMALE, genderRestriction);
        genderRestriction = importTournamentService.genderRestriction("U19G");
        assertEquals(GenderRestriction.FEMALE, genderRestriction);
        genderRestriction = importTournamentService.genderRestriction("U17GD");
        assertEquals(GenderRestriction.FEMALE, genderRestriction);
    }

    @Test
    @Disabled
    public void removeUnwantedProfiles() {
        String fileWithIds = "C:\\Users\\Swavek\\Downloads\\createdprofiles.txt";
//        String fileWithIds = "C:\\Users\\Swavek\\Downloads\\createdprofilesAustin.txt";

        Set<String> profileIdsToDelete = this.importTournamentService.removeUnwantedProfiles(new ImportProgressInfo(),
                fileWithIds);
        StringBuilder sb = new StringBuilder();
        String idsAsList = StringUtils.join(profileIdsToDelete, "','");
        sb.append("DELETE from userprofileext where profile_id in ")
                .append("('")
                .append(idsAsList)
                .append("');");
        System.out.println(sb.toString());
    }
}
