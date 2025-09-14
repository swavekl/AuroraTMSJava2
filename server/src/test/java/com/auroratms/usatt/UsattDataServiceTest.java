package com.auroratms.usatt;

import com.auroratms.ratingsprocessing.MembershipsProcessorStatus;
import com.auroratms.ratingsprocessing.RatingsProcessorStatus;
import com.auroratms.server.ServerApplication;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testFetchOneUser () {
        String filename = "C:\\myprojects\\data\\DubinaRecords.csv";
        RatingsProcessorStatus ratingsProcessorStatus = new RatingsProcessorStatus();
        List<UsattPlayerRecord> usattPlayerInfos = usattDataService.readAllPlayersFromFile(filename, ratingsProcessorStatus);
        assertEquals(11, usattPlayerInfos.size(), "wrong count of records inserted into db");

        usattDataService.insertPlayerData(usattPlayerInfos, ratingsProcessorStatus);
        long count = usattDataService.getTotalCount();
        assertTrue(count > 68000, "wrong count of records inserted into db");

        UsattPlayerRecord playerInfo = usattDataService.getPlayerByNames("Samson", "Dubina");
        assertNotNull(playerInfo, "wrong count");
        assertEquals (playerInfo.getFirstName(), "Samson", "wrong first name");
        assertEquals (playerInfo.getLastName(), "Dubina", "wrong last name");
        assertEquals (playerInfo.getMembershipId().longValue(), 9051L, "wrong membership id");

        UsattPlayerRecord playerInfo2 = usattDataService.getPlayerByNames("SAMSON", "DUBINA");
        assertNotNull(playerInfo2, "wrong count");
        assertEquals (playerInfo2.getFirstName(), "Samson", "wrong first name");
        assertEquals (playerInfo2.getLastName(), "Dubina", "wrong last name");
        assertEquals (playerInfo2.getMembershipId().longValue(), 9051L, "wrong membership id");

        UsattPlayerRecord playerInfo3 = usattDataService.getPlayerByNames("SAMSON", "DUBINA ");
        assertNotNull(playerInfo3, "wrong count");
        assertEquals (playerInfo3.getFirstName(), "Samson", "wrong first name");
        assertEquals (playerInfo3.getLastName(), "Dubina", "wrong last name");
        assertEquals (playerInfo3.getMembershipId().longValue(), 9051L, "wrong membership id");

        UsattPlayerRecord playerByMembershipId = usattDataService.getPlayerByMembershipId(9051L);
        assertNotNull(playerByMembershipId, "didn't find player");
        assertEquals (playerInfo3.getFirstName(), "Samson", "wrong first name");
        assertEquals (playerInfo3.getLastName(), "Dubina", "wrong last name");
        assertEquals (playerInfo3.getMembershipId().longValue(), 9051L, "wrong membership id");

    }

    @Test
    @Disabled
    public void testReadingFromCSV () {
        String filename = "C:\\myprojects\\TD Ratings File 9.26.2019.csv";
        RatingsProcessorStatus ratingsProcessorStatus = new RatingsProcessorStatus();
        List<UsattPlayerRecord> usattPlayerInfos = usattDataService.readAllPlayersFromFile(filename, ratingsProcessorStatus);
        assertTrue((usattPlayerInfos.size() > 60000), "wrong number of records");
        for (UsattPlayerRecord usattPlayerInfo : usattPlayerInfos) {
            assertNotNull(usattPlayerInfo.getFirstName(), "first name is null");
            assertNotNull(usattPlayerInfo.getLastName(), "last name is null");
            assertNotNull(usattPlayerInfo.getGender(), "Gender is null");
        }

        usattDataService.insertPlayerData(usattPlayerInfos, ratingsProcessorStatus);
        long count = usattDataService.getTotalCount();
        assertEquals(usattPlayerInfos.size(), count, "wrong count of records inserted into db");

    }

    @Test
    @Disabled
    public void testRating() {
        // Samson's rating as of
//        INSERT INTO auroratmstest.usattratinghistory
//         (final_rating, final_rating_date, initial_rating, initial_rating_date, membership_id, tournament_fk)
//        VALUES
//         (2444, '2020-02-08 00:00:00', 2449, '2020-02-07 00:00:00', 9051, null)
//         (2415, '2020-02-21 00:00:00', 2444, '2020-02-08 00:00:00', 9051, null)
//         (2551, '2020-02-27 00:00:00', 2415, '2020-02-21 00:00:00', 9051, null)
//         (2437, '2020-03-01 00:00:00', 2551, '2020-02-27 00:00:00', 9051, null)
//         (2443, '2020-03-08 00:00:00', 2437, '2020-03-01 00:00:00', 9051, null);

        long membershipId = 9051L;

        int playerRatingAsOfDate = 0;

        Calendar calendar = Calendar.getInstance();
//        calendar.set(2020, Calendar.MARCH, 5, 0, 0 ,0);
//        playerRatingAsOfDate = usattDataService.getPlayerRatingAsOfDate(membershipId,  calendar.getTime());
//        assertEquals("wrong rating as of " + calendar.getTime(), 2437, playerRatingAsOfDate);

        calendar.set(2020, Calendar.MARCH, 9, 0, 0 ,0);
        calendar.set(Calendar.MILLISECOND, 0);
        playerRatingAsOfDate = usattDataService.getPlayerRatingAsOfDate(membershipId,  calendar.getTime());
        assertEquals(2443, playerRatingAsOfDate, "wrong rating as of " + calendar.getTime());

        calendar.set(2020, Calendar.MARCH, 8, 0, 0 ,0);
        playerRatingAsOfDate = usattDataService.getPlayerRatingAsOfDate(membershipId,  calendar.getTime());
        assertEquals(2443, playerRatingAsOfDate, "wrong rating as of " + calendar.getTime());

        calendar.set(2020, Calendar.FEBRUARY, 28, 0, 0 ,0);
        playerRatingAsOfDate = usattDataService.getPlayerRatingAsOfDate(membershipId,  calendar.getTime());
        assertEquals(2551, playerRatingAsOfDate, "wrong rating as of " + calendar.getTime());

        calendar.set(2020, Calendar.FEBRUARY, 27, 0, 0 ,0);
        playerRatingAsOfDate = usattDataService.getPlayerRatingAsOfDate(membershipId,  calendar.getTime());
        assertEquals(2437, playerRatingAsOfDate, "wrong rating as of " + calendar.getTime());

        calendar.set(2020, Calendar.FEBRUARY, 26, 0, 0 ,0);
        playerRatingAsOfDate = usattDataService.getPlayerRatingAsOfDate(membershipId,  calendar.getTime());
        assertEquals(2437, playerRatingAsOfDate, "wrong rating as of " + calendar.getTime());

        calendar.set(2020, Calendar.FEBRUARY, 22, 0, 0 ,0);
        playerRatingAsOfDate = usattDataService.getPlayerRatingAsOfDate(membershipId,  calendar.getTime());
        assertEquals(2437, playerRatingAsOfDate, "wrong rating as of " + calendar.getTime());

        calendar.set(2020, Calendar.FEBRUARY, 21, 0, 0 ,0);
        playerRatingAsOfDate = usattDataService.getPlayerRatingAsOfDate(membershipId,  calendar.getTime());
        assertEquals(2444, playerRatingAsOfDate, "wrong rating as of " + calendar.getTime());

        calendar.set(2020, Calendar.FEBRUARY, 20, 0, 0 ,0);
        playerRatingAsOfDate = usattDataService.getPlayerRatingAsOfDate(membershipId,  calendar.getTime());
        assertEquals(2444, playerRatingAsOfDate, "wrong rating as of " + calendar.getTime());

        // get latest rating as before the first tournament
        calendar.set(2015, Calendar.DECEMBER, 12, 0, 0 ,0);
        playerRatingAsOfDate = usattDataService.getPlayerRatingAsOfDate(membershipId,  calendar.getTime());
        assertEquals(2409, playerRatingAsOfDate, "wrong rating as of " + calendar.getTime());

        // get latest rating as before the first tournament
        calendar.set(2021, Calendar.FEBRUARY, 26, 0, 0 ,0);
        playerRatingAsOfDate = usattDataService.getPlayerRatingAsOfDate(400000,  calendar.getTime());
        assertEquals(0, playerRatingAsOfDate, "wrong rating as of " + calendar.getTime());
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

    @Test
    @Disabled
    public void testReadingMembership () {
        String filename = "F:\\ratings files\\currentMembersReport.csv";
        MembershipsProcessorStatus membershipsProcessorStatus = new MembershipsProcessorStatus();
        List<UsattPlayerRecord> usattPlayerInfos = usattDataService.readMembershipFile(filename, membershipsProcessorStatus);
        assertTrue((usattPlayerInfos.size() > 13000), "wrong number of records");
        for (UsattPlayerRecord usattPlayerInfo : usattPlayerInfos) {
            assertNotNull(usattPlayerInfo.getFirstName(), "first name is null");
            assertNotNull(usattPlayerInfo.getLastName(), "last name is null");
            assertNotNull(usattPlayerInfo.getGender(), "Gender is null");
        }

        long countBefore = usattDataService.getTotalCount();
        usattDataService.insertNewMembers(usattPlayerInfos, membershipsProcessorStatus);
        long countAfter = usattDataService.getTotalCount();
        long addedRecords = countAfter - countBefore;
        System.out.println("addedRecords = " + addedRecords);
//        assertEquals("wrong count of records inserted into db", usattPlayerInfos.size(), addedRecords);
    }

}
