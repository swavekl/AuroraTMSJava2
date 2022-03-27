package com.auroratms.reports;

/**
 * Result of tournament report generation
 */
public class TournamentReportGenerationResult {
    private String reportFilename;
    private double grandTotalDue;

    public TournamentReportGenerationResult(String reportFilename, double grandTotalDue) {
        this.reportFilename = reportFilename;
        this.grandTotalDue = grandTotalDue;
    }

    public String getReportFilename() {
        return reportFilename;
    }

    public double getGrandTotalDue() {
        return grandTotalDue;
    }
}
