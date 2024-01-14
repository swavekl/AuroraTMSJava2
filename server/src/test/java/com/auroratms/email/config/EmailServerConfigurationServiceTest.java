package com.auroratms.email.config;

import com.auroratms.AbstractServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@Transactional
public class EmailServerConfigurationServiceTest extends AbstractServiceTest {

    @Autowired
    private EmailServerConfigurationService service;

    @Test
    public void testCRUD () {
        String userProfileId = "mytestprofile";
        EmailServerConfigurationEntity entity = new EmailServerConfigurationEntity();
        entity.setId(userProfileId);
        entity.setServerHost("mail.yahoo.com");
        entity.setServerPort(587);
        entity.setUserId("someuser@yahoo.com");
        entity.setPassword("secret");
        EmailServerConfigurationEntity saved = service.save(entity);

        boolean existsById = service.existsById(userProfileId);
        assertTrue("doesn't exist but should", existsById);

        EmailServerConfigurationEntity found = service.findById(userProfileId);

        found.setPassword("updatedPassword");
        EmailServerConfigurationEntity updated = service.save(found);

        String password = updated.getPassword();
        assertEquals("wrong password", "updatedPassword", password);

        service.delete(userProfileId);
        existsById = service.existsById(userProfileId);
        assertFalse("Exist but shouldn't", existsById);
    }
}
