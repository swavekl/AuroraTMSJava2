/**
 * Summary of work by umpire during the tournament
 */
export interface UmpireWorkSummary {
  // profile id of umpire
  profileId: string;

  // name of the umpire
  umpireName: string;

  // indicates if umpire is officiating a match currently
  busy: boolean;

  // number of matches umpired as a main umpire during this tournament
  numUmpiredMatches: number;

  // number of matches umpired as an assistant umpire during this tournament
  numAssistantUmpiredMatches: number;
}
