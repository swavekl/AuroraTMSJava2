package com.auroratms.clubaffiliationapp.notification.event;

import com.auroratms.clubaffiliationapp.ClubAffiliationApplication;

public class ClubAffiliationApplicationEvent {

    private ClubAffiliationApplication clubAffiliationApplication;

    public ClubAffiliationApplicationEvent(ClubAffiliationApplication clubAffiliationApplication) {
        this.clubAffiliationApplication = clubAffiliationApplication;
    }

    public ClubAffiliationApplication getClubAffiliationApplication() {
        return clubAffiliationApplication;
    }

    public void setClubAffiliationApplication(ClubAffiliationApplication clubAffiliationApplication) {
        this.clubAffiliationApplication = clubAffiliationApplication;
    }
}
