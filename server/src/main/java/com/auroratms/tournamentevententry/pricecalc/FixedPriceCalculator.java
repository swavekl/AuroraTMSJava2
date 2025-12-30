package com.auroratms.tournamentevententry.pricecalc;

import com.auroratms.event.EventEntryType;
import com.auroratms.event.TournamentEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public class FixedPriceCalculator implements IPriceCalculator {
    private TournamentEvent tournamentEvent;
    private Date tournamentStartDate;

    public FixedPriceCalculator(TournamentEvent tournamentEvent, Date tournamentStartDate) {
        this.tournamentEvent = tournamentEvent;
        this.tournamentStartDate = tournamentStartDate;
    }

    @Override
    public double calculatePrice(Date dateOfEntry, Date birthDate) {
        LocalDate lcBirthDate = convertToLocalDate(birthDate);
        LocalDate lcOtherDate = convertToLocalDate(tournamentStartDate);
        int ageOnDayOfEvent = calculateAge(lcBirthDate, lcOtherDate);
        double price = tournamentEvent.getFeeAdult();
        if (tournamentEvent.getEventEntryType() == EventEntryType.INDIVIDUAL) {
            price = (ageOnDayOfEvent >= 18) ? tournamentEvent.getFeeAdult() : tournamentEvent.getFeeJunior();
        } else if (tournamentEvent.getEventEntryType() == EventEntryType.TEAM) {
            price = tournamentEvent.getPerTeamFee();
        }
        return price;
    }

    private int calculateAge(LocalDate lcBirthDate, LocalDate lcOtherDate) {
        return Period.between(lcBirthDate, lcOtherDate).getYears();
    }

    private LocalDate convertToLocalDate(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
