package com.auroratms.tournamentevententry.pricecalc;

import com.auroratms.event.FeeStructure;
import com.auroratms.event.TournamentEvent;

import java.util.Date;

public class PriceCalculatorFactory {

    public static IPriceCalculator makeCalculator(TournamentEvent tournamentEvent, Date tournamentStartDate) {
        IPriceCalculator defaultCalculator = new FixedPriceCalculator(tournamentEvent, tournamentStartDate);
        if (tournamentEvent != null) {
            FeeStructure feeStructure = tournamentEvent.getFeeStructure();
            if (feeStructure != null) {
                return switch (feeStructure) {
                    case FIXED -> new FixedPriceCalculator(tournamentEvent, tournamentStartDate);
                    case PER_SCHEDULE -> new FeeSchedulePriceCalculator(tournamentEvent);
                    default -> defaultCalculator;
                };
            }
        }
        return defaultCalculator;
    }
}
