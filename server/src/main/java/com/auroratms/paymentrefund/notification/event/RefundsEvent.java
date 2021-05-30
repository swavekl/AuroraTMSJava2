package com.auroratms.paymentrefund.notification.event;

import com.auroratms.paymentrefund.PaymentRefundFor;
import com.auroratms.paymentrefund.RefundRequest;

import java.util.List;

/**
 * Event representing multiple partial refunds
 */
public class RefundsEvent {

    // refund request with all details
    private RefundRequest refundRequest;

    // ids of issued refund items
    private List<Long> refundItemIds;

    public RefundsEvent() {
    }

    public RefundsEvent(RefundRequest refundRequest, List<Long> refundItemIds) {
        this.refundRequest = refundRequest;
        this.refundItemIds = refundItemIds;
    }

    public PaymentRefundFor getPaymentRefundFor () {
        return this.refundRequest.getPaymentRefundFor();
    }

    public long getItemId() {
        return refundRequest.getTransactionItemId();
    }

    public int getAmount() {
        return refundRequest.getAmount();
    }

    public String getCurrency() {
        return refundRequest.getCurrencyCode();
    }

    public List<Long> getRefundItemIds() {
        return refundItemIds;
    }
}
