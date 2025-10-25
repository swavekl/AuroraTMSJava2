package com.auroratms.utils;

public class ImportEntriesRequest {
    // id of a ttAurora tournament to import to, 0 if new tournament is to be created
    public long tournamentId;

    // url of the players list in Omnipong
    public String playersUrl;

    // path to a file in repository containing the list of player names, email addresses and state of residence
    public String emailsFileRepoPath;
}

