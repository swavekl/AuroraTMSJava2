package com.auroratms.paymentrefund;


import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RefundCalculatorTest {

    @Test
    public void refundEntireOnePayment() {
        int amountToRefund = 10000;
        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment = makePaymentObject(10000, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 86);
        payments.add(payment);
        RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund);
        List<PaymentRefund> refunds = refundCalculator.determineRefunds();
        assertEquals("wrong number of refunds", 1, refunds.size());

        PaymentRefund refund = refunds.get(0);
        assertEquals("wrong refund amount", 10000, refund.getAmount());
        assertEquals("wrong payment intent id", payment.getPaymentIntentId(), refund.getPaymentIntentId());
        assertEquals("wrong status", PaymentRefundStatus.REFUND_COMPLETED, refund.getStatus());
    }

    @Test
    public void refundPartiallyOnePayment() {
        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment = makePaymentObject(10000, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 86);
        payments.add(payment);

        int amountToRefund = 6000;
        RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund);
        List<PaymentRefund> refunds = refundCalculator.determineRefunds();
        assertEquals("wrong number of refunds", 1, refunds.size());

        PaymentRefund refund = refunds.get(0);
        assertEquals("wrong refund amount", amountToRefund, refund.getAmount());
        assertEquals("wrong payment intent id", payment.getPaymentIntentId(), refund.getPaymentIntentId());
        assertEquals("wrong status", PaymentRefundStatus.REFUND_COMPLETED, refund.getStatus());
        assertEquals("wrong item id", payment.getItemId(), refund.getItemId());
    }

    @Test
    public void refundPartiallyOneTwicePayment() {
        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment = makePaymentObject(10000, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 86);
        payments.add(payment);

        {
            int amountToRefund = 6000;
            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals("wrong number of refunds", 1, refunds.size());

            PaymentRefund refund = refunds.get(0);
            assertEquals("wrong refund amount", amountToRefund, refund.getAmount());
            assertEquals("wrong payment intent id", payment.getPaymentIntentId(), refund.getPaymentIntentId());
            assertEquals("wrong status", PaymentRefundStatus.REFUND_COMPLETED, refund.getStatus());
            assertEquals("wrong item id", payment.getItemId(), refund.getItemId());

            // 'persist' refund
            payments.addAll(refunds);
        }

        {
            int amountToRefund = 4000;
            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals("wrong number of refunds", 1, refunds.size());

            PaymentRefund refund = refunds.get(0);
            assertEquals("wrong refund amount", amountToRefund, refund.getAmount());
            assertEquals("wrong payment intent id", payment.getPaymentIntentId(), refund.getPaymentIntentId());
            assertEquals("wrong status", PaymentRefundStatus.REFUND_COMPLETED, refund.getStatus());
            assertEquals("wrong item id", payment.getItemId(), refund.getItemId());
        }
    }

    @Test
    public void refundFullyTwoPayments() {
        // two payments totalling $250
        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment1 = makePaymentObject(15000, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 86);
        PaymentRefund payment2 = makePaymentObject(10000, "pi_2XYjHuLnh8tr5SoHlBAu6a12", 86);
        payments.add(payment1);
        payments.add(payment2);

        {
            int amountToRefund = 25000;
            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals("wrong number of refunds", 2, refunds.size());

            PaymentRefund refund1 = refunds.get(0);
            assertEquals("wrong payment intent id", payment1.getPaymentIntentId(), refund1.getPaymentIntentId());
            assertEquals("wrong refund amount", 15000, refund1.getAmount());
            assertEquals("wrong status", PaymentRefundStatus.REFUND_COMPLETED, refund1.getStatus());
            assertEquals("wrong item id", payment1.getItemId(), refund1.getItemId());

            PaymentRefund refund2 = refunds.get(1);
            assertEquals("wrong payment intent id", payment2.getPaymentIntentId(), refund2.getPaymentIntentId());
            assertEquals("wrong refund amount", 10000, refund2.getAmount());
            assertEquals("wrong status", PaymentRefundStatus.REFUND_COMPLETED, refund2.getStatus());
            assertEquals("wrong item id", payment2.getItemId(), refund2.getItemId());
        }
    }

    @Test
    public void refundPartOfTwoPayments() {
        // two payments totalling $250
        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment1 = makePaymentObject(15000, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 86);
        PaymentRefund payment2 = makePaymentObject(10000, "pi_2XYjHuLnh8tr5SoHlBAu6a12", 86);
        payments.add(payment1);
        payments.add(payment2);

        {
            int amountToRefund = 8000;
            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals("wrong number of refunds", 1, refunds.size());

            PaymentRefund refund1 = refunds.get(0);
            assertEquals("wrong payment intent id", payment1.getPaymentIntentId(), refund1.getPaymentIntentId());
            assertEquals("wrong refund amount", 8000, refund1.getAmount());
            assertEquals("wrong status", PaymentRefundStatus.REFUND_COMPLETED, refund1.getStatus());
            assertEquals("wrong item id", payment1.getItemId(), refund1.getItemId());
        }
    }

    @Test
    public void refundFullyTwoPaymentsInTwoSteps() {
        // two payments totalling $250
        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment1 = makePaymentObject(15000, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 86);
        PaymentRefund payment2 = makePaymentObject(10000, "pi_2XYjHuLnh8tr5SoHlBAu6a12", 86);
        payments.add(payment1);
        payments.add(payment2);

        {
            int amountToRefund = 8000;
            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals("wrong number of refunds", 1, refunds.size());

            PaymentRefund refund1 = refunds.get(0);
            assertEquals("wrong payment intent id", payment1.getPaymentIntentId(), refund1.getPaymentIntentId());
            assertEquals("wrong refund amount", 8000, refund1.getAmount());
            assertEquals("wrong status", PaymentRefundStatus.REFUND_COMPLETED, refund1.getStatus());
            assertEquals("wrong item id", payment1.getItemId(), refund1.getItemId());

            // persist refund
            payments.addAll(refunds);
        }

        // refund remainder
        {
            int amountToRefund = 17000;
            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals("wrong number of refunds", 2, refunds.size());

            PaymentRefund refund1 = refunds.get(0);
            assertEquals("wrong payment intent id", payment1.getPaymentIntentId(), refund1.getPaymentIntentId());
            assertEquals("wrong refund amount", 7000, refund1.getAmount());
            assertEquals("wrong status", PaymentRefundStatus.REFUND_COMPLETED, refund1.getStatus());
            assertEquals("wrong item id", payment1.getItemId(), refund1.getItemId());

            PaymentRefund refund2 = refunds.get(1);
            assertEquals("wrong payment intent id", payment2.getPaymentIntentId(), refund2.getPaymentIntentId());
            assertEquals("wrong refund amount", 10000, refund2.getAmount());
            assertEquals("wrong status", PaymentRefundStatus.REFUND_COMPLETED, refund2.getStatus());
            assertEquals("wrong item id", payment2.getItemId(), refund2.getItemId());
        }
    }


    private PaymentRefund makePaymentObject(int amount, String paymentId, long itemId) {
        PaymentRefund payment = new PaymentRefund();
        payment.setPaymentRefundFor(PaymentRefundFor.TOURNAMENT_ENTRY);
        payment.setId(1);
        payment.setItemId(itemId);
        payment.setStatus(PaymentRefundStatus.PAYMENT_COMPLETED);
        payment.setTransactionDate(new Date());
        payment.setAmount(amount);
        payment.setPaymentIntentId(paymentId);
        return payment;
    }
}
