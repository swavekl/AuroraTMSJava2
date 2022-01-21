package com.auroratms.tournament;

import com.auroratms.event.TournamentEvent;
import com.auroratms.server.ServerApplication;
import com.auroratms.users.UserRoles;
import org.junit.Ignore;
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
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ServerApplication.class})
@ContextConfiguration
@WebAppConfiguration(value = "src/test/resources")
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
    @WithMockUser(username = "swaveklorenc@yahoo.com", authorities = {UserRoles.Admins})
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
    @WithMockUser(username="mario", authorities = {UserRoles.TournamentDirectors})
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

    private void addPersonnel (String profileId, String fullName, String userRole, Tournament tournament) {
        List<Personnel> personnelList = tournament.getConfiguration().getPersonnelList();
        if (personnelList == null) {
            personnelList = new ArrayList<>();
            tournament.getConfiguration().setPersonnelList(personnelList);
        }
        Personnel personnel = new Personnel();
        personnel.setName(fullName);
        personnel.setProfileId(profileId);
        personnel.setRole(userRole);
        personnelList.add(personnel);
    }

    private void removePersonnel(String profileId, Tournament tournament) {
        List<Personnel> personnelList = tournament.getConfiguration().getPersonnelList();
        if (personnelList != null) {
            for (Personnel personnel : personnelList) {
                if (personnel.getProfileId().equals(profileId)) {
                    personnelList.remove(personnel);
                    break;
                }
            }
        }
    }

    private Tournament makeTournament(String name) {
        Tournament tournament = new Tournament();
        tournament.setName(name);
        tournament.setCity("Rockford");
        tournament.setState("IL");
        tournament.setEndDate(new Date());
        tournament.setStartDate(new Date());
        TournamentConfiguration tournamentConfiguration = new TournamentConfiguration();
        tournamentConfiguration.setEligibilityDate(new Date());
        tournamentConfiguration.setEntryCutoffDate(new Date());
        tournament.setConfiguration(tournamentConfiguration);
        return tournament;
    }

    @Test
    @WithMockUser(username = "swaveklorenc@yahoo.com", authorities = {UserRoles.Admins})
    public void testOwnersAccess () {
        String[] tournamentsForEnglebert = makeTournamentsForEnglebert();
        String[] tournamentForEd = makeTournamentsForEd();
        checkEngelbertsTournaments(tournamentsForEnglebert);
        checkEdsTournaments(tournamentForEd);
        // as admin
        Collection<Tournament> adminVisibleTournaments = tournamentService.listOwned(0, 100);
        for (String tournamentName : tournamentForEd) {
            boolean contains = false;
            for (Tournament tournament : adminVisibleTournaments) {
                if (tournament.getName().equals(tournamentName)) {
                    contains = true;
                    break;
                }
            }
            assertTrue(tournamentName + " tournament is not visible to Admin", contains);
        }
        for (String tournamentName : tournamentsForEnglebert) {
            boolean contains = false;
            for (Tournament tournament : adminVisibleTournaments) {
                if (tournament.getName().equals(tournamentName)) {
                    contains = true;
                    break;
                }
            }
            assertTrue(tournamentName + " tournament is not visible to Admin", contains);
        }

        int expectedCount = tournamentsForEnglebert.length + tournamentForEd.length;
//        assertEquals("not all tournaments were accessible to admin", expectedCount, list.size());
        Collection<Tournament> listAll = tournamentService.list();
        assertTrue("More tournaments should be visible", listAll.size() > expectedCount);
    }

    private void checkEngelbertsTournaments(String[] tournamentNames) {
        Authentication savedAuthentication = switchAuthentication("engelbert@gmail.com", UserRoles.TournamentDirectors);
        checkTournaments(tournamentNames);
        SecurityContextHolder.getContext().setAuthentication(savedAuthentication);
    }

    private void checkEdsTournaments(String[] tournamentNames) {
        Authentication savedAuthentication = switchAuthentication("ed@gmail.com", UserRoles.TournamentDirectors);
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
            assertTrue("Tournament '" + tournamentName + "' not found", found);
        }
        assertEquals("not found", countFound, tournamentNames.length);
    }

    private String[] makeTournamentsForEnglebert() {
        Authentication savedAuthentication = switchAuthentication("engelbert@gmail.com", UserRoles.TournamentDirectors);

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
        Authentication savedAuthentication = switchAuthentication("ed@gmail.com", UserRoles.TournamentDirectors);
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

    private Authentication switchAuthentication(String username, String role) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(role));
        User principal = new User(username, "password", true, true, true, true,
                grantedAuthorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, principal.getPassword(), principal.getAuthorities());
        Authentication savedAuthentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return savedAuthentication;
    }

    @Test
    @Ignore
    @WithMockUser(username="mario", authorities = {UserRoles.TournamentDirectors})
    public void testEvents () {
        Tournament tournament = makeTournament("2020 Aurora Summer Open with events");
        Set<TournamentEvent> events = new HashSet<>();
        String [] eventNames = { "Open", "U2400", "U2200", "U2000", "U1900", "U1800", "U1600"
        };
        for (String eventName : eventNames) {
            TournamentEvent event = makeEvent (eventName);
            events.add(event);
        }
        tournament.setEvents(events);

        Tournament savedTournament = tournamentService.saveTournament(tournament);
        Tournament returnedTournament = tournamentService.getByKey(savedTournament.getId());
        Set<TournamentEvent> savedEvents = returnedTournament.getEvents();
        assertEquals("wrong number of events", eventNames.length, savedEvents.size());

        savedEvents.add(makeEvent("U1400"));
        savedEvents.add(makeEvent("U1200"));
        savedEvents.add(makeEvent("U1000"));
        savedEvents.add(makeEvent("U800"));
        tournamentService.saveTournament(returnedTournament);
        Tournament anotherTournament = tournamentService.getByKey(savedTournament.getId());
        Set<TournamentEvent> events1 = anotherTournament.getEvents();
        assertEquals("wrong events after addition", (eventNames.length + 4), events1.size());
    }

    private TournamentEvent makeEvent(String eventName) {
        TournamentEvent eventEntity = new TournamentEvent();
        eventEntity.setName(eventName);
        return eventEntity;
    }

    @Test
    @WithMockUser(username="swavek", authorities = {UserRoles.TournamentDirectors})
    public void testPersonnelAccess () {
        Tournament tournament = makeTournament("2022 Aurora Summer Open");
        addPersonnel("00u107pgb64tCP7wn0h8","Nagarathnam, Uma", UserRoles.DataEntryClerks, tournament);
        addPersonnel("xyz09123214","Vanegas, Jorge", UserRoles.Umpires, tournament);
        addPersonnel("xyz09123214","Leaf, Linda", UserRoles.Umpires, tournament);
        tournament = tournamentService.saveTournament(tournament);

//        Tournament tournament2 = makeTournament("2022 Badger Open");
//        addPersonnel("xyz09123213","Robinson, Ted", UserRoles.DataEntryClerks, tournament2);
//        addPersonnel("xyz09123214","Vanegas, Jorge", UserRoles.Umpires, tournament2);
//        addPersonnel("xyz09123214","Leaf, Linda", UserRoles.Umpires, tournament2);
//        tournament2 = tournamentService.saveTournament(tournament2);

        Authentication savedAuthentication = switchAuthentication("swaveklorenc+uma@gmail.com", UserRoles.DataEntryClerks);
        Collection<Tournament> tournamentsForUma = tournamentService.listOwned(0, 100);
        SecurityContextHolder.getContext().setAuthentication(savedAuthentication);

        Tournament[] tournamentsArray = tournamentsForUma.toArray(new Tournament [0]);
        Tournament first = tournamentsArray[0];
        assertEquals("2022 Aurora Summer Open", first.getName());

        removePersonnel("00u107pgb64tCP7wn0h8", tournament);
        tournament = tournamentService.saveTournament(tournament);

        savedAuthentication = switchAuthentication("swaveklorenc+uma@gmail.com", UserRoles.DataEntryClerks);
        Collection<Tournament> tournamentsForUma2 = tournamentService.listOwned(0, 100);
        SecurityContextHolder.getContext().setAuthentication(savedAuthentication);

        assertEquals("wrong number of tournaments", 0, tournamentsForUma2.size());

    }
}
