package com.auroratms.paymentrefund;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 */
@Data
@NoArgsConstructor
public class PaymentRefundRequest {

    // what this payment is for
    private PaymentRefundFor paymentRefundFor;

    // id of a tournament, clinic or something for which we are paying/refunding
    long itemId;

    // this is decimal expressed as a number $20.34 is 2034
    int amount;

    // 22 chars long descriptor which will appear on the credit card statement. No special chars allowed " ' * < >
    String statementDescriptor;

    // name person paying for
    String fullName;

    // email address where to send receipt
    String receiptEmail;

    // should we do payment or refund
    boolean isRefund = false;
}
