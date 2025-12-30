package com.auroratms.tournamentevententry.pricecalc;

import com.auroratms.event.FeeScheduleItem;
import com.auroratms.event.FeeStructure;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventConfiguration;
import com.auroratms.tournament.TournamentConfiguration;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FeeSchedulePriceCalculatorTest {

    @Test
    public void testBeforeEarliestDate() {
        List<FeeScheduleItem> items = new ArrayList<>(4);
        items.add(makeFeeScheduleItem("At the tournament", "12/31/2025", 849, 350));
        items.add(makeFeeScheduleItem("Early bird", "03/31/2026", 949, 350));
        items.add(makeFeeScheduleItem("Regular Entry", "07/15/2026", 1049, 350));
        items.add(makeFeeScheduleItem("Late Entry", "08/20/2026", 1149, 1149));
        TournamentEventConfiguration configuration = new TournamentEventConfiguration();
        configuration.setFeeScheduleItems(items);
        TournamentEvent tournamentEvent = new TournamentEvent();
        tournamentEvent.setConfiguration(configuration);
        tournamentEvent.setFeeStructure(FeeStructure.PER_SCHEDULE);

        try {
            FeeSchedulePriceCalculator calculator = new FeeSchedulePriceCalculator(tournamentEvent);
            Date birthDate = convertDate("10/20/2012");
            // tournament special
            testPrice("12/20/2025", calculator, birthDate, 849.0d);
            testPrice("12/31/2025", calculator, birthDate, 849.0d);
            // early bird
            testPrice("01/01/2026", calculator, birthDate, 949.0d);
            testPrice("02/15/2026", calculator, birthDate, 949.0d);
            testPrice("03/31/2026", calculator, birthDate, 949.0d);
            // regular
            testPrice("04/15/2026", calculator, birthDate, 1049.0d);
            testPrice("07/15/2026", calculator, birthDate, 1049.0d);
            // late
            testPrice("07/16/2026", calculator, birthDate, 1149.0d);
            testPrice("08/10/2026", calculator, birthDate, 1149.0d);
            testPrice("08/20/2026", calculator, birthDate, 1149.0d);
            testPrice("09/05/2026", calculator, birthDate, 1149.0d);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void testPrice(String strDate, FeeSchedulePriceCalculator calculator, Date birthDate, double expectedPrice) throws ParseException {
        Date entryDate = convertDate(strDate);
        double price = calculator.calculatePrice(entryDate, birthDate);
        assertEquals(expectedPrice, price, "wrong price for date " + strDate);
    }

    private FeeScheduleItem makeFeeScheduleItem(String offer, String strDeadline, int entryFee, int cancellationFee) {
        try {
            Date deadline = convertDate(strDeadline);
            FeeScheduleItem feeScheduleItem = new FeeScheduleItem();
            feeScheduleItem.setOfferName(offer);
            feeScheduleItem.setDeadline(deadline);
            feeScheduleItem.setEntryFee(entryFee);
            feeScheduleItem.setCancellationFee(cancellationFee);
            return feeScheduleItem;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private Date convertDate(String strDate) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        return dateFormat.parse(strDate);
    }
}
