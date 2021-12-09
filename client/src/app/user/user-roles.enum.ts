/**
 * User roles in application - these are Okta Groups
 */
export enum UserRoles {
  ROLE_EVERYONE = 'Everyone',
  ROLE_ADMINS = 'Admins',
  // tournament directors
  ROLE_TOURNAMENT_DIRECTORS = 'TournamentDirectors',
  // tournament referee or deputy referee
  ROLE_REFEREES = 'Referees',
  // tournament umpire
  ROLE_UMPIRES = 'Umpires',
  // clerks entering match scores during tournament
  ROLE_DATA_ENTRY_CLERKS = 'DataEntryClerks',
  // persons dealing with tournament sanction finalization and results processing
  ROLE_USATT_TOURNAMENT_MANAGERS = 'USATTTournamentManagers',
  // persons dealing with club affiliations
  ROLE_USATT_CLUB_MANAGERS = 'USATTClubManagers',
  // persons dealing with referees & umpires certification and updating their status
  ROLE_USATT_MATCH_OFFICIALS_MANAGERS = 'USATTMatchOfficialsManagers',
  // persons dealing with players memberships
  ROLE_USATT_PLAYER_MANAGERS = 'USATTPlayerManagers',
  // persons dealing with sanctioning tournaments - regional and national
  ROLE_USATT_SANCTION_COORDINATORS = 'USATTSanctionCoordinators',
  // persons dealing with insurance certificates or insurance agents issuing these certificates
  ROLE_USATT_INSURANCE_MANAGERS = 'USATTInsuranceManagers'

}
