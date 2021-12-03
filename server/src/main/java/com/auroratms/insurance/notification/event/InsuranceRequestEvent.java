package com.auroratms.insurance.notification.event;

import com.auroratms.insurance.InsuranceRequest;
import com.auroratms.insurance.InsuranceRequestStatus;

public class InsuranceRequestEvent {

    private InsuranceRequest insuranceRequest;
    private InsuranceRequestStatus oldStatus;

    public InsuranceRequestEvent(InsuranceRequest insuranceRequest, InsuranceRequestStatus oldStatus) {
        this.insuranceRequest = insuranceRequest;
        this.oldStatus = oldStatus;
    }

    public InsuranceRequest getInsuranceRequest() {
        return insuranceRequest;
    }

    public InsuranceRequestStatus getOldStatus() {
        return oldStatus;
    }
}
