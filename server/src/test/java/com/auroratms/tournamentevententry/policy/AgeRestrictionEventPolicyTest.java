package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.AgeRestrictionType;
import com.auroratms.event.TournamentEvent;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AgeRestrictionEventPolicyTest {

    // ========================================================================
    // Junior event policies
    // ========================================================================

    @Test
    public void testJuniorEvent() {
        TournamentEvent ageRestrictedEvent = new TournamentEvent();
        ageRestrictedEvent.setAgeRestrictionType(AgeRestrictionType.AGE_UNDER_OR_EQUAL_ON_DAY_EVENT);
        ageRestrictedEvent.setMaxPlayerAge(12);

        Date tournamentStartDate = getDate(2022, Calendar.JANUARY, 14);

        // young player
        Date dateofBirth = getDate(2010, Calendar.FEBRUARY, 24);
        AgeRestrictionEventPolicy policy = new AgeRestrictionEventPolicy(tournamentStartDate, dateofBirth);
        boolean entryDenied = policy.isEntryDenied(ageRestrictedEvent);
        assertFalse("player should qualify", entryDenied);
    }

    @Test
    public void testJuniorEventOnBirthday() {
        TournamentEvent ageRestrictedEvent = new TournamentEvent();
        ageRestrictedEvent.setAgeRestrictionType(AgeRestrictionType.AGE_UNDER_OR_EQUAL_ON_DAY_EVENT);
        ageRestrictedEvent.setMaxPlayerAge(12);

        Date tournamentStartDate = getDate(2022, Calendar.JANUARY, 14);

        // young player
        Date dateofBirth = getDate(2009, Calendar.JANUARY, 14);
        AgeRestrictionEventPolicy policy = new AgeRestrictionEventPolicy(tournamentStartDate, dateofBirth);
        boolean entryDenied = policy.isEntryDenied(ageRestrictedEvent);
        assertTrue("player should not qualify", entryDenied);
    }

    @Test
    public void testJuniorEventBeforeBirthday() {
        TournamentEvent ageRestrictedEvent = new TournamentEvent();
        ageRestrictedEvent.setAgeRestrictionType(AgeRestrictionType.AGE_UNDER_OR_EQUAL_ON_DAY_EVENT);
        ageRestrictedEvent.setMaxPlayerAge(12);

        Date tournamentStartDate = getDate(2022, Calendar.JANUARY, 14);

        // young player
        Date dateofBirth = getDate(2009, Calendar.JANUARY, 15);
        AgeRestrictionEventPolicy policy = new AgeRestrictionEventPolicy(tournamentStartDate, dateofBirth);
        boolean entryDenied = policy.isEntryDenied(ageRestrictedEvent);
        assertFalse("player should qualify", entryDenied);
    }

    @Test
    public void testJuniorEventTooOld() {
        TournamentEvent ageRestrictedEvent = new TournamentEvent();
        ageRestrictedEvent.setAgeRestrictionType(AgeRestrictionType.AGE_UNDER_OR_EQUAL_ON_DAY_EVENT);
        ageRestrictedEvent.setMaxPlayerAge(12);

        Date tournamentStartDate = getDate(2022, Calendar.JANUARY, 14);

        // older player
        Date dateofBirth = getDate(2008, Calendar.MARCH, 10);
        AgeRestrictionEventPolicy policy = new AgeRestrictionEventPolicy(tournamentStartDate, dateofBirth);
        boolean entryDenied = policy.isEntryDenied(ageRestrictedEvent);
        assertTrue("player should not qualify", entryDenied);
    }

    //====================================================================
    // Junior with specific date of birth limit policies -
    //
    //====================================================================
    @Test
    public void testBirthDateBeforeSpecificDateAfter() {
        TournamentEvent ageRestrictedEvent = new TournamentEvent();
        ageRestrictedEvent.setAgeRestrictionType(AgeRestrictionType.BORN_ON_OR_AFTER_DATE);
        // "9 and Under" event  must be born on or after January 1, 2007
        Date ageRestrictionDate = getDate(2007, Calendar.JANUARY, 1);
        ageRestrictedEvent.setAgeRestrictionDate(ageRestrictionDate);

        // US Open December 12-17, 2016
        Date tournamentStartDate = getDate(2016, Calendar.DECEMBER, 12);

        // too old
        Date dateofBirth = getDate(2008, Calendar.MARCH, 10);
        AgeRestrictionEventPolicy policy = new AgeRestrictionEventPolicy(tournamentStartDate, dateofBirth);
        boolean entryDenied = policy.isEntryDenied(ageRestrictedEvent);
        assertFalse("player should qualify", entryDenied);
    }

    @Test
    public void testBirthDateBeforeSpecificDateBefore() {
        TournamentEvent ageRestrictedEvent = new TournamentEvent();
        ageRestrictedEvent.setAgeRestrictionType(AgeRestrictionType.BORN_ON_OR_AFTER_DATE);
        // "9 and Under" event  must be born on or after January 1, 2007
        Date ageRestrictionDate = getDate(2007, Calendar.JANUARY, 1);
        ageRestrictedEvent.setAgeRestrictionDate(ageRestrictionDate);

        // US Open December 12-17, 2016
        Date tournamentStartDate = getDate(2016, Calendar.DECEMBER, 12);

        // too old
        Date dateofBirth = getDate(2006, Calendar.DECEMBER, 10);
        AgeRestrictionEventPolicy policy = new AgeRestrictionEventPolicy(tournamentStartDate, dateofBirth);
        boolean entryDenied = policy.isEntryDenied(ageRestrictedEvent);
        assertTrue("player should no qualify", entryDenied);
    }

    @Test
    public void testBirthDateBeforeSpecificDateOnBirthday() {
        TournamentEvent ageRestrictedEvent = new TournamentEvent();
        ageRestrictedEvent.setAgeRestrictionType(AgeRestrictionType.BORN_ON_OR_AFTER_DATE);
        // "9 and Under" event  must be born on or after January 1, 2007
        Date ageRestrictionDate = getDate(2007, Calendar.JANUARY, 1);
        ageRestrictedEvent.setAgeRestrictionDate(ageRestrictionDate);

        // US Open December 12-17, 2016
        Date tournamentStartDate = getDate(2016, Calendar.DECEMBER, 12);

        // too old
        Date dateofBirth = getDate(2007, Calendar.JANUARY, 1);
        AgeRestrictionEventPolicy policy = new AgeRestrictionEventPolicy(tournamentStartDate, dateofBirth);
        boolean entryDenied = policy.isEntryDenied(ageRestrictedEvent);
        assertFalse("player should qualify", entryDenied);
    }

    //====================================================================
    // Senior (40 and Over) policies
    //====================================================================
    @Test
    public void testSeniorEvent() {
        TournamentEvent ageRestrictedEvent = new TournamentEvent();
        ageRestrictedEvent.setAgeRestrictionType(AgeRestrictionType.AGE_OVER_AT_THE_END_OF_YEAR);
        ageRestrictedEvent.setMinPlayerAge(40);

        Date tournamentStartDate = getDate(2022, Calendar.JANUARY, 14);

        // 40 plus year old
        Date dateofBirth = getDate(1980, Calendar.JANUARY, 10);
        AgeRestrictionEventPolicy policy = new AgeRestrictionEventPolicy(tournamentStartDate, dateofBirth);
        boolean entryDenied = policy.isEntryDenied(ageRestrictedEvent);
        assertFalse("player should qualify", entryDenied);
    }

    @Test
    public void testSeniorEventTooYoung() {
        TournamentEvent ageRestrictedEvent = new TournamentEvent();
        ageRestrictedEvent.setAgeRestrictionType(AgeRestrictionType.AGE_OVER_AT_THE_END_OF_YEAR);
        ageRestrictedEvent.setMinPlayerAge(40);

        Date tournamentStartDate = getDate(2022, Calendar.JANUARY, 14);

        // 30+ year old
        Date dateofBirth = getDate(1985, Calendar.JANUARY, 10);
        AgeRestrictionEventPolicy policy = new AgeRestrictionEventPolicy(tournamentStartDate, dateofBirth);
        boolean entryDenied = policy.isEntryDenied(ageRestrictedEvent);
        assertTrue("player should not qualify", entryDenied);
    }

    @Test
    public void testSeniorEventTooYoungBornEndOfYear() {
        TournamentEvent ageRestrictedEvent = new TournamentEvent();
        ageRestrictedEvent.setAgeRestrictionType(AgeRestrictionType.AGE_OVER_AT_THE_END_OF_YEAR);
        ageRestrictedEvent.setMinPlayerAge(40);

        Date tournamentStartDate = getDate(2022, Calendar.JANUARY, 14);

        // 30+ year old
        Date dateofBirth = getDate(1982, Calendar.DECEMBER, 31);
        AgeRestrictionEventPolicy policy = new AgeRestrictionEventPolicy(tournamentStartDate, dateofBirth);
        boolean entryDenied = policy.isEntryDenied(ageRestrictedEvent);
        assertFalse("player should qualify", entryDenied);
    }

    private Date getDate(int year, int month, int day) {
        Calendar bdCal = Calendar.getInstance();
        bdCal.set(year, month, day, 0, 0, 0);
        return bdCal.getTime();
    }

}
