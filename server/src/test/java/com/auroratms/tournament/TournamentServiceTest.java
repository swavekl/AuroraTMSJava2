package com.auroratms.tournament;

import com.auroratms.server.ServerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ServerApplication.class})
@ContextConfiguration
@TestExecutionListeners(listeners={ServletTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        WithSecurityContextTestExecutionListener.class})
@Transactional
public class TournamentServiceTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private TournamentService tournamentService;

    @Test
    @WithMockUser(username = "swaveklorenc@yahoo.com", authorities = {"Admins"})
    public void adminGetAll (){
        Collection<Tournament> list = tournamentService.list();
        for (Tournament tournament : list) {
            System.out.println("tournament.getName() = " + tournament.getName());
        }
        assertTrue ("wrong count of tournaments", list.size() > 0);

        int sizeBefore = list.size();
        Tournament tournament = makeTournament("2020 Aurora Fall Open");
        Tournament saveTournament = tournamentService.saveTournament(tournament);
        assertNotNull("tournament id is null", saveTournament.getId());
        Collection<Tournament> newList = tournamentService.list();
        int sizeAfter = newList.size();
        assertTrue("", sizeBefore < sizeAfter);
    }

    @Test
    @WithMockUser(username="mario", authorities = {"TournamentDirector"})
    public void tournamentDirectorGetOwned (){
        String [] tournamentNames = new String [] {
            "2020 Phoenix Sizzler Open",
            "2021 Phoenix Sizzler Open",
            "2022 Phoenix Sizzler Open",
            "2023 Phoenix Sizzler Open",
            "2024 Phoenix Sizzler Open",
            "2025 Phoenix Sizzler Open",
            "2026 Phoenix Sizzler Open",
            "2027 Phoenix Sizzler Open"
        };
        List<Long> tournamentIds = new ArrayList<Long>();
        for (int i = 0; i < tournamentNames.length; i++) {
            String tournamentName = tournamentNames[i];
            Tournament tournament = tournamentService.saveTournament(makeTournament(tournamentName));
            tournamentIds.add(tournament.getId());
        }

        int resultIndex = 0;
        for (int i = 0; i < 3; i++) {
            Collection<Tournament> tournaments = tournamentService.listOwned(i, 3);
            assertTrue("page of tournaments empty", tournaments.size() > 0);
            for (Tournament tournament : tournaments) {
                String expectedTournamentName = tournamentNames[resultIndex];
                assertEquals("wrong tournament name", expectedTournamentName, tournament.getName());
                resultIndex++;
            }
        }

        // delete tournaments one by one
        for (Long tournamentId : tournamentIds) {
            tournamentService.deleteTournament(tournamentId);
        }

        // check they were deleted
        Collection<Tournament> tournaments = tournamentService.listOwned(0, 100);
        assertEquals("tournaments left after deletion",0,  tournaments.size());
    }

    private Tournament makeTournament(String name) {
        Tournament tournament = new Tournament();
        tournament.setName(name);
        tournament.setCity("Rockford");
        tournament.setState("IL");
        tournament.setEndDate(new Date());
        tournament.setStartDate(new Date());
        return tournament;
    }

    @Test
    @WithMockUser(username = "swaveklorenc@yahoo.com", authorities = {"Admins"})
    public void testOwnersAccess () {
        String[] tournamentsForEnglebert = makeTournamentsForEnglebert();
        String[] tournamentForEd = makeTournamentsForEd();
        checkEngelbertsTournaments(tournamentsForEnglebert);
        checkEdsTournaments(tournamentForEd);
        // as admin
        Collection<Tournament> list = tournamentService.listOwned(0, 15);
        int expectedCount = tournamentsForEnglebert.length + tournamentForEd.length;
        assertEquals("not all tournaments were accessible to admin", expectedCount, list.size());
        Collection<Tournament> listAll = tournamentService.list();
        assertTrue("More tournaments should be visible", listAll.size() > expectedCount);
    }

    private void checkEngelbertsTournaments(String[] tournamentNames) {
        Authentication savedAuthentication = switchAuthentication("engelbert@gmail.com");
        checkTournaments(tournamentNames);
        SecurityContextHolder.getContext().setAuthentication(savedAuthentication);
    }

    private void checkEdsTournaments(String[] tournamentNames) {
        Authentication savedAuthentication = switchAuthentication("ed@gmail.com");
        checkTournaments(tournamentNames);
        SecurityContextHolder.getContext().setAuthentication(savedAuthentication);
    }

    private void checkTournaments(String[] tournamentNames) {
        Collection<Tournament> tournaments = tournamentService.listOwned(0, 10);
        int countFound = 0;
        for (String tournamentName : tournamentNames) {
            boolean found = false;
            for (Tournament tournament : tournaments) {
                if (tournament.getName().equals(tournamentName)) {
                    countFound++;
                    found = true;
                    break;
                }
            }
            assertTrue("Tournament " + tournamentName + " found", found);
        }
        assertEquals("not found", countFound, tournamentNames.length);
    }

    private String[] makeTournamentsForEnglebert() {
        Authentication savedAuthentication = switchAuthentication("engelbert@gmail.com");

        String [] tournamentNames = new String [] {
                "2020 Libertyville Open",
                "2021 Libertyville Open",
                "2022 Libertyville Open",
                "2023 Libertyville Open",
                "2024 Libertyville Open",
                "2025 Libertyville Open"
        };
        for (String tournamentName : tournamentNames) {
            tournamentService.saveTournament(makeTournament(tournamentName));
        }

        SecurityContextHolder.getContext().setAuthentication(savedAuthentication);
        return tournamentNames;
    }

    private String[] makeTournamentsForEd() {
        Authentication savedAuthentication = switchAuthentication("ed@gmail.com");
        String [] tournamentNames = new String [] {
                "2020 Americas Team Championship Open",
                "2021 Americas Team Championship Open",
                "2022 Americas Team Championship Open"
        };
        for (String tournamentName : tournamentNames) {
            tournamentService.saveTournament(makeTournament(tournamentName));
        }
        SecurityContextHolder.getContext().setAuthentication(savedAuthentication);

        return tournamentNames;
    }

    private Authentication switchAuthentication(String username) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("TournamentDirector"));
        User principal = new User(username, "password", true, true, true, true,
                grantedAuthorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, principal.getPassword(), principal.getAuthorities());
        Authentication savedAuthentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return savedAuthentication;
    }

}
