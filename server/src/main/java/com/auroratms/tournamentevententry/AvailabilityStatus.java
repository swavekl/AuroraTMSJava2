package com.auroratms.tournamentevententry;

/**
 * Availability status of each event
 */
public enum AvailabilityStatus {
	ALREADY_ENTERED,
	AVAILABLE_FOR_ENTRY,
	EVENT_FULL,
	DISQUALIFIED_BY_AGE,
	DISQUALIFIED_BY_GENDER,
	DISQUALIFIED_BY_RATING,
	SCHEDULING_CONFLICT,
	MAX_EVENTS_PER_DAY,
    MAX_EVENTS_PER_TOURNAMENT
}
