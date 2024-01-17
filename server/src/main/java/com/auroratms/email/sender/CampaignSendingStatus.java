package com.auroratms.email.sender;

public class CampaignSendingStatus {
    // uuid
    public String id;

    public String phase;

    public String error;
    // start & end time
    public long startTime;
    public long endTime;
    public int totalSent;
    public int totalErrors;

}
