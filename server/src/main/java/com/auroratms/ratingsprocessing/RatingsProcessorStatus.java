package com.auroratms.ratingsprocessing;

public class RatingsProcessorStatus {

    // phase of processing - reading file, updating player records, writing records history etc.
    public String phase;
    public String error;
    // start & end time
    public long startTime;
    public long endTime;
    public int totalRecords;
    public int processedRecords;

    public int badRecords;
    public int newRecords;

    public int newHistoryRecords;
    public int updatedHistoryRecords;
}
