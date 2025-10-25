package com.auroratms.utils;

public class ImportTournamentRequest {
    // id of a ttAurora tournament to import to, 0 if new tournament is to be created
    public long tournamentId;

    // url of the players list in Omnipong
    public String playersUrl;

    // name extracted from the list
    public String tournamentName;
    public String tournamentCity;
    public String tournamentState;

    // start and end dates of the tournament
    public String tournamentDates;
    public String tournamentStarLevel;

    // tournament director name, email and phone
    public String tournamentDirectorName;
    public String tournamentDirectorEmail;
    public String tournamentDirectorPhone;

    // type of ball to be used
    public String ballType;

}

