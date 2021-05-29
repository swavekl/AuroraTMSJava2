package com.auroratms.paymentrefund.notification.event;

import com.auroratms.paymentrefund.PaymentRefundFor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Event which initiates sending email after tournament payment is complete
 */
@Data
@NoArgsConstructor
public class PaymentEvent {

    // tournament, clinic or usatt fee
    private PaymentRefundFor paymentRefundFor;

    // id of a tournament, clinic or something for which we are paying/refunding
    private long itemId;

    // amount of transaction (payment or refund) - this is decimal expressed as a number $20.34 is 2034
    private int amount;

    // amount actually paid in currency of payment - may be different from tournament currency
    private int paidAmount;

    // currency code in which the paid amount is expressed
    private String paidCurrency;

    // date & time of payment and refund
    private Date transactionDate;
}

