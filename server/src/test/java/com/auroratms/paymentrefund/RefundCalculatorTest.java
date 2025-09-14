package com.auroratms.paymentrefund;


import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RefundCalculatorTest {

    @Test
    public void refundEntireOnePayment() {
        int amountToRefund = 10000;
        List<PaymentRefund> payments = new ArrayList<>();
        Calendar calendar = new GregorianCalendar(2021, Calendar.MARCH, 27, 10, 45, 0);
        Date paymentDate = calendar.getTime();
        PaymentRefund payment = makePaymentObject(10000, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 86, paymentDate);
        payments.add(payment);
        RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund, 1.0);
        List<PaymentRefund> refunds = refundCalculator.determineRefunds();
        assertEquals(1, refunds.size(), "wrong number of refunds");

        PaymentRefund refund = refunds.get(0);
        assertEquals(10000, refund.getAmount(), "wrong refund amount");
        assertEquals(10000, refund.getPaidAmount(), "wrong refund paid amount");
        assertEquals(payment.getPaymentIntentId(), refund.getPaymentIntentId(), "wrong payment intent id");
        assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund.getStatus(), "wrong status");
        assertEquals(true, refund.isRefundFully(), "wrong full refund status");
    }

    @Test
    public void refundPartiallyOnePayment() {
        Calendar calendar = new GregorianCalendar(2021, Calendar.MARCH, 27, 10, 45, 0);
        Date paymentDate = calendar.getTime();
        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment = makePaymentObject(10000, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 86, paymentDate);
        payments.add(payment);

        int amountToRefund = 6000;
        RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund, 1.0);
        List<PaymentRefund> refunds = refundCalculator.determineRefunds();
        assertEquals(1, refunds.size(), "wrong number of refunds");

        PaymentRefund refund = refunds.get(0);
        assertEquals(amountToRefund, refund.getAmount(), "wrong refund amount");
        assertEquals(amountToRefund, refund.getPaidAmount(), "wrong refund amount");
        assertEquals(payment.getPaymentIntentId(), refund.getPaymentIntentId(), "wrong payment intent id");
        assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund.getStatus(), "wrong status");
        assertEquals(payment.getItemId(), refund.getItemId(), "wrong item id");
        assertFalse(refund.isRefundFully(), "wrong full refund status");
    }

    @Test
    public void refundPartiallyOneTwicePayment() {
        Calendar calendar = new GregorianCalendar(2021, Calendar.MARCH, 27, 10, 45, 0);
        Date paymentDate = calendar.getTime();
        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment = makePaymentObject(10000, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 86, paymentDate);
        payments.add(payment);

        {
            int amountToRefund = 6000;
            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund, 1.0);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals(1, refunds.size(), "wrong number of refunds");

            PaymentRefund refund = refunds.get(0);
            assertEquals(amountToRefund, refund.getAmount(), "wrong refund amount");
            assertEquals(amountToRefund, refund.getPaidAmount(), "wrong refund paid amount");
            assertEquals(payment.getPaymentIntentId(), refund.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund.getStatus(), "wrong status");
            assertEquals(payment.getItemId(), refund.getItemId(), "wrong item id");
            assertFalse(refund.isRefundFully(), "wrong full refund status");

            // 'persist' refund
            payments.addAll(refunds);
        }

        {
            int amountToRefund = 4000;
            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund, 1.0);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals(1, refunds.size(), "wrong number of refunds");

            PaymentRefund refund = refunds.get(0);
            assertEquals(amountToRefund, refund.getAmount(), "wrong refund amount");
            assertEquals(amountToRefund, refund.getPaidAmount(), "wrong refund paid amount");
            assertEquals(payment.getPaymentIntentId(), refund.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund.getStatus(), "wrong status");
            assertEquals(payment.getItemId(), refund.getItemId(), "wrong item id");
            assertTrue(refund.isRefundFully(), "wrong full refund status");
        }
    }

    @Test
    public void refundFullyTwoPayments() {
        Calendar calendar = new GregorianCalendar(2021, Calendar.MARCH, 27, 10, 45, 0);
        Date paymentDate = calendar.getTime();
        calendar.add(Calendar.DATE, 2);
        Date secondPaymentDate = calendar.getTime();

        // two payments totalling $250
        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment1 = makePaymentObject(15000, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 86, paymentDate);
        PaymentRefund payment2 = makePaymentObject(10000, "pi_2XYjHuLnh8tr5SoHlBAu6a12", 87, secondPaymentDate);
        payments.add(payment1);
        payments.add(payment2);

        {
            int amountToRefund = 25000;
            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund, 1.0);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals(2, refunds.size(), "wrong number of refunds");

            // refund most recent first
            PaymentRefund refund2 = refunds.get(0);
            assertEquals(payment2.getPaymentIntentId(), refund2.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(10000, refund2.getAmount(), "wrong refund amount");
            assertEquals(10000, refund2.getPaidAmount(), "wrong refund paid amount");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund2.getStatus(), "wrong status");
            assertEquals(payment2.getItemId(), refund2.getItemId(), "wrong item id");
            assertTrue(refund2.isRefundFully(), "wrong full refund status");

            PaymentRefund refund1 = refunds.get(1);
            assertEquals(payment1.getPaymentIntentId(), refund1.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(15000, refund1.getAmount(), "wrong refund amount");
            assertEquals(15000, refund1.getPaidAmount(), "wrong refund paid amount");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund1.getStatus(), "wrong status");
            assertEquals(payment1.getItemId(), refund1.getItemId(), "wrong item id");
            assertTrue(refund1.isRefundFully(), "wrong full refund status");
        }
    }

    @Test
    public void refundPartOfTwoPayments() {
        Calendar calendar = new GregorianCalendar(2021, Calendar.MARCH, 27, 10, 45, 0);
        Date paymentDate = calendar.getTime();
        calendar.add(Calendar.DATE, 2);
        Date secondPaymentDate = calendar.getTime();
        // two payments totalling $250
        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment1 = makePaymentObject(15000, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 86, paymentDate);
        PaymentRefund payment2 = makePaymentObject(10000, "pi_2XYjHuLnh8tr5SoHlBAu6a12", 87, secondPaymentDate);
        payments.add(payment1);
        payments.add(payment2);

        {
            int amountToRefund = 8000;
            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund, 1.0);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals(1, refunds.size(), "wrong number of refunds");

            PaymentRefund refund1 = refunds.get(0);
            assertEquals(payment2.getPaymentIntentId(), refund1.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(8000, refund1.getAmount(), "wrong refund amount");
            assertEquals(8000, refund1.getPaidAmount(), "wrong refund paid amount");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund1.getStatus(), "wrong status");
            assertEquals(payment2.getItemId(), refund1.getItemId(), "wrong item id");
            assertFalse(refund1.isRefundFully(), "wrong full refund status");

        }
    }

    @Test
    public void refundFullyTwoPaymentsInTwoSteps() {
        Calendar calendar = new GregorianCalendar(2021, Calendar.MARCH, 27, 10, 45, 0);
        Date paymentDate = calendar.getTime();
        calendar.add(Calendar.DATE, 2);
        Date secondPaymentDate = calendar.getTime();
        // two payments totalling $250
        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment1 = makePaymentObject(15000, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 86, paymentDate);
        PaymentRefund payment2 = makePaymentObject(10000, "pi_2XYjHuLnh8tr5SoHlBAu6a12", 87, secondPaymentDate);
        payments.add(payment1);
        payments.add(payment2);

        {
            int amountToRefund = 8000;
            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund, 1.0);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals(1, refunds.size(), "wrong number of refunds");

            PaymentRefund refund1 = refunds.get(0);
            assertEquals(payment2.getPaymentIntentId(), refund1.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(8000, refund1.getAmount(), "wrong refund amount");
            assertEquals(8000, refund1.getPaidAmount(), "wrong refund paid amount");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund1.getStatus(), "wrong status");
            assertEquals(payment2.getItemId(), refund1.getItemId(), "wrong item id");
            assertFalse(refund1.isRefundFully(), "wrong full refund status");

            // persist refund
            payments.addAll(refunds);
        }

        // refund remainder
        {
            int amountToRefund = 17000;
            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefund, 1.0);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals(2, refunds.size(), "wrong number of refunds");

            // full refund of more recent one
            PaymentRefund refund2 = refunds.get(0);
            assertEquals(payment2.getPaymentIntentId(), refund2.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(2000, refund2.getAmount(), "wrong refund amount");
            assertEquals(2000, refund2.getPaidAmount(), "wrong refund paid amount");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund2.getStatus(), "wrong status");
            assertEquals(payment2.getItemId(), refund2.getItemId(), "wrong item id");
            assertTrue(refund2.isRefundFully(), "wrong full refund status");

            // refund remainder for the earlier one
            PaymentRefund refund1 = refunds.get(1);
            assertEquals(payment1.getPaymentIntentId(), refund1.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(15000, refund1.getAmount(), "wrong refund amount");
            assertEquals(15000, refund1.getPaidAmount(), "wrong refund paid amount");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund1.getStatus(), "wrong status");
            assertEquals(payment1.getItemId(), refund1.getItemId(), "wrong item id");
            assertTrue(refund1.isRefundFully(), "wrong full refund status");
        }
    }


    private PaymentRefund makePaymentObject(int amount, String paymentId, long id, Date paymentDate) {
        PaymentRefund payment = new PaymentRefund();
        payment.setPaymentRefundFor(PaymentRefundFor.TOURNAMENT_ENTRY);
        payment.setId(id);
        payment.setItemId(86);
        payment.setStatus(PaymentRefundStatus.PAYMENT_COMPLETED);
        payment.setTransactionDate(paymentDate);
        payment.setAmount(amount);
        payment.setPaidAmount(amount);
        payment.setPaidCurrency("usd");
        payment.setPaymentIntentId(paymentId);
        return payment;
    }
    ///////////////////////////////////////////////////////////////
    // Foreign currency tests
    ///////////////////////////////////////////////////////////////

    @Test
    public void foreignRefundEntireOnePayment() {
        int amount = 10000;
        double paymentDateExchangeRate = 1.26;
        double refundDayExchangeRate = 1.26;
        int amountPaid = (int)(paymentDateExchangeRate * amount);
        int amountToRefundInAccountCurrency = amount;
        int amountToRefund = (int)(refundDayExchangeRate * amount);

        Calendar calendar = new GregorianCalendar(2021, Calendar.MARCH, 27, 10, 45, 0);
        Date paymentDate = calendar.getTime();

        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment = makeForeignCurrencyPaymentObject(amount, amountPaid,"pi_1IYjHuLnh8tr5SoHlBAu6a12", 86, paymentDate);
        payments.add(payment);
        RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefundInAccountCurrency, refundDayExchangeRate);
        List<PaymentRefund> refunds = refundCalculator.determineRefunds();
        assertEquals(1, refunds.size(), "wrong number of refunds");

        PaymentRefund refund = refunds.get(0);
        assertEquals(amountToRefundInAccountCurrency, refund.getAmount(), "wrong refund amount");
        assertEquals(amountToRefund, refund.getPaidAmount(), "wrong refund paid amount");
        assertEquals(payment.getPaymentIntentId(), refund.getPaymentIntentId(), "wrong payment intent id");
        assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund.getStatus(), "wrong status");
        assertTrue(refund.isRefundFully(), "wrong full refund status");
    }

    @Test
    public void foreignRefundEntireOnePaymentDifferentExchange() {
        int amount = 10000;
        double paymentDateExchangeRate = 1.26;
        double refundDayExchangeRate = 1.28;
        int amountPaid = (int)(paymentDateExchangeRate * amount);

        // refund equivalent of $70.00
        int amountToRefundInAccountCurrency = 7000;
        int amountToRefundToday = (int)(refundDayExchangeRate * amountToRefundInAccountCurrency);

        Calendar calendar = new GregorianCalendar(2021, Calendar.MARCH, 27, 10, 45, 0);
        Date paymentDate = calendar.getTime();

        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment = makeForeignCurrencyPaymentObject(amount, amountPaid, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 86, paymentDate);
        payments.add(payment);
        RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefundInAccountCurrency, refundDayExchangeRate);
        List<PaymentRefund> refunds = refundCalculator.determineRefunds();
        assertEquals(1, refunds.size(), "wrong number of refunds");

        PaymentRefund refund = refunds.get(0);
        assertEquals(amountToRefundInAccountCurrency, refund.getAmount(), "wrong refund amount");
        assertEquals(amountToRefundToday, refund.getPaidAmount(), "wrong refund amount");
        assertEquals(payment.getPaymentIntentId(), refund.getPaymentIntentId(), "wrong payment intent id");
        assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund.getStatus(), "wrong status");
        assertEquals(false, refund.isRefundFully(), "wrong full refund status");
    }

    @Test
    public void foreignRefundTwoPaymentsTwoPartialRefunds() {

        // make a total of $250 in 2 payments
        Calendar calendar = new GregorianCalendar(2021, Calendar.MARCH, 27, 10, 45, 0);
        Date paymentDate = calendar.getTime();
        double paymentDateExchangeRate = 1.26;
        int amount = 10000;
        int amountPaid = (int)(paymentDateExchangeRate * amount);
        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment = makeForeignCurrencyPaymentObject(amount, amountPaid, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 3, paymentDate);
        payments.add(payment);

        calendar.add(Calendar.DATE, 2);
        Date secondPaymentDate = calendar.getTime();
        double paymentDate2ExchangeRate = 1.27;
        int amount2 = 15000;
        int amountPaid2 = (int)(paymentDate2ExchangeRate * amount2);
        PaymentRefund payment2 = makeForeignCurrencyPaymentObject(amount2, amountPaid2, "pi_1IYjHuLnh8tr5SoHlBAu6aAB", 4, secondPaymentDate);
        payments.add(payment2);

        {
            // refund 1
            double refundDayExchangeRate = 1.28;
            // refund equivalent of $70.00 from the more recent transaction i.e. $150 one
            int amountToRefundInAccountCurrency = 7000;
            int amountToRefundToday = (int) (refundDayExchangeRate * amountToRefundInAccountCurrency);

            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefundInAccountCurrency, refundDayExchangeRate);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals(1, refunds.size(), "wrong number of refunds");

            PaymentRefund refund = refunds.get(0);
            assertEquals(amountToRefundInAccountCurrency, refund.getAmount(), "wrong refund amount");
            assertEquals(amountToRefundToday, refund.getPaidAmount(), "wrong refund amount");
            // refund more recent first
            assertEquals(payment2.getPaymentIntentId(), refund.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund.getStatus(), "wrong status");
            assertFalse(refund.isRefundFully(), "wrong full refund status");

            // persist these refunds
            payments.addAll(refunds);
        }

        {
            // refund 2 - remainder of the second refund
            double refundDayExchangeRate = 1.30;
            // refund equivalent of $80.00 remaining (150 - 70) = 80
            int amountToRefundInAccountCurrency = 8000;
            int amountToRefundToday = (int) (refundDayExchangeRate * amountToRefundInAccountCurrency);

            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefundInAccountCurrency, refundDayExchangeRate);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals(1, refunds.size(), "wrong number of refunds");

            PaymentRefund refund = refunds.get(0);
            assertEquals(amountToRefundInAccountCurrency, refund.getAmount(), "wrong refund amount");
            assertEquals(amountToRefundToday, refund.getPaidAmount(), "wrong refund paid amount");
            assertEquals(payment2.getPaymentIntentId(), refund.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund.getStatus(), "wrong status");
            assertTrue(refund.isRefundFully(), "wrong full refund status");
        }
    }

    @Test
    public void foreignRefundTwoPaymentsFullRefund() {

        // make a total of $250 in 2 payments
        Calendar calendar = new GregorianCalendar(2021, Calendar.MARCH, 27, 10, 45, 0);
        Date paymentDate = calendar.getTime();
        double paymentDateExchangeRate = 1.26;
        int amount1 = 10000;
        int amount1Paid = (int)(paymentDateExchangeRate * amount1);
        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment1 = makeForeignCurrencyPaymentObject(amount1, amount1Paid, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 3, paymentDate);
        payments.add(payment1);

        calendar.add(Calendar.DATE, 2);
        Date secondPaymentDate = calendar.getTime();
        double paymentDate2ExchangeRate = 1.27;
        int amount2 = 15000;
        int amount2Paid = (int)(paymentDate2ExchangeRate * amount2);
        PaymentRefund payment2 = makeForeignCurrencyPaymentObject(amount2, amount2Paid, "pi_1IYjHuLnh8tr5SoHlBAu6aAB", 4, secondPaymentDate);
        payments.add(payment2);

        {
            // refund fully both payments
            double refundDayExchangeRate = 1.30;
            int amountToRefundInAccountCurrency = 25000;
            int totalAmountToRefundToday = (int) (refundDayExchangeRate * amountToRefundInAccountCurrency);
            int amount1ToRefundToday = (int) (refundDayExchangeRate * amount1);
            int amount2ToRefundToday = (int) (refundDayExchangeRate * amount2);

            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefundInAccountCurrency, refundDayExchangeRate);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals(2, refunds.size(), "wrong number of refunds");

            // refund mor recent payment first
            PaymentRefund refund1 = refunds.get(0);
            assertEquals(amount2, refund1.getAmount(), "wrong refund amount");
            assertEquals(amount2ToRefundToday, refund1.getPaidAmount(), "wrong refund amount");
            // refund more recent first
            assertEquals(payment2.getPaymentIntentId(), refund1.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund1.getStatus(), "wrong status");
            assertTrue(refund1.isRefundFully(), "wrong full refund status");

            // the the older payment second
            PaymentRefund refund2 = refunds.get(1);
            assertEquals(amount1, refund2.getAmount(), "wrong refund amount");
            assertEquals(amount1ToRefundToday, refund2.getPaidAmount(), "wrong refund amount");
            // refund more recent first
            assertEquals(payment1.getPaymentIntentId(), refund2.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund2.getStatus(), "wrong status");
            assertTrue(refund2.isRefundFully(), "wrong full refund status");
        }
    }

    @Test
    public void foreignRefundTwoPaymentsFullRefundInTwoBatches() {

        // make a total of $250 in 2 payments
        Calendar calendar = new GregorianCalendar(2021, Calendar.MARCH, 27, 10, 45, 0);
        Date paymentDate = calendar.getTime();
        double paymentDateExchangeRate = 1.26;
        int amount1 = 10000;
        int amount1Paid = (int)(paymentDateExchangeRate * amount1);
        List<PaymentRefund> payments = new ArrayList<>();
        PaymentRefund payment1 = makeForeignCurrencyPaymentObject(amount1, amount1Paid, "pi_1IYjHuLnh8tr5SoHlBAu6a12", 3, paymentDate);
        payments.add(payment1);

        calendar.add(Calendar.DATE, 2);
        Date secondPaymentDate = calendar.getTime();
        double paymentDate2ExchangeRate = 1.27;
        int amount2 = 15000;
        int amount2Paid = (int)(paymentDate2ExchangeRate * amount2);
        PaymentRefund payment2 = makeForeignCurrencyPaymentObject(amount2, amount2Paid, "pi_1IYjHuLnh8tr5SoHlBAu6aAB", 4, secondPaymentDate);
        payments.add(payment2);

        // first refund
        {
            // refund fully last payment and portion of older payment i.e. $50
            double refundDayExchangeRate = 1.30;
            int amountToRefundInAccountCurrency = 20000;
            int totalAmountToRefundToday = (int) (refundDayExchangeRate * amountToRefundInAccountCurrency);
            int partOfAmount1 = amountToRefundInAccountCurrency - amount2;
            int amount1ToRefundToday = (int) (refundDayExchangeRate * partOfAmount1);
            int amount2ToRefundToday = (int) (refundDayExchangeRate * amount2);

            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefundInAccountCurrency, refundDayExchangeRate);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals(2, refunds.size(), "wrong number of refunds");

            // refund more recent payment first - fully
            PaymentRefund refund1 = refunds.get(0);
            assertEquals(amount2, refund1.getAmount(), "wrong refund amount");
            assertEquals(amount2ToRefundToday, refund1.getPaidAmount(), "wrong refund amount");
            // refund more recent first
            assertEquals(payment2.getPaymentIntentId(), refund1.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund1.getStatus(), "wrong status");
            assertTrue(refund1.isRefundFully(), "wrong full refund status");

            // the the older payment second - partially
            PaymentRefund refund2 = refunds.get(1);
            assertEquals(partOfAmount1, refund2.getAmount(), "wrong refund amount");
            assertEquals(amount1ToRefundToday, refund2.getPaidAmount(), "wrong refund amount");
            // refund more recent first
            assertEquals(payment1.getPaymentIntentId(), refund2.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund2.getStatus(), "wrong status");
            assertFalse(refund2.isRefundFully(), "wrong full refund status");

            // 'persist' refunds
            payments.addAll(refunds);
        }

        // second complete refund
        {
            // refund fully  250 - 200 = 50
            double refundDayExchangeRate = 1.31;
            int amountToRefundInAccountCurrency = 5000;
            int totalAmountToRefundToday = (int) (refundDayExchangeRate * amountToRefundInAccountCurrency);
            int amount1ToRefundToday = (int) (refundDayExchangeRate * amountToRefundInAccountCurrency);

            RefundCalculator refundCalculator = new RefundCalculator(payments, amountToRefundInAccountCurrency, refundDayExchangeRate);
            List<PaymentRefund> refunds = refundCalculator.determineRefunds();
            assertEquals(1, refunds.size(), "wrong number of refunds");

            // refund the remaining older payment fully
            PaymentRefund refund1 = refunds.get(0);
            assertEquals(amountToRefundInAccountCurrency, refund1.getAmount(), "wrong refund amount");
            assertEquals(amount1ToRefundToday, refund1.getPaidAmount(), "wrong refund amount");
            // refund more recent first
            assertEquals(payment1.getPaymentIntentId(), refund1.getPaymentIntentId(), "wrong payment intent id");
            assertEquals(PaymentRefundStatus.REFUND_COMPLETED, refund1.getStatus(), "wrong status");
            assertTrue(refund1.isRefundFully(), "wrong full refund status");
        }
    }

    /**
     * makes payment object in foreign currency
     * @param amount
     * @param amountPaid
     * @param paymentIntentId
     * @param id
     * @param paymentDate
     * @return
     */
    private PaymentRefund makeForeignCurrencyPaymentObject(int amount, int amountPaid, String paymentIntentId, long id, Date paymentDate) {
        PaymentRefund payment = new PaymentRefund();
        payment.setPaymentRefundFor(PaymentRefundFor.TOURNAMENT_ENTRY);
        payment.setId(id);
        payment.setItemId(87);
        payment.setStatus(PaymentRefundStatus.PAYMENT_COMPLETED);
        payment.setTransactionDate(paymentDate);
        payment.setAmount(amount);
        payment.setPaidAmount(amountPaid);
        payment.setPaidCurrency("cad");
        payment.setPaymentIntentId(paymentIntentId);
        return payment;
    }

}
