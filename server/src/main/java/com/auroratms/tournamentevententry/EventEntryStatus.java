package com.auroratms.tournamentevententry;

public enum EventEntryStatus {
    NOT_ENTERED,
    ENTERED,  // confirmed entry
    ENTERED_WAITING_LIST,
    PENDING_CONFIRMATION,
    PENDING_DELETION,
    PENDING_WAITING_LIST,
    RESERVED_WAITING_LIST // special state waiting for TD to allocate the free spot to a player on the waiting list
}
