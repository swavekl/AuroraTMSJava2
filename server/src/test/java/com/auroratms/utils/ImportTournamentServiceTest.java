package com.auroratms.utils;

import com.auroratms.AbstractServiceTest;
import com.auroratms.users.UserRoles;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ImportTournamentServiceTest extends AbstractServiceTest {

    @Autowired
    private ImportTournamentService importTournamentService;

    @Test
    @WithMockUser(username = "swaveklorenc@gmail.com", authorities = {UserRoles.TournamentDirectors})
    @Disabled
    public void testMatching () {
        ImportTournamentRequest importTournamentRequest = new ImportTournamentRequest();
        importTournamentRequest.playersUrl = "T-tourney.asp?t=100&r=5115";
        importTournamentRequest.blankEntryFormPDFUrl = "expected-json\\1121-55-LYTTC November Open 2025.json";
        importTournamentRequest.tournamentStarLevel = "0";
        importTournamentRequest.tournamentDates = "11/08/25 - 11/09/25";
        importTournamentRequest.tournamentName = "LYTTC November Open 2025";
        importTournamentRequest.tournamentCity = "Dunellen";
        importTournamentRequest.tournamentState = "NJ";
        importTournamentRequest.ballType = "";
        importTournamentRequest.tournamentDirectorName = "Judy Hugh";
        importTournamentRequest.tournamentDirectorPhone = "732-200-5820";
        importTournamentRequest.tournamentDirectorEmail = "Lyttc2017@gmail.com";

        importTournamentService.importTournamentConfiguration(importTournamentRequest, new ImportProgressInfo());
    }

    @Test
    @WithMockUser(username = "swaveklorenc@gmail.com", authorities = {UserRoles.TournamentDirectors})
    public void testJuniorsMatching () {
        ImportTournamentRequest importTournamentRequest = new ImportTournamentRequest();
        importTournamentRequest.playersUrl = "T-tourney.asp?t=100&r=4839";
        importTournamentRequest.blankEntryFormPDFUrl = "expected-json\\1124-39_Florida State Open.json";
        importTournamentRequest.tournamentStarLevel = "4";
        importTournamentRequest.tournamentDates = "12/12/25 - 12/14/25";
        importTournamentRequest.tournamentName = "Carmel Barrau International Open";
        importTournamentRequest.tournamentCity = "Davie";
        importTournamentRequest.tournamentState = "FL";
        importTournamentRequest.ballType = "Nittaku 3* Premium 40+";
        importTournamentRequest.tournamentDirectorName = "Carlos Zeller";
        importTournamentRequest.tournamentDirectorPhone = "954 849 5436";
        importTournamentRequest.tournamentDirectorEmail = "browardttc@gmail.com";

        importTournamentService.importTournamentConfiguration(importTournamentRequest, new ImportProgressInfo());
    }

}
