package com.auroratms.tournamententry.notification.event;

public class TournamentEntryConfirmedEvent {

    private long tournamentEntryId;

    // if true confirmation is for withdrawal
    private boolean withdrawing;

    public TournamentEntryConfirmedEvent(long tournamentEntryId, boolean withdrawing) {
        this.tournamentEntryId = tournamentEntryId;
        this.withdrawing = withdrawing;
    }

    public long getTournamentEntryId() {
        return tournamentEntryId;
    }

    public boolean isWithdrawing() {
        return withdrawing;
    }
}
