package com.auroratms.paymentrefund;

import lombok.Data;

import java.util.List;

/**
 * Info class combining payment information with name of paying person
 */
@Data
public class PaymentRefundInfo {

    // id of person who made these payments
    private String profileId;

    // full name of person
    private String fullName;

    // list of payments and refunds
    private List<PaymentRefund> paymentRefundList;
}
