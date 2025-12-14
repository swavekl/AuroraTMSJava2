package com.auroratms.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class FeeScheduleItem implements Serializable {
    // description of price offer - e.g. early bird special
    private String offerName;
    // deadline when this offer expires
    private Date deadline;
    // entry fee before this deadline
    private int entryFee;
    // cancellation fee
    private int cancellationFee;

    public FeeScheduleItem(FeeScheduleItem feeScheduleItem) {
        this.offerName = feeScheduleItem.offerName;
        this.deadline = feeScheduleItem.deadline;
        this.entryFee = feeScheduleItem.entryFee;
        this.cancellationFee = feeScheduleItem.cancellationFee;
    }
}
