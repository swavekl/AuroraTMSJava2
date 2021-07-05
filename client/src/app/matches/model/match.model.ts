export class Match {
  // unique id
  id: number;

  // match card grouping matches together
  // matchCard: MatchCard;

  // match number within a round so that matches are ordered properly on the match card
  matchNum: number;

  // for round robin phase 0,
  // for single elimination - 64, 32, 16, 8 (quarter finals), 4 (semifinals), 2 (finals and 3rd/4th place)
  round: number;

  // profile id of two players for singles matches
  // for doubles matches profile ids of team members are separated by ; like this
  // playerAProfileId;playerAPartnerProfileId and playerBProfileId;playerBPartnerProfileId
  playerAProfileId: string;
  playerBProfileId: string;

  // true if the side defaulted.  If both are true the match wasn't played
  sideADefaulted: boolean;
  sideBDefaulted: boolean;

  // indicates if side took a timeout - help for umpire
  sideATimeoutTaken: boolean;
  sideBTimeoutTaken: boolean;

  // indicates if side A is to serve first, if false side B servers first - help for umpire
  sideAServesFirst: boolean;

  // game (set) scores of played match e.g. 11:7, 11:8,
  // positive if match winner won the game,
  // negative if match winner lost the game
  game1Score: number;
  game2Score: number;
  game3Score: number;
  game4Score: number;
  game5Score: number;
  game6Score: number;
  game7Score: number;
}
