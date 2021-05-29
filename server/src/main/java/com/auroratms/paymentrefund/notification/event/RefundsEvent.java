package com.auroratms.paymentrefund.notification.event;

import com.auroratms.paymentrefund.PaymentRefundFor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Event representing multiple partial refunds
 */
@Data
@NoArgsConstructor
public class RefundsEvent {

    // tournament, clinic or usatt fee
    private PaymentRefundFor paymentRefundFor;

    // id of a tournament, clinic or something for which we are paying/refunding
    private long itemId;

    // date & time of payment and refund
    private Date transactionDate;

    private List<RefundItem> refundItems;

    public static class RefundItem {
        // amount actually paid in currency of payment - may be different from tournament currency
        private double paidAmount;

        // currency code in which the paid amount is expressed
        private String paidCurrency;

        public RefundItem(double paidAmount, String paidCurrency) {
            this.paidAmount = paidAmount;
            this.paidCurrency = paidCurrency;
        }

        public double getPaidAmount() {
            return paidAmount;
        }

        public String getPaidCurrency() {
            return paidCurrency;
        }
    }
}
