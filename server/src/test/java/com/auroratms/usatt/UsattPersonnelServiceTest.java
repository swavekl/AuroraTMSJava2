package com.auroratms.usatt;

import com.auroratms.AbstractServiceTest;
import com.auroratms.profile.UserProfile;
import com.auroratms.users.UserRoles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UsattPersonnelServiceTest extends AbstractServiceTest {

    @Autowired
    private UsattPersonnelService usattPersonnelService;

    @Test
    public void testGettingTournamentsPerson () {
        UserProfile personInRole = usattPersonnelService.getPersonInRole(UserRoles.USATTTournamentManagers);
        assertNotNull(personInRole);
        String firstName = personInRole.getFirstName();
        String lastName = personInRole.getLastName();
        assertEquals("Tina", firstName, "wrong first name");
        assertEquals("Ren", lastName, "wrong last name");
    }

    @Test
    public void testGettingSanctionCoordinators () {
        UserProfile midwestCoordinator = usattPersonnelService.getSanctionCoordinator("Midwest");
        assertNotNull(midwestCoordinator);
        String firstName = midwestCoordinator.getFirstName();
        String lastName = midwestCoordinator.getLastName();
        assertEquals("Ed", firstName, "wrong first name");
        assertEquals("Hogshead", lastName, "wrong last name");

        UserProfile nationalCoordinator = usattPersonnelService.getSanctionCoordinator("National");
        assertNotNull(nationalCoordinator);
        firstName = nationalCoordinator.getFirstName();
        lastName = nationalCoordinator.getLastName();
        assertEquals("Larry", firstName, "wrong first name");
        assertEquals("Thoman", lastName, "wrong last name");

        UserProfile montainCoordinator = usattPersonnelService.getSanctionCoordinator("Mountain");
        assertNotNull(montainCoordinator);
        firstName = montainCoordinator.getFirstName();
        lastName = montainCoordinator.getLastName();
        assertEquals("Larry", firstName, "wrong first name");
        assertEquals("Thoman", lastName, "wrong last name");
    }
}
