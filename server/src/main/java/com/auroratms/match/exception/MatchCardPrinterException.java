package com.auroratms.match.exception;

/**
 * Exception during creation of match card PDF
 */
public class MatchCardPrinterException extends RuntimeException {

    public MatchCardPrinterException(String message, Throwable cause) {
        super(message, cause);
    }
}
