package com.auroratms.paymentrefund.exception;

/**
 * Exception encountered during refund processing
 */
public class RefundException extends RuntimeException {

    public RefundException(String message) {
        super(message);
    }
}
