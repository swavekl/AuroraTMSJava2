package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.AgeRestrictionType;
import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.AvailabilityStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

/**
 * Policy for determining if player is allowed to play according to age
 *
 * Policy can be:
 * Examples from US Open which took place on December 12-17, 2016.
 * 1) on the day of event the player must be younger than age limit
 * 2) born on or before specific date e.g.
 * For "9 and Under" event  must be born on or after January 1, 2007;
 *     "11 and Under" event must be born on or after January 1, 2005
 * 3) Players—Must be Over 30, Over 40, Over 50, Over 60, Over 65, Over 70, Over 75, and
 *    Over 80 as of December 31, 2016.
 */
public class AgeRestrictionEventPolicy implements IEventPolicy {
    private final Date tournamentStartDate;
    private final Date dateOfBirth;
    private AvailabilityStatus availabilityStatus;

    public AgeRestrictionEventPolicy(Date tournamentStartDate, Date dateOfBirth) {
        this.tournamentStartDate = new Date(tournamentStartDate.getTime());
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public boolean isEntryDenied(TournamentEvent event) {
        boolean isDenied = false;
        if (AgeRestrictionType.AGE_UNDER_OR_EQUAL_ON_DAY_EVENT.equals(event.getAgeRestrictionType()) &&
                event.getMaxPlayerAge() > 0) {
            // for youth (14 and Under, 18 and Under) events player must be under the age on the day of the event
            LocalDate lcBirthDate = convertToLocalDate(dateOfBirth);
            LocalDate lcOtherDate = convertToLocalDate(tournamentStartDate);
            int ageOnDayOfEvent = calculateAge(lcBirthDate, lcOtherDate);
            isDenied = (ageOnDayOfEvent > event.getMaxPlayerAge());
        } else if (AgeRestrictionType.AGE_OVER_AT_THE_END_OF_YEAR.equals(event.getAgeRestrictionType()) &&
                event.getMinPlayerAge() > 0) {
            // for 40 and Over, 50 and Over events age must be at or above that value at the end of the
            // calendar year in which the tournament takes place
            // find the date at the end of the calendar year of the tournament
            LocalDate lcBirthDate = convertToLocalDate(dateOfBirth);
            LocalDate lcTournamentStartDate = convertToLocalDate(this.tournamentStartDate);
            lcTournamentStartDate = lcTournamentStartDate.withMonth(12);
            lcTournamentStartDate = lcTournamentStartDate.withDayOfMonth(31);
            int ageAtTheEndOfYear = calculateAge(lcBirthDate, lcTournamentStartDate);
            isDenied = (ageAtTheEndOfYear < event.getMinPlayerAge());
        } else if (AgeRestrictionType.BORN_ON_OR_AFTER_DATE.equals(event.getAgeRestrictionType())) {
            //  "9 and Under" event  must be born on or after January 1, 2007
            Date ageRestrictionDate = event.getAgeRestrictionDate();
            if (ageRestrictionDate != null) {
                isDenied = dateOfBirth.before(ageRestrictionDate);
            }
        }

        availabilityStatus = (isDenied) ? AvailabilityStatus.DISQUALIFIED_BY_AGE : AvailabilityStatus.AVAILABLE_FOR_ENTRY;

        return isDenied;
    }

    private int calculateAge(LocalDate lcBirthDate, LocalDate lcOtherDate) {
        return Period.between(lcBirthDate, lcOtherDate).getYears();
    }

    private LocalDate convertToLocalDate(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    @Override
    public AvailabilityStatus getStatus() {
        return availabilityStatus;
    }
}
