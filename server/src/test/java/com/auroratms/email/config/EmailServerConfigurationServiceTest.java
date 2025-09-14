package com.auroratms.email.config;

import com.auroratms.AbstractServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class EmailServerConfigurationServiceTest extends AbstractServiceTest {

    @Autowired
    private EmailServerConfigurationService service;

    @Test
    public void testCRUD () {
        String userProfileId = "mytestprofile";
        EmailServerConfigurationEntity entity = new EmailServerConfigurationEntity();
        entity.setProfileId(userProfileId);
        entity.setServerHost("mail.yahoo.com");
        entity.setServerPort(587);
        entity.setUserId("someuser@yahoo.com");
        entity.setPassword("secret");
        EmailServerConfigurationEntity saved = service.save(entity);

        boolean existsById = service.existsById(userProfileId);
        assertTrue(existsById, "doesn't exist but should");

        EmailServerConfigurationEntity found = service.findById(userProfileId);

        found.setPassword("updatedPassword");
        EmailServerConfigurationEntity updated = service.save(found);

        String password = updated.getPassword();
        assertEquals("updatedPassword", password, "wrong password");

        service.delete(userProfileId);
        existsById = service.existsById(userProfileId);
        assertFalse(existsById, "Exist but shouldn't");
    }
}
