package com.auroratms.sanction;

import com.auroratms.server.ServerApplication;
import com.auroratms.users.UserRoles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {ServerApplication.class})
@ActiveProfiles("test")
@ContextConfiguration
@WebAppConfiguration(value = "src/test/resources")
@TestExecutionListeners(listeners = {ServletTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        WithSecurityContextTestExecutionListener.class})
@Transactional
public class SanctionRequestServiceTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private SanctionRequestService service;

    @Test
    @WithMockUser(username = "swaveklorenc@gmail.com", authorities = {UserRoles.TournamentDirectors})
    public void tournamentDirectorGetOwned() {

        SanctionRequest sanctionRequest = makeTournamentForSwavek("2023 Aurora Winter Giant RR", 1);

        SanctionRequestEntity sanctionRequestEntity = sanctionRequest.convertToEntity();
        SanctionRequestEntity saved = service.save(sanctionRequestEntity);

        Page<SanctionRequestEntity> page = service.findByName("", Pageable.unpaged());
        long totalElements = page.getTotalElements();
        assertEquals(1, totalElements, "wrong number of sanction requests");
        List<SanctionRequestEntity> sanctionRequests = page.getContent();
        assertEquals(1, sanctionRequests.size(), "");
        SanctionRequestEntity sanctionRequestEntity1 = sanctionRequests.get(0);
        assertEquals(saved.getTournamentName(), sanctionRequestEntity1.getTournamentName(), "wrong name");
    }

    @Test
    @WithMockUser(username = "swaveklorenc@gmail.com", authorities = {UserRoles.TournamentDirectors})
    public void checkCoordinatorAccess() {

        SanctionRequest sanctionRequest = makeTournamentForSwavek("2023 Aurora Winter Special", 3);

        SanctionRequestEntity sanctionRequestEntity = sanctionRequest.convertToEntity();
        SanctionRequestEntity saved = service.save(sanctionRequestEntity);

        Authentication previousUser = switchAuthentication("swaveklorenc+edho@gmail.com", UserRoles.USATTSanctionCoordinators);
        Page<SanctionRequestEntity> edVisibleSRs = service.findByName(null, Pageable.unpaged());
        List<SanctionRequestEntity> sanctionRequestsList = edVisibleSRs.getContent();
        for (SanctionRequestEntity requestEntity : sanctionRequestsList) {
            System.out.println("requestEntity.getTournamentName() = " + requestEntity.getTournamentName());
            System.out.println("requestEntity.getStarLevel() = " + requestEntity.getStarLevel());

        }
        assertEquals(1, edVisibleSRs.getTotalElements(), "wrong number of tournaments");

        SanctionRequestEntity sanctionRequestEntity1 = sanctionRequestsList.get(0);
        SecurityContextHolder.getContext().setAuthentication(previousUser);

    }

    private SanctionRequest makeTournamentForSwavek(String tournamentName, int starLevel) {
        SanctionRequest sanctionRequest = new SanctionRequest();
        sanctionRequest.setTournamentName(tournamentName);
        sanctionRequest.setVenueCity("Aurora");
        sanctionRequest.setVenueState("IL");
        sanctionRequest.setVenueZipCode("50504");
        sanctionRequest.setVenueStreetAddress("555 S. Eola Rd.");
        sanctionRequest.setStartDate(new Date());
        sanctionRequest.setEndDate(new Date());
        sanctionRequest.setRequestDate(new Date());
        sanctionRequest.setStatus(SanctionRequestStatus.New);
        sanctionRequest.setStarLevel(starLevel);
        sanctionRequest.setSanctionFee(150);
        sanctionRequest.setClubName("Fox Valley Table Tennis Club");
        sanctionRequest.setClubAffiliationExpiration(new Date());
        sanctionRequest.setContactPersonName("Swavek Lorenc");
        sanctionRequest.setContactPersonState("IL");
        sanctionRequest.setCoordinatorFirstName("Ed");
        sanctionRequest.setCoordinatorLastName("Hogshead");
        sanctionRequest.setCoordinatorEmail("swaveklorenc+edho@gmail.com");
        sanctionRequest.setTournamentRefereeName("Linda Leaf");
        sanctionRequest.setTournamentRefereeRank("IU/NR");
        sanctionRequest.setTournamentRefereeMembershipExpires(new Date());
        return sanctionRequest;
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

}
