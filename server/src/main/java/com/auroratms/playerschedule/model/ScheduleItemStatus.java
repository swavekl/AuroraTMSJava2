package com.auroratms.playerschedule.model;

public enum ScheduleItemStatus {
    // not ready because one or more players have not been determined
    NotReady,
    // all players are determined but the match was not started on the table yet
    NotStarted,
    // match started on the table
    Started,
    // some matches have been played already
    InProgress,
    // all matches are completed
    Completed
}
