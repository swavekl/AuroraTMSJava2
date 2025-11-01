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
    public int profilesExisting;
    public int profilesMissing;
    // file which can be downloaded  containing player names who are missing profiles
    public String missingProfileFileRepoUrl;

    public int entriesAdded;
    public int entriesUpdated;
    public int entriesDeleted;
    public int totalEntries;

    public int evenEntriesAdded;
    public int evenEntriesDeleted;
    public int totalEventEntries;
}
