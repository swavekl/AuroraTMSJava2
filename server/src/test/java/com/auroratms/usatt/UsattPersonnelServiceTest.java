package com.auroratms.usatt;

import com.auroratms.AbstractServiceTest;
import com.auroratms.profile.UserProfile;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UsattPersonnelServiceTest extends AbstractServiceTest {

    @Autowired
    private UsattPersonnelService usattPersonnelService;

    @Test
    public void testGettingTournamentsPerson () {
        UserProfile personInRole = usattPersonnelService.getPersonInRole(UsattPersonnelService.DEPARTMENT_TOURNAMENTS);
        assertNotNull(personInRole);
        String firstName = personInRole.getFirstName();
        String lastName = personInRole.getLastName();
        assertEquals("wrong first name", "Tina", firstName);
        assertEquals("wrong last name", "Ren", lastName);
    }

    @Test
    public void testGettingSanctionCoordinators () {
        UserProfile midwestCoordinator = usattPersonnelService.getSanctionCoordinator("Midwest");
        assertNotNull(midwestCoordinator);
        String firstName = midwestCoordinator.getFirstName();
        String lastName = midwestCoordinator.getLastName();
        assertEquals("wrong first name", "Ed", firstName);
        assertEquals("wrong last name", "Hogshead", lastName);

        UserProfile nationalCoordinator = usattPersonnelService.getSanctionCoordinator("National");
        assertNotNull(nationalCoordinator);
        firstName = nationalCoordinator.getFirstName();
        lastName = nationalCoordinator.getLastName();
        assertEquals("wrong first name", "Larry", firstName);
        assertEquals("wrong last name", "Thoman", lastName);

        UserProfile montainCoordinator = usattPersonnelService.getSanctionCoordinator("Mountain");
        assertNotNull(montainCoordinator);
        firstName = montainCoordinator.getFirstName();
        lastName = montainCoordinator.getLastName();
        assertEquals("wrong first name", "Larry", firstName);
        assertEquals("wrong last name", "Thoman", lastName);
    }
}
