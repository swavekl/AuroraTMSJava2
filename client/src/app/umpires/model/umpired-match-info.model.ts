/**
 * Information about an individual match umpired by the umpire or assistant umpire
 */
export interface UmpiredMatchInfo {
  // date of the match
  matchDate: Date;

  // tournament name
  tournamentName: string;

  // event name
  eventName: string;

  // final, semifinal or round-robin
  roundName: string;

  // names of players who played the match or doubles team names
  playerAName: string;
  playerBName: string;

  // true if playerA won the match
  playerAWon: boolean;
  playerBWon: boolean;

  // short version of a match score e.e. 9,9,-8,7
  matchScore: string;

  // if true served as assistant umpire during this match
  assistantUmpire: boolean;
}
