/**
 * Request for importing tournament player entries
 */
export interface ImportEntriesRequest {
  // id of a ttAurora tournament to import to, 0 if new tournament is to be created
  tournamentId: number;

  // url of the players list in Omnipong
  playersUrl: string;

  // path to a file in repository containing the list of player names, email addresses and state of residence
  emailsFileRepoPath: string;
}
