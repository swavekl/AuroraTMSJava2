/**
 * Request form importing tournament and its events
 */
export interface ImportTournamentRequest {
  // id of a ttAurora tournament to import to, 0 if new tournament is to be created
  tournamentId: number;

  // url of the players list in Omnipong
  playersUrl: string;

  // name extracted from the list
  tournamentName: string;
  tournamentCity: string;
  tournamentState: string;

  // start and end dates of the tournament
  tournamentDates: string;
  tournamentStarLevel: string;

  // tournament director name, email and phone
  tournamentDirectorName: string;
  tournamentDirectorEmail: string;
  tournamentDirectorPhone: string;

  // type of ball to be used
  ballType: string;
}
