package com.auroratms.justgo;

import com.auroratms.AbstractServiceTest;
import com.auroratms.usatt.UsattPlayerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

//    @Test
//    public void getTournamentRatingByLastFirstName2() {
//        Integer rating = service.getTournamentRatingByFullName("Mario", "Lorenc");
//        assertTrue(rating >= 1900, "Expected non-negative tournament rating from JustGo sandbox " + rating);
//        assertEquals(Integer.class, rating.getClass()); // expected from data.finalRating
//    }

    @Test
    public void getTournamentRatingByLastFirstName3() {
        Integer rating = service.getTournamentRatingByFullName("Miroslaw", "Lepa");
        assertTrue(rating >= 1600, "Expected non-negative tournament rating from JustGo sandbox " + rating);
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
        assertEquals(84639L, playerRecordByName.getMembershipId(), "Expected USATT membership id");
        assertEquals("718dfa8b-3604-4dcc-afe6-25d0e85cb6bd", playerRecordByName.getMemberGuid(), "Expected JustGo member guid");
    }

    @Test
    public void getPlayerRecordByMembershipNumber() {
        UsattPlayerRecord playerRecordByName = service.findPlayerRecordByMembershipId(84639L);
        assertNotNull( playerRecordByName, "Expected player record");

        assertEquals("Swavek", playerRecordByName.getFirstName(), "Expected first name");
        assertEquals("Lorenc", playerRecordByName.getLastName(), "Expected last name");
        assertEquals("Aurora", playerRecordByName.getCity(), "Expected city");
        assertEquals(84639L, playerRecordByName.getMembershipId(), "Expected USATT membership id");
        assertEquals("718dfa8b-3604-4dcc-afe6-25d0e85cb6bd", playerRecordByName.getMemberGuid(), "Expected JustGo member guid");
    }

    @Test
    public void getPlayerRecordByMembershipNumberAsOfDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2026, Calendar.JULY, 3);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE,11);
        calendar.set(Calendar.SECOND, 22);
        calendar.set(Calendar.MILLISECOND, 123);
        Date asOfDate = calendar.getTime();
        List<UsattPlayerRecord> changedPlayers = service.findChangedPlayers(asOfDate);
        assertEquals(6, changedPlayers.size(), "Expected 6 players");
        int nonZeroRating = 0;
        for (UsattPlayerRecord p : changedPlayers) {
            assertNotNull( p.getFirstName(), "Expected first name");
            assertNotNull( p.getLastName(), "Expected last name");
            assertNotNull( p.getMemberGuid(), "Expected JustGo uuid for member");
            assertNotNull( p.getMembershipId(), "Expected USATT membership id for member");
            if (p.getTournamentRating() != 0) {
                nonZeroRating++;
            }
        }
        assertEquals(nonZeroRating, 5, "Expected non-zero rating");
    }

}
