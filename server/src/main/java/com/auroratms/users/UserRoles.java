package com.auroratms.users;

/**
 * User roles - stored in the acl_sid table
 */
public interface UserRoles {
    public static final String Everyone = "Everyone";
    public static final String Admins = "Admins";
    public static final String TournamentDirectors = "TournamentDirectors";
    public static final String Referees = "Referees";
    public static final String Umpires = "Umpires";
    public static final String DataEntryClerks = "DataEntryClerks";
    // digital score board i.e. table for entering scores which are then displayed on monitor
    public static final String DigitalScoreBoards = "DigitalScoreBoards";
    // monitor for displaying scores on bigger screen and for data entry via tablet
    public static final String Monitors = "Monitors";
    // persons dealing with tournament sanction finalization and results processing
    public static final String USATTTournamentManagers = "USATTTournamentManagers";
    // persons dealing with club affiliations
    public static final String USATTClubManagers = "USATTClubManagers";
    // persons dealing with referees & umpires certification and updating their status
    public static final String USATTMatchOfficialsManagers = "USATTMatchOfficialsManagers";
    // persons dealing with players memberships
    public static final String USATTPlayerManagers = "USATTPlayerManagers";
    // persons dealing with sanctioning tournaments - regional and national
    public static final String USATTSanctionCoordinators = "USATTSanctionCoordinators";
    // persons dealing with insurance certificates or insurance agents issuing these certificates
    public static final String USATTInsuranceManagers = "USATTInsuranceManagers";
}
