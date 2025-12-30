package com.auroratms.tournamentevententry.pricecalc;

import com.auroratms.event.TournamentEvent;

import java.util.Date;

public class PriceCalculatorFactory {

    public static IPriceCalculator makeCalculator (TournamentEvent tournamentEvent, Date tournamentStartDate) {
        IPriceCalculator priceCalculator = switch (tournamentEvent.getFeeStructure()) {
            case FIXED -> new FixedPriceCalculator(tournamentEvent, tournamentStartDate);
            case PER_SCHEDULE -> new FeeSchedulePriceCalculator(tournamentEvent);
            default -> null;
        };
        return priceCalculator;
    }
}
