package com.auroratms.paymentrefund;

import com.auroratms.paymentrefund.exception.RefundException;

import java.util.*;

/**
 * Identifies potentially multiple refunds to cover the
 * requested refund amount.  All calculations are conducted in account currency
 * and the exchange rate is used only in determining amount to pay
 */
public class RefundCalculator {

    // list of payments and refunds
    private List<PaymentRefund> paymentRefundList;

    // total refund amount in account currency in cents e.g. $10 is 1000 cents
    private long refundAmountInAccountCurrency;

    // currency exchange rate as of moment of refund from account currency to currency of refund
    private double refundDayExchangeRate = 1.0;

    /**
     * @param paymentRefundList list of prior payments and refunds
     * @param refundAmountInAccountCurrency refund amount in account currency (settlement currency)
     * @param refundDayExchangeRate exchange rate between the two currencies or 1.0 if currency is the same
     */
    public RefundCalculator(List<PaymentRefund> paymentRefundList,
                            long refundAmountInAccountCurrency,
                            double refundDayExchangeRate) {
        this.paymentRefundList = paymentRefundList;
        this.refundAmountInAccountCurrency = refundAmountInAccountCurrency;
        this.refundDayExchangeRate = refundDayExchangeRate;
    }

    /**
     * Determines refunds based on the payments
     *
     * @return
     * @throws RefundException
     */
    public List<PaymentRefund> determineRefunds() throws RefundException {
        List<PaymentRefund> refundsToIssue = new ArrayList<>();

        // we don't want to change the original payment record so we must calculate the
        // available refund amount based on the amounts of offsetting refunds if any
        Map<String, Integer> balancesAvailableForRefund = getBalancesAvailableForRefund();

        // get payments which still have any money left for refunds
        List<PaymentRefund> qualifiedPaymentsOnly = new ArrayList<>();
        for (String paymentIntentId : balancesAvailableForRefund.keySet()) {
            Integer remainingBalance = balancesAvailableForRefund.get(paymentIntentId);
            if (remainingBalance > 0) {
                for (PaymentRefund paymentRefund : this.paymentRefundList) {
                    // find this payment
                    if (paymentRefund.getPaymentIntentId().equals(paymentIntentId) &&
                            paymentRefund.getStatus().equals(PaymentRefundStatus.PAYMENT_COMPLETED)) {
                        qualifiedPaymentsOnly.add(paymentRefund);
                        break;
                    }
                }
            }
        }

        // sort by date transactions issued last can be quickly reversed by Stripe
        //  instead of going through more lengthy refund process
        List<PaymentRefund> sortedQualifiedPaymentList = sortByTransactionDate(qualifiedPaymentsOnly);

        long remainingToRefund = refundAmountInAccountCurrency;
        for (PaymentRefund payment : sortedQualifiedPaymentList) {
            Integer remainingPaymentBalance = balancesAvailableForRefund.get(payment.getPaymentIntentId());
            // if amount of remaining balance in this payment covers totally refund request amount
            // then just issue refund for the requested amount,
            // otherwise use the a portion of remaining balance to cover refund
            long amountOfRefund = (remainingPaymentBalance > remainingToRefund)
                    ? remainingToRefund : remainingPaymentBalance;
            // create an offsetting refund for this transaction
            remainingToRefund -= amountOfRefund;
            // check if this transaction should be fully refunded
            // this eliminates the need to specify refund amount when issuing refund request to Stripe
            boolean refundFully = (amountOfRefund == remainingPaymentBalance);

            PaymentRefund refund = new PaymentRefund();
            // in account currency
            refund.setAmount((int) amountOfRefund);
            // get rough amount in requested refund currency using refund day exchange rate
            refund.setPaidAmount((int) (amountOfRefund * refundDayExchangeRate));
            refund.setPaidCurrency(payment.getPaidCurrency());
            refund.setRefundFully(refundFully);
            refund.setItemId(payment.getItemId());
            refund.setPaymentRefundFor(payment.getPaymentRefundFor());
            refund.setPaymentIntentId(payment.getPaymentIntentId());
            refund.setTransactionDate(new Date());
            refund.setStatus(PaymentRefundStatus.REFUND_COMPLETED);
            refundsToIssue.add(refund);

            if (remainingToRefund <= 0) {
                break;
            }
        }

//        System.out.println("Refunds to issue");
//        for (PaymentRefund paymentRefund : refundsToIssue) {
//            System.out.println(paymentRefund);
//        }

        return refundsToIssue;
    }

    /**
     * Sorts payments by date from most recent first to the oldest last
     *
     * @param qualifiedPaymentsOnly list of payments to sort
     * @return
     */
    private List<PaymentRefund> sortByTransactionDate(List<PaymentRefund> qualifiedPaymentsOnly) {
        // sort them by date from newest to oldest
        Collections.sort(qualifiedPaymentsOnly, new Comparator<PaymentRefund>() {

            @Override
            public int compare(PaymentRefund o1, PaymentRefund o2) {
                Date transactionDate1 = o1.getTransactionDate();
                Date transactionDate2 = o2.getTransactionDate();
                return -1 * transactionDate1.compareTo(transactionDate2);
            }
        });
        return qualifiedPaymentsOnly;
    }

    /**
     * @return
     */
    private Map<String, Integer> getBalancesAvailableForRefund() {
        Map<String, Integer> balancesAvailableForRefund = new HashMap<>();

        int totalPayments = 0;
        // get original payment amounts - in account currency
        for (PaymentRefund paymentRefund : paymentRefundList) {
            if (paymentRefund.getStatus().equals(PaymentRefundStatus.PAYMENT_COMPLETED)) {
                totalPayments += paymentRefund.getAmount();
                balancesAvailableForRefund.put(paymentRefund.getPaymentIntentId(), new Integer(paymentRefund.getAmount()));
            }
        }

        int totalPriorRefunds = 0;
        // reduce the original payment amounts by refunds from these payments - again in account currency
        for (PaymentRefund paymentRefund : paymentRefundList) {
            if (paymentRefund.getStatus().equals(PaymentRefundStatus.REFUND_COMPLETED)) {
                Integer remainingBalance = balancesAvailableForRefund.get(paymentRefund.getPaymentIntentId());
                // make sure we don't go below 0
                totalPriorRefunds += paymentRefund.getAmount();
                remainingBalance = Math.max(0, remainingBalance - paymentRefund.getAmount());
                balancesAvailableForRefund.put(paymentRefund.getPaymentIntentId(), remainingBalance);
            }
        }

        // check if we have enough do a refund
        int totalAvailableForRefunds = totalPayments - totalPriorRefunds;
        if (refundAmountInAccountCurrency > totalAvailableForRefunds) {
            String errorMessage = String.format("Requested refund amount of %d exceeds %d total transactions balance available for refund.",
                    refundAmountInAccountCurrency, totalAvailableForRefunds);
            throw new RefundException(errorMessage);
        }
        return balancesAvailableForRefund;
    }
}
