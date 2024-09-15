import {PositionsRecorder} from './positions-recorder';

/**
 * Calculator for determining the order of serving
 */
export class OrderOfServingCalculator {

  // we move in these arrays in circular fashion
  // either forward e.g. A -> X, X -> B, B -> Y, Y -> A  or
  // backwards e.g. A -> Y, Y -> B, B -> X, X -> A
  private readonly doublesOrderOfPlayers: string [] = ['A', 'X', 'B', 'Y'];
  // trivial case for singles A -> X, X -> A
  private readonly singlesOrderOfPlayers: string [] = ['A', 'X'];

  // if true it is a doubles event
  doubles: boolean = false;

  // if true the current game serving order is determined by cycling forward in orderOfPlayers array
  // if false backwards
  cyclingForward: boolean = true;

  // previous total points to detect if we are moving forward
  previousTotalPoints: number = 0;

  // current game index - 0 based
  currentGame: number = 0;

  // array of servers and receivers for given games e.g. A,X means A serving X
  gameServerAndReceiver: string [] = [];

  // current server and receiver letters
  // letters designating players e.g. A X Y or B
  currentServer: string = 'A';
  currentReceiver: string = 'X';

  positionsRecorder: PositionsRecorder;

  constructor(numGames: number, doubles: boolean, playerAProfileId: string, playerBProfileId: string) {
    this.gameServerAndReceiver = new Array(numGames);
    this.doubles = doubles;
    this.positionsRecorder = new PositionsRecorder(doubles, playerAProfileId, playerBProfileId);
    this.previousTotalPoints = 0;
  }

  private getOrderOfPlayers() {
    return (this.doubles) ? this.doublesOrderOfPlayers : this.singlesOrderOfPlayers;
  }

  /**
   * advances to the next game
   */
  public startNextGame(): number {
    this.previousTotalPoints = 0;
    return ++this.currentGame;
  }

  /**
   * Records server and receiver of the current game
   * @param serverLetter
   * @param receiverLetter
   */
  public recordServerAndReceiver(serverLetter: string, receiverLetter: string): void {
    this.gameServerAndReceiver[this.currentGame] = `${serverLetter},${receiverLetter}`;
    this.currentServer = serverLetter;
    this.currentReceiver = receiverLetter;
    this.cyclingForward = this.isCyclingForward(this.currentServer, this.currentReceiver);
    this.positionsRecorder.recordPlayerPositions(this.currentServer, this.currentReceiver);
  }

  /**
   * Finds out if it is cyclying forward or backwards based on the passed in server and receiver
   * @param currentServer
   * @param currentReceiver
   */
  private isCyclingForward(currentServer: string, currentReceiver: string): boolean {
    const orderOfPlayers = this.getOrderOfPlayers();
    // determine direction of cycling through A X B Y order or A X
    let idxServer = orderOfPlayers.indexOf(currentServer);
    let idxReceiver = orderOfPlayers.indexOf(currentReceiver);
    return (idxServer + 1 === idxReceiver) ||
      (idxServer === (orderOfPlayers.length - 1) && idxReceiver === 0);
  }

  /**
   *
   * @param pointsSideA
   * @param pointsSideB
   */
  public determineNextServerAndReceiver(pointsSideA: number, pointsSideB: number) {
    const orderOfPlayers = this.getOrderOfPlayers();
    let idxServer = orderOfPlayers.indexOf(this.currentServer);
    let idxReceiver = orderOfPlayers.indexOf(this.currentReceiver);

    const previousServer = this.currentServer;
    const previousReceiver = this.currentReceiver;

    const totalPoints = pointsSideA + pointsSideB;
    // if umpire is correcting a score from say 2:1 to 1:2 via 1:1 then 1:2 then don't cycle forward or backward
    const divisor = (pointsSideA >= 10 && pointsSideB >= 10) ? 1 : 2;
    const remainder = totalPoints % divisor;
    const previousRemainder = this.previousTotalPoints % divisor;
    const reverseDirection = (this.previousTotalPoints > totalPoints);

    // figure out if server and receiver changes (every 2 points or at every point after 10 : 10)
    const changeServer =
      (!reverseDirection && (totalPoints > 0) && (remainder === 0)) ||
      (reverseDirection && (this.previousTotalPoints > 0) && (previousRemainder === 0));

    this.previousTotalPoints = totalPoints;
    let changeReceiver: boolean = changeServer;
    if (changeServer) {
      if (!reverseDirection) {
        if (this.cyclingForward) {
          idxServer++;
          idxServer = (idxServer === orderOfPlayers.length) ? 0 : idxServer;
        } else {
          idxServer--;
          idxServer = (idxServer < 0) ? orderOfPlayers.length - 1 : idxServer;
        }
      } else {
        // temporarily change the direction of cycling
        if (this.cyclingForward) {
          idxServer--;
          idxServer = (idxServer < 0) ? orderOfPlayers.length - 1 : idxServer;
        } else {
          idxServer++;
          idxServer = (idxServer === orderOfPlayers.length) ? 0 : idxServer;
        }
      }
      this.currentServer = orderOfPlayers[idxServer];
    }

    // determine receiver
    let switchSidesInLastGame = false;
    if (this.doubles) {
      // if this is the last game
      if (this.currentGame === (this.gameServerAndReceiver.length - 1))
        // one of the teams won 5 points
        if ((pointsSideA === 5 && pointsSideB < 5) ||
          (pointsSideA < 5 && pointsSideB === 5)) {
          switchSidesInLastGame = true;
          changeReceiver = true;
          this.cyclingForward = !this.cyclingForward;
          // adjust index so calculation below is properly cycling to the other receiver
          if (!reverseDirection) {
            if (this.cyclingForward) {
              idxReceiver++;
            } else {
              idxReceiver--;
            }
          }
        }
    }

    if (changeReceiver) {
      if (!reverseDirection) {
        if (this.cyclingForward) {
          idxReceiver++;
          idxReceiver = (idxReceiver === orderOfPlayers.length) ? 0 : idxReceiver;
        } else {
          idxReceiver--;
          idxReceiver = (idxReceiver < 0) ? orderOfPlayers.length - 1 : idxReceiver;
        }
      } else {
        // temporarily reverse the direction of cycling
        if (this.cyclingForward) {
          idxReceiver--;
          idxReceiver = (idxReceiver < 0) ? orderOfPlayers.length - 1 : idxReceiver;
        } else {
          idxReceiver++;
          idxReceiver = (idxReceiver === orderOfPlayers.length) ? 0 : idxReceiver;
        }
      }
      this.currentReceiver = orderOfPlayers[idxReceiver];
    }

    if (this.doubles) {
      if (this.currentReceiver !== previousReceiver) {
        const leftSide = this.positionsRecorder.isPlayerOnLeftSide(this.currentReceiver);
        this.positionsRecorder.switchPlayers(leftSide);
      }
      if (switchSidesInLastGame) {
        this.positionsRecorder.switchSides();
        if (this.currentServer !== previousServer) {
          const leftSide = this.positionsRecorder.isPlayerOnLeftSide(this.currentServer);
          this.positionsRecorder.switchPlayers(leftSide);
        }
      }
    }
    console.log('NEW currentServer ' + this.currentServer + ' currentReceiver ' + this.currentReceiver);
  }

  /**
   * Determines the next receiver given selected server based on the previous game's serving order
   * @param selectedServer
   */
  public determineNextReceiver(selectedServer: string) {
    // get serving direction of the previous game
    const previousGame = Math.max(0, this.currentGame - 1);
    const currentGameServerAndReceiver = this.gameServerAndReceiver[previousGame];
    const playerLetters: string [] = (currentGameServerAndReceiver != null) ? currentGameServerAndReceiver.split(',') : [];
    const serverPlayer = playerLetters[0];
    const receiverPlayer = playerLetters[1];
    const wasCyclingForward = this.isCyclingForward(serverPlayer, receiverPlayer);

    // reverse the direction
    const cycleForward = !wasCyclingForward;

    const orderOfPlayers = this.getOrderOfPlayers();
    let idxNewServer = orderOfPlayers.indexOf(selectedServer);
    let idxReceiver = 0;
    if (cycleForward) {
      idxReceiver = idxNewServer + 1;
      idxReceiver = (idxReceiver === orderOfPlayers.length) ? 0 : idxReceiver;
    } else {
      idxReceiver = idxNewServer - 1;
      idxReceiver = (idxReceiver < 0) ? orderOfPlayers.length - 1 : idxReceiver;
    }
    return orderOfPlayers[idxReceiver];
  }

  getServer(): string {
    return this.currentServer;
  }

  getReceiver(): string {
    return this.currentReceiver;
  }

  /**
   * prepare string to record in database
   */
  public toJson(): string {
    return JSON.stringify({
      doubles: this.doubles,
      cyclingForward: this.cyclingForward,
      currentGame: this.currentGame,
      gameServerAndReceiver: this.gameServerAndReceiver,
      currentServer: this.currentServer,
      currentReceiver: this.currentReceiver,
      positionsRecorder: this.positionsRecorder
    });
  }

  /**
   * reconstitute from string stored in a database
   * @param jsonString
   */
  public fromJson(jsonString: string) {
    const values = JSON.parse(jsonString);
    this.doubles = values.doubles;
    this.cyclingForward = values.cyclingForward;
    this.currentGame = values.currentGame;
    this.gameServerAndReceiver = values.gameServerAndReceiver;
    this.currentReceiver = values.currentReceiver;
    this.currentServer = values.currentServer;
    const fakeProfileIds = (this.doubles) ? 'aa;bb' : 'aaa';
    let positionsRecorder: PositionsRecorder = new PositionsRecorder(this.doubles, fakeProfileIds, fakeProfileIds);
    positionsRecorder.from(values.positionsRecorder);
    this.positionsRecorder = positionsRecorder;
  }

  public switchSides() {
    this.positionsRecorder.switchSides();
  }

  lookupPlayerProfile(sideAndNumber: string) {
    return this.positionsRecorder.getPlayerProfile(sideAndNumber);
  }

  switchPlayers(leftSide: boolean) {
    this.positionsRecorder.switchPlayers(leftSide);
  }

  public isServer(leftSide: boolean) {
    const sideAndNumber = (leftSide) ? 'L2' : 'R1';
    const playerPositions = this.positionsRecorder.getPlayerPositions();
    // console.log('player at position ' + sideAndNumber + ' is ' + playerPositions[sideAndNumber]);
    // console.log('current server is ' + this.currentServer);
    return playerPositions[sideAndNumber] === this.currentServer;
  }

  public isPlayerServer(playerProfileId: string): boolean {
    if (this.positionsRecorder != null) {
      const playerLetter = this.positionsRecorder.lookupPlayerLetter(playerProfileId);
      return playerLetter === this.currentServer;
    } else {
      return false;
    }
  }

  public isPlayerReceiver(playerProfileId: string): boolean {
    if (this.positionsRecorder != null) {
      const playerLetter = this.positionsRecorder.lookupPlayerLetter(playerProfileId);
      return playerLetter === this.currentReceiver;
    } else {
      return false;
    }
  }

  lookupPlayerInPosition(sideAndNumber: string): string {
    const playerPositions = this.positionsRecorder.getPlayerPositions();
    return playerPositions[sideAndNumber];
  }

  determineDefaultServerAndReceiver(isServingFromLeft: boolean) {
    const sideAndNumber = isServingFromLeft ? 'L2' : 'R1';
    const defaultServer = this.lookupPlayerInPosition(sideAndNumber);
    const nextReceiver = this.determineNextReceiver(defaultServer);
    this.recordServerAndReceiver(defaultServer, nextReceiver);
    // this.determineNextServerAndReceiver(this.gameScoreSideA, this.gameScoreSideB);

  }
}
