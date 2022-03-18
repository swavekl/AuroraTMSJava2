package com.auroratms.reports;

/**
 * Thrown when report generation fails
 */
public class ReportGenerationException extends Exception {

    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
