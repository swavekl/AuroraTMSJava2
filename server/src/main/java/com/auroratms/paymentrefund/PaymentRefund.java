package com.auroratms.paymentrefund;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

/**
 * Class for recording payments and refunds in our database
 * This is needed to be able to get payment intent id in case user wants a refund
 */
@Entity
@Table(name = "paymentrefund")
@Data
@NoArgsConstructor
public class PaymentRefund {

    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // for payments this is Stripe original payment intent id
    // for refunds, then this is payment id from which this refund was made
    @NonNull
    private String paymentIntentId;

    // Stripe refund id if this is a refund
    private String refundId;

    // what this payment is for i.e. what is represented by itemId
    @NonNull
    private PaymentRefundFor paymentRefundFor;

    // id of a tournament entry, clinic entry or something for which we are paying/refunding
    private long itemId;

    // amount of transaction (payment or refund) - this is decimal expressed as a number $20.34 is 2034
    private int amount;

    // amount actually paid in currency of payment - may be different from tournament currency
    private int paidAmount;

    // currency code in which the paid amount is expressed
    private String paidCurrency;

    // date & time of payment and refund
    @NonNull
    private Date transactionDate;

    // status of the payment
    @NonNull
    private PaymentRefundStatus status = PaymentRefundStatus.PAYMENT_COMPLETED;

    // if true refund this payment fully
    private boolean refundFully;

    // what form of payment was used credit card, check or cash
    @Column(columnDefinition = "integer default 0")
    private PaymentForm paymentForm;
}

