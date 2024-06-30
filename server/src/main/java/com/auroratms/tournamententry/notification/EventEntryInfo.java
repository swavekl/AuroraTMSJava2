package com.auroratms.tournamententry.notification;

import com.auroratms.event.TournamentEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Helper class for getting day and time of event
 */
public class EventEntryInfo {
    private String eventName;
    private String eventDayAndTime;

    public EventEntryInfo(Date tournamentStartDate, TournamentEvent tournamentEvent) {
        this.eventName = tournamentEvent.getName();
        this.eventDayAndTime = formatDayAndTime(tournamentStartDate, tournamentEvent.getDay(), tournamentEvent.getStartTime());
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventDayAndTime() {
        return eventDayAndTime;
    }

    private String formatDayAndTime(Date tournamentStartDate, int day, double startTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(tournamentStartDate);
        calendar.add(Calendar.DATE, day - 1);
        // time is expressed as 17.5 = 5:30 PM
        int hourOfDay = (int) Math.floor(startTime);
        int minutes = (int) ((startTime - Math.floor(startTime)) * 60);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minutes);

        DateFormat dateFormat = new SimpleDateFormat("E hh:mm a");
        return dateFormat.format(calendar.getTime());
    }
}
