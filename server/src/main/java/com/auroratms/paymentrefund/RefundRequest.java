package com.auroratms.paymentrefund;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data required to issue a refund
 */
@Data
@NoArgsConstructor
public class RefundRequest {
    // what this payment is for
    private PaymentRefundFor paymentRefundFor = PaymentRefundFor.TOURNAMENT_ENTRY;

    // item id which identifies the account to which the payment is to be made e.g. tournament
    private long accountItemId;

    // item id for which the payment is meant e.g. tournament entry
    private long transactionItemId;

    // this is decimal expressed as a number $20.34 is 2034
    private int amount;
}
