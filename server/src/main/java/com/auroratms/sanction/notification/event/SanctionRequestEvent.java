package com.auroratms.sanction.notification.event;

import com.auroratms.sanction.SanctionRequest;
import com.auroratms.sanction.SanctionRequestStatus;

public class SanctionRequestEvent {

    private SanctionRequest sanctionRequest;

    private SanctionRequestStatus oldStatus;

    public SanctionRequestEvent(SanctionRequest sanctionRequest, SanctionRequestStatus oldStatus) {
        this.sanctionRequest = sanctionRequest;
        this.oldStatus = oldStatus;
    }

    public SanctionRequest getSanctionRequest() {
        return sanctionRequest;
    }

    public SanctionRequestStatus getOldStatus() {
        return oldStatus;
    }
}
