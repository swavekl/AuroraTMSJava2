export class Match {
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

      if (playerAGameScore >= pointsPerGame && playerBGameScore < playerAGameScore) {
        numGamesWonByA++;
      } else if (playerBGameScore >= pointsPerGame && playerAGameScore < playerBGameScore) {
        numGamesWonByB++;
      }
    }
    // console.log('A defaulted', this.sideADefaulted);
    // console.log('B defaulted', this.sideBDefaulted);
    // in best of 3 need to win 2 gaems, best of 5 need to win 3, best of 7 need to win 4
    const minimumNumberOfGamesToWin = (numberOfGames === 3) ? 2 : ((numberOfGames === 5) ? 3 : 4);
    if (profileId === match.playerAProfileId) {
      return (numGamesWonByA === minimumNumberOfGamesToWin) || (match.sideBDefaulted && !match.sideADefaulted);
    } else {
      return (numGamesWonByB === minimumNumberOfGamesToWin) || (match.sideADefaulted && !match.sideBDefaulted);
    }
  }
}
