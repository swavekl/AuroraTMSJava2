import {MonitorMessageType} from '../../monitor/model/monitor-message-type';

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

  // indicates if warmup was started
  warmupStarted: boolean;

  // cards received by player/team A
  playerACardsJSON: string;

  // cards received by player/team B
  playerBCardsJSON: string;

  servingOrderStateJSON: string;

  // names of umpire and assistant umpire
  umpireName: string;
  assistantUmpireName: string;

  // initial server side - either 'left' or 'right'
  initialServerSide: string;

  // if true the match is umpired and scores are entered via a tablet
  matchUmpired: boolean;

  // type of message
  messageType: MonitorMessageType;

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

  public static isGameFinished(player1GameScore: number, player2GameScore: number, pointsPerGame: number) {
    return Match.isGameWon(player1GameScore, player2GameScore, pointsPerGame) ||
           Match.isGameWon(player2GameScore, player1GameScore, pointsPerGame);

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

  /**
   * Finishes the match which may have been started but not finished due to default
   * @param match
   * @param defaultedPlayerIndex
   * @param numberOfGames
   * @param pointsPerGame
   */
  public static defaultMatch(match: Match, defaultedPlayerIndex: number, numberOfGames: number, pointsPerGame: number): Match {
    const sideADefaulted: boolean = (defaultedPlayerIndex === 0);
    const sideBDefaulted: boolean = (defaultedPlayerIndex === 1);
    let defaultedMatch: Match = {
      ...match,
      sideADefaulted: sideADefaulted,
      sideBDefaulted: sideBDefaulted
    };

    let numGamesWonByA = 0;
    let numGamesWonByB = 0;
    for (let gameIndex = 0; gameIndex < numberOfGames; gameIndex++) {
      let playerAGameScore = 0;
      let playerBGameScore = 0;
      switch (gameIndex) {
        case 0:
          playerAGameScore = defaultedMatch.game1ScoreSideA;
          playerBGameScore = defaultedMatch.game1ScoreSideB;
          break;
        case 1:
          playerAGameScore = defaultedMatch.game2ScoreSideA;
          playerBGameScore = defaultedMatch.game2ScoreSideB;
          break;
        case 2:
          playerAGameScore = defaultedMatch.game3ScoreSideA;
          playerBGameScore = defaultedMatch.game3ScoreSideB;
          break;
        case 3:
          playerAGameScore = defaultedMatch.game4ScoreSideA;
          playerBGameScore = defaultedMatch.game4ScoreSideB;
          break;
        case 4:
          playerAGameScore = defaultedMatch.game5ScoreSideA;
          playerBGameScore = defaultedMatch.game5ScoreSideB;
          break;
        case 5:
          playerAGameScore = defaultedMatch.game6ScoreSideA;
          playerBGameScore = defaultedMatch.game6ScoreSideB;
          break;
        case 6:
          playerAGameScore = defaultedMatch.game7ScoreSideA;
          playerBGameScore = defaultedMatch.game7ScoreSideB;
          break;
      }

      if (this.isGameWon(playerAGameScore, playerBGameScore, pointsPerGame)) {
        numGamesWonByA++;
      } else if (this.isGameWon(playerBGameScore, playerAGameScore, pointsPerGame)) {
        numGamesWonByB++;
      } else {
        // game was not finished or started
        defaultedMatch = this.defaultGame(defaultedMatch, gameIndex, sideADefaulted, pointsPerGame);
        if (sideADefaulted) {
          numGamesWonByB++;
        } else {
          numGamesWonByA++;
        }
      }
      const minimumNumberOfGamesToWin = (numberOfGames === 3) ? 2 : ((numberOfGames === 5) ? 3 : 4);
      if (sideADefaulted && numGamesWonByB === minimumNumberOfGamesToWin) {
        break;
      } else if (sideBDefaulted && numGamesWonByA === minimumNumberOfGamesToWin) {
        break;
      }
    }

    return defaultedMatch;
  }

  /**
   * Writes the score 11: x or x: 11 depending on who defaulted
   * @param match
   * @param gameIndex
   * @param sideADefaulted
   * @param pointsPerGame
   * @private
   */
  private static defaultGame(match: Match, gameIndex: number, sideADefaulted: boolean, pointsPerGame: number): Match {
    let defaultedMatch: Match = {...match};
    switch (gameIndex) {
      case 0:
        defaultedMatch = {
          ...defaultedMatch,
          game1ScoreSideA: (sideADefaulted) ? defaultedMatch.game1ScoreSideA : pointsPerGame,
          game1ScoreSideB: (!sideADefaulted) ? defaultedMatch.game1ScoreSideB : pointsPerGame
        };
        break;
      case 1:
        defaultedMatch = {
          ...defaultedMatch,
          game2ScoreSideA: (sideADefaulted) ? defaultedMatch.game2ScoreSideA : pointsPerGame,
          game2ScoreSideB: (!sideADefaulted) ? defaultedMatch.game2ScoreSideB : pointsPerGame
        };
        break;
      case 2:
        defaultedMatch = {
          ...defaultedMatch,
          game3ScoreSideA: (sideADefaulted) ? defaultedMatch.game3ScoreSideA : pointsPerGame,
          game3ScoreSideB: (!sideADefaulted) ? defaultedMatch.game3ScoreSideB : pointsPerGame
        };
        break;
      case 3:
        defaultedMatch = {
          ...defaultedMatch,
          game4ScoreSideA: (sideADefaulted) ? defaultedMatch.game4ScoreSideA : pointsPerGame,
          game4ScoreSideB: (!sideADefaulted) ? defaultedMatch.game4ScoreSideB : pointsPerGame
        };
        break;
      case 4:
        defaultedMatch = {
          ...defaultedMatch,
          game5ScoreSideA: (sideADefaulted) ? defaultedMatch.game5ScoreSideA : pointsPerGame,
          game5ScoreSideB: (!sideADefaulted) ? defaultedMatch.game5ScoreSideB : pointsPerGame
        };
        break;
      case 5:
        defaultedMatch = {
          ...defaultedMatch,
          game6ScoreSideA: (sideADefaulted) ? defaultedMatch.game6ScoreSideA : pointsPerGame,
          game6ScoreSideB: (!sideADefaulted) ? defaultedMatch.game6ScoreSideB : pointsPerGame
        };
        break;
      case 6:
        defaultedMatch = {
          ...defaultedMatch,
          game7ScoreSideA: (sideADefaulted) ? defaultedMatch.game7ScoreSideA : pointsPerGame,
          game7ScoreSideB: (!sideADefaulted) ? defaultedMatch.game7ScoreSideB : pointsPerGame
        };
        break;
    }
    return defaultedMatch;
  }

  public static getScoreInGames(numberOfGames: number, pointsPerGame: number, match: Match): any {
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

        if (Match.isGameWon(playerAGameScore, playerBGameScore, pointsPerGame)) {
          numGamesWonByA++;
        } else if (Match.isGameWon(playerBGameScore, playerAGameScore, pointsPerGame)) {
          numGamesWonByB++;
        }
      }
      return {playerAGames: numGamesWonByA, playerBGames: numGamesWonByB};
  }
}
