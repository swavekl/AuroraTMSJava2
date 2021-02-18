package com.auroratms.usatt;

import com.auroratms.server.ServerApplication;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
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
public class UsattDataServiceTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private UsattDataService usattDataService;

    @Test ()
//    @Ignore
    public void testFetchOneUser () {
        String filename = "C:\\myprojects\\DubinaRecords.csv";
        List<UsattPlayerRecord> usattPlayerInfos = usattDataService.readAllPlayersFromFile(filename);
        assertEquals("wrong count of records inserted into db", 11, usattPlayerInfos.size());

        usattDataService.insertPlayerData(usattPlayerInfos);
        long count = usattDataService.getTotalCount();
        assertEquals("wrong count of records inserted into db", 11, count);

        List<UsattPlayerRecord> playerInfos = usattDataService.findAllPlayersByNames("Samson", "Dubina", PageRequest.of(0, 3));
        assertEquals("wrong count", 1, playerInfos.size());
        UsattPlayerRecord playerInfo = playerInfos.get(0);
        assertEquals ("wrong first name", playerInfo.getFirstName(), "Samson");
        assertEquals ("wrong last name", playerInfo.getLastName(), "Dubina");
        assertEquals ("wrong membership id", playerInfo.getMembershipId().longValue(), 9051L);

        UsattPlayerRecord playerByMembershipId = usattDataService.getPlayerByMembershipId(9051L);
        assertNotNull("didn't find player", playerByMembershipId);
        assertEquals ("wrong first name", playerInfo.getFirstName(), "Samson");
        assertEquals ("wrong last name", playerInfo.getLastName(), "Dubina");
        assertEquals ("wrong membership id", playerInfo.getMembershipId().longValue(), 9051L);

    }

    @Test
    @Ignore
    public void testReadingFromCSV () {
        String filename = "C:\\myprojects\\TD Ratings File 9.26.2019.csv";
        List<UsattPlayerRecord> usattPlayerInfos = usattDataService.readAllPlayersFromFile(filename);
        assertTrue("wrong number of records", (usattPlayerInfos.size() > 60000));
        for (UsattPlayerRecord usattPlayerInfo : usattPlayerInfos) {
            assertNotNull("first name is null", usattPlayerInfo.getFirstName());
            assertNotNull("last name is null", usattPlayerInfo.getLastName());
            assertNotNull("Gender is null", usattPlayerInfo.getGender());
        }

        usattDataService.insertPlayerData(usattPlayerInfos);
        long count = usattDataService.getTotalCount();
        assertEquals("wrong count of records inserted into db", usattPlayerInfos.size(), count);

    }

    @Test
    public void testZipACountry () {
        assertFalse(usattDataService.isZipACountry("12345"));
        assertFalse(usattDataService.isZipACountry("12345-9087"));
        // canadian zipcodes
        assertFalse(usattDataService.isZipACountry("K6V5V8"));
        assertFalse(usattDataService.isZipACountry("L1K 2R4"));

        // countries
//        assertTrue(usattDataService.isZipACountry("Slovak"));
        assertTrue(usattDataService.isZipACountry("Trinidad & Tobago"));
//        assertTrue(usattDataService.isZipACountry("TN"));
        assertTrue(usattDataService.isZipACountry("Puerto Rico"));
    }
}
