export class Match {
  // to be determined player profile id
  static readonly TBD_PROFILE_ID = 'TBD';

  // unique id
  id: number;

  // match card grouping matches together
  // matchCard: MatchCard;

  // match number within a round so that matches are ordered properly on the match card
  matchNum: number;

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
  game1ScoreSideA: number;
  game1ScoreSideB: number;
  game2ScoreSideA: number;
  game2ScoreSideB: number;
  game3ScoreSideA: number;
  game3ScoreSideB: number;
  game4ScoreSideA: number;
  game4ScoreSideB: number;
  game5ScoreSideA: number;
  game5ScoreSideB: number;
  game6ScoreSideA: number;
  game6ScoreSideB: number;
  game7ScoreSideA: number;
  game7ScoreSideB: number;

  // profile id of player who entered score or null
  scoreEnteredByProfileId: string;

  // left (A) side and right (B) side letter code for player in a group e.g. A, B, C, D etc.
  playerALetter: string;
  playerBLetter: string;

  // seed rating of player in a match
  playerARating: number;
  playerBRating: number;

  /**
   *
   * @param profileId
   * @param match
   * @param numberOfGames
   * @param pointsPerGame
   */
  public static isMatchWinner(profileId: string,
                              match: Match,
                              numberOfGames: number,
                              pointsPerGame: number): boolean {
    let numGamesWonByA = 0;
    let numGamesWonByB = 0;
    for (let i = 0; i < numberOfGames; i++) {
      let playerAGameScore = 0;
      let playerBGameScore = 0;
      switch (i) {
        case 0:
          playerAGameScore = match.game1ScoreSideA;
          playerBGameScore = match.game1ScoreSideB;
          break;
        case 1:
          playerAGameScore = match.game2ScoreSideA;
          playerBGameScore = match.game2ScoreSideB;
          break;
        case 2:
          playerAGameScore = match.game3ScoreSideA;
          playerBGameScore = match.game3ScoreSideB;
          break;
        case 3:
          playerAGameScore = match.game4ScoreSideA;
          playerBGameScore = match.game4ScoreSideB;
          break;
        case 4:
          playerAGameScore = match.game5ScoreSideA;
          playerBGameScore = match.game5ScoreSideB;
          break;
        case 5:
          playerAGameScore = match.game6ScoreSideA;
          playerBGameScore = match.game6ScoreSideB;
          break;
        case 6:
          playerAGameScore = match.game7ScoreSideA;
          playerBGameScore = match.game7ScoreSideB;
          break;
      }

      if (this.isGameWon(playerAGameScore, playerBGameScore, pointsPerGame)) {
        numGamesWonByA++;
      } else if (this.isGameWon(playerBGameScore, playerAGameScore, pointsPerGame)) {
        numGamesWonByB++;
      }
    }
    // console.log('-------------------------------- match #', match.matchNum);
    // console.log('A defaulted', match.sideADefaulted);
    // console.log('B defaulted', match.sideBDefaulted);
    // console.log('numGamesWonByA', numGamesWonByA);
    // console.log('numGamesWonByB', numGamesWonByB);
    // in best of 3 need to win 2 gaems, best of 5 need to win 3, best of 7 need to win 4
    const minimumNumberOfGamesToWin = (numberOfGames === 3) ? 2 : ((numberOfGames === 5) ? 3 : 4);
    if (profileId === match.playerAProfileId) {
      return (numGamesWonByA === minimumNumberOfGamesToWin) || (match.sideBDefaulted && !match.sideADefaulted);
    } else {
      return (numGamesWonByB === minimumNumberOfGamesToWin) || (match.sideADefaulted && !match.sideBDefaulted);
    }
  }

  public static isGameWon(player1GameScore: number, player2GameScore: number, pointsPerGame: number) {
    if (player1GameScore >= pointsPerGame) {
      return (player1GameScore > player2GameScore && ((player1GameScore - player2GameScore) >= 2));
    } else {
      return false;
    }
  }


  /**
   * Tests if the complete match score was entered
   * @param match
   * @param numberOfGames
   * @param pointsPerGame
   */
  public static isMatchFinished (match: Match, numberOfGames: number, pointsPerGame: number): boolean {
    return Match.isMatchWinner(match.playerAProfileId, match, numberOfGames, pointsPerGame) ||
           Match.isMatchWinner(match.playerBProfileId, match, numberOfGames, pointsPerGame);
  }

  /**
   * Gets index of the first game whose score has not been entered
   * @param match
   * @param numberOfGames
   */
  public static nextNotEnteredGameIndex(match: Match, numberOfGames: number): number {
    if (match.game1ScoreSideA === 0 && match.game1ScoreSideB === 0) {
      return 0;
    } else if (match.game2ScoreSideA === 0 && match.game2ScoreSideB === 0) {
      return 1;
    } else if (match.game3ScoreSideA === 0 && match.game3ScoreSideB === 0) {
      return 2;
    } else if (match.game4ScoreSideA === 0 && match.game4ScoreSideB === 0) {
      return 3;
    } else if (match.game5ScoreSideA === 0 && match.game5ScoreSideB === 0) {
      return 4;
    } else if (match.game6ScoreSideA === 0 && match.game6ScoreSideB === 0) {
      return 5;
    } else if (match.game7ScoreSideA === 0 && match.game7ScoreSideB === 0) {
      return 6;
    } else {
      return 0;
    }
  }
}
