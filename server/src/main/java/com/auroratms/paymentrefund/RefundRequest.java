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

    // the currency of refund
    private String currencyCode;

    // amount in account currency e.g. if refunding $10 for tournament in USA this will be 1000 cents
    private int amountInAccountCurrency;

    // exchange rate to use when calculating refund amount, 1.0 if no exchange
    private double exchangeRate;
}
