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
    public static final String USATTOfficial = "USATTOfficial";
}
