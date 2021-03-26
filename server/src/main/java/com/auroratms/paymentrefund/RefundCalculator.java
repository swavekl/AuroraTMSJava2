package com.auroratms.paymentrefund;

import com.auroratms.paymentrefund.exception.RefundException;

import java.util.*;

/**
 * Identifies potentially multiple refunds to cover the
 * requested refund amount.
 */
public class RefundCalculator {

    // list of payments and refunds
    private List<PaymentRefund> paymentRefundList;

    // total refund amount to be refunded
    private long refundAmount;

    /**
     *
     * @param paymentRefundList
     * @param refundAmount
     */
    public RefundCalculator(List<PaymentRefund> paymentRefundList, long refundAmount) {
        this.paymentRefundList = paymentRefundList;
        this.refundAmount = refundAmount;
    }

    /**
     * Determines refunds based on the payments
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

        long remainingToRefund = refundAmount;
        for (PaymentRefund payment : sortedQualifiedPaymentList) {
            Integer remainingBalance = balancesAvailableForRefund.get(payment.getPaymentIntentId());
            // amount of remaining balance covers our refund request then we will be done.
            long amountOfRefund = (remainingBalance > remainingToRefund)
                    ? remainingToRefund : remainingBalance;
            // create an offsetting refund for this transaction
            remainingToRefund -= amountOfRefund;

            PaymentRefund refund = new PaymentRefund();
            refund.setAmount((int)amountOfRefund);
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
        return refundsToIssue;
    }

    /**
     *
     * @param qualifiedPaymentsOnly
     * @return
     */
    private List<PaymentRefund> sortByTransactionDate(List<PaymentRefund> qualifiedPaymentsOnly) {
        // sort them by date from newest to oldest
        Collections.sort(qualifiedPaymentsOnly, new Comparator<PaymentRefund>() {

            @Override
            public int compare(PaymentRefund o1, PaymentRefund o2) {
                Date transactionDate1 = o1.getTransactionDate();
                Date transactionDate2 = o2.getTransactionDate();
                return transactionDate1.compareTo(transactionDate2);
            }
        });
        return qualifiedPaymentsOnly;
    }

    /**
     *
     * @return
     */
    private Map<String, Integer> getBalancesAvailableForRefund() {
        Map<String, Integer> balancesAvailableForRefund = new HashMap<>();

        int totalPayments = 0;
        // get original payment amounts
        for (PaymentRefund paymentRefund : paymentRefundList) {
            if (paymentRefund.status.equals(PaymentRefundStatus.PAYMENT_COMPLETED)) {
                totalPayments += paymentRefund.getAmount();
                balancesAvailableForRefund.put(paymentRefund.getPaymentIntentId(), new Integer(paymentRefund.getAmount()));
            }
        }

        int totalPriorRefunds = 0;
        // reduce the original payment amounts by refunds from these payments
        for (PaymentRefund paymentRefund : paymentRefundList) {
            if (paymentRefund.status.equals(PaymentRefundStatus.REFUND_COMPLETED)) {
                Integer remainingBalance = balancesAvailableForRefund.get(paymentRefund.getPaymentIntentId());
                // make sure we don't go below 0
                totalPriorRefunds += paymentRefund.getAmount();
                remainingBalance = Math.max(0, remainingBalance - paymentRefund.getAmount());
                balancesAvailableForRefund.put(paymentRefund.getPaymentIntentId(), remainingBalance);
            }
        }

        // check if we have enough do a refund
        int totalAvailableForRefunds = totalPayments - totalPriorRefunds;
        if (refundAmount > totalAvailableForRefunds) {
            String errorMessage = String.format("Amount of refund %d exceeds total available for refund %d",
                    refundAmount, totalAvailableForRefunds);
            throw new RefundException(errorMessage);
        }
        return balancesAvailableForRefund;
    }
}
