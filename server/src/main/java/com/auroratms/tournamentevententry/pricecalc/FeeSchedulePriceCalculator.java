package com.auroratms.tournamentevententry.pricecalc;

import com.auroratms.event.FeeScheduleItem;
import com.auroratms.event.TournamentEvent;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FeeSchedulePriceCalculator implements IPriceCalculator {

    private TournamentEvent tournamentEvent;

    public FeeSchedulePriceCalculator(TournamentEvent tournamentEvent) {
        this.tournamentEvent = tournamentEvent;
    }

    @Override
    public double calculatePrice(Date dateOfEntry, Date birthDate) {
        double price = 0;
        List<FeeScheduleItem> feeScheduleItems = tournamentEvent.getConfiguration().getFeeScheduleItems();
        if (feeScheduleItems != null) {
            feeScheduleItems.sort(Comparator.comparing(FeeScheduleItem::getDeadline));
            for (FeeScheduleItem feeScheduleItem : feeScheduleItems) {
                Date deadline = feeScheduleItem.getDeadline();
                if (!dateOfEntry.after(deadline)) {
                    price = feeScheduleItem.getEntryFee();
                    break;
                }
            }

            if (price == 0) {
                FeeScheduleItem lastFeeScheduleItem = feeScheduleItems.get(feeScheduleItems.size() - 1);
                price = lastFeeScheduleItem.getEntryFee();
            }
        }

        return price;
    }
}
