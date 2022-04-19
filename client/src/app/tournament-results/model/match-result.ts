export class MatchResult {

  // number of games won by A and B sides
  gamesWonByA: number;
  gamesWonByB: number;

  // compact result e.g. 6, 7, -5, 9
  compactMatchResult: string;

  playerALetter: string;
  playerBLetter: string;

  // true if the side defaulted.  If both are true the match wasn't played
  sideADefaulted: boolean;
  sideBDefaulted: boolean;
}
