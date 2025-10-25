package com.auroratms.utils;

/**
 * Class for communicating import progress
 */
public class ImportProgressInfo {
    public String jobId;
    // target tournament id
    public long tournamentId;
    public String phaseName;
    public String status = "STARTING";  // RUNNING, COMPLETED, FAILED

    // percent completion
    public int overallCompleted;
    public int phaseCompleted;

    public int profilesCreated;

    public int entriesAdded;
    public int entriesUpdated;
    public int entriesDeleted;
    public int totalEntries;

    public int evenEntriesAdded;
    public int evenEntriesDeleted;
    public int totalEventEntries;
}
