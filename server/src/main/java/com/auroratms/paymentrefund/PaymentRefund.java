package com.auroratms.paymentrefund;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

/**
 * Class for recording payments and refunds in our database
 * This is needed to be able to get payment intent id in case of a refund
 */
@Entity
@Table(name = "paymentrefund")
@Data
@NoArgsConstructor
public class PaymentRefund {

    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    // Stripe payment intent id
    @NonNull
    String paymentIntentId;

    // what this payment is for i.e. what is represented by itemId
    @NonNull
    private PaymentRefundFor paymentRefundFor;

    // id of a tournament, clinic or something for which we are paying/refunding
    @NonNull
    long itemId;

    // this is decimal expressed as a number $20.34 is 2034
    @NonNull
    int amount;

    // date & time of payment and refund
    @NonNull
    Date transactionDate;

    // status of the payment
    @NonNull
    PaymentRefundStatus status = PaymentRefundStatus.PAYMENT_COMPLETED;
}

