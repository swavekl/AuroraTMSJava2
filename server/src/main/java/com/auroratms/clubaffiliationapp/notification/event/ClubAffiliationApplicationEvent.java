package com.auroratms.clubaffiliationapp.notification.event;

import com.auroratms.clubaffiliationapp.ClubAffiliationApplication;
import com.auroratms.clubaffiliationapp.ClubAffiliationApplicationStatus;

public class ClubAffiliationApplicationEvent {

    private ClubAffiliationApplication clubAffiliationApplication;
    private ClubAffiliationApplicationStatus oldStatus;

    public ClubAffiliationApplicationEvent(ClubAffiliationApplication clubAffiliationApplication, ClubAffiliationApplicationStatus oldStatus) {
        this.clubAffiliationApplication = clubAffiliationApplication;
        this.oldStatus = oldStatus;
    }

    public ClubAffiliationApplication getClubAffiliationApplication() {
        return clubAffiliationApplication;
    }

    public ClubAffiliationApplicationStatus getOldStatus() {
        return oldStatus;
    }
}
