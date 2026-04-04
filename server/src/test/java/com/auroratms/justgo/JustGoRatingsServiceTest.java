package com.auroratms.justgo;

import com.auroratms.AbstractServiceTest;
import com.auroratms.usatt.UsattPlayerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class JustGoRatingsServiceTest extends AbstractServiceTest {

    @Autowired
    private JustGoRatingsService service;

    @Test
    public void getTournamentRatingByLastFirstName1() {
        Integer rating = service.getTournamentRatingByFullName("Swavek", "Lorenc");
        assertTrue(rating >= 1700, "Expected non-negative tournament rating from JustGo sandbox");
        assertEquals(Integer.class, rating.getClass()); // expected from data.finalRating
    }

    @Test
    public void getTournamentRatingByLastFirstName2() {
        Integer rating = service.getTournamentRatingByFullName("Mario", "Lorenc");
        assertTrue(rating >= 1900, "Expected non-negative tournament rating from JustGo sandbox");
        assertEquals(Integer.class, rating.getClass()); // expected from data.finalRating
    }

    @Test
    public void getTournamentRatingByLastFirstNameAsOfDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.OCTOBER, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date asOfDate = calendar.getTime();
        Integer eligibilityRating = service.getTournamentRatingByFullNameAsOfDate("Swavek", "Lorenc", asOfDate);
        assertEquals( 1879, eligibilityRating, "Expected exact rating");

        calendar.set(Calendar.DATE, 28);
        asOfDate = calendar.getTime();
        Integer postTournamentRating = service.getTournamentRatingByFullNameAsOfDate("Swavek", "Lorenc", asOfDate);
        assertEquals( 1750, postTournamentRating, "Expected exact rating");
    }

    @Test
    public void getPlayerRecordByFullName() {
        UsattPlayerRecord playerRecordByName = service.findPlayerRecordByName("Swavek", "Lorenc");
        assertNotNull( playerRecordByName, "Expected player record");

        assertEquals("Swavek", playerRecordByName.getFirstName(), "Expected first name");
        assertEquals("Lorenc", playerRecordByName.getLastName(), "Expected last name");
        assertEquals("Aurora", playerRecordByName.getCity(), "Expected city");
    }

    @Test
    public void getPlayerRecordByMembershipNumber() {
        UsattPlayerRecord playerRecordByName = service.findPlayerRecordByMembershipId(84639L);
        assertNotNull( playerRecordByName, "Expected player record");

        assertEquals("Swavek", playerRecordByName.getFirstName(), "Expected first name");
        assertEquals("Lorenc", playerRecordByName.getLastName(), "Expected last name");
        assertEquals("Aurora", playerRecordByName.getCity(), "Expected city");
    }

}
