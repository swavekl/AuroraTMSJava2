/**
 * Calculator for determining the order of serving
 */
export class OrderOfServingCalculator {

  // if true it is a doubles event
  doubles: boolean = false;

  // we move in these arrays in circular fashion
  // either forward e.g. A -> X, X -> B, B -> Y, Y -> A  or
  // backwards e.g. A -> Y, Y -> B, B -> X, X -> A
  doublesOrderOfPlayers: string [] = ['A', 'X', 'B', 'Y'];
  // trivial case for singles A -> X, X -> A
  singlesOrderOfPlayers: string [] = ['A', 'X'];

  // if true the current game serving order is determined by cycling forward in orderOfPlayers array
  // if false backwards
  cyclingForward: boolean = true;

  // current game index - 0 based
  currentGame: number = 0;

  // array of servers and receivers for given games e.g. A,X means A serving X
  gameServerAndReceiver: string [] = [];

  // current server and receiver letters
  // letters designating players e.g. A X Y or B
  currentServer: string;
  currentReceiver: string;

  constructor(numGames: number, doubles: boolean) {
    this.gameServerAndReceiver = new Array(numGames);
    this.doubles = doubles;
  }

  private getOrderOfPlayers() {
    return (this.doubles) ? this.doublesOrderOfPlayers : this.singlesOrderOfPlayers;
  }

  /**
   * advances to the next game
   */
  public startNextGame(): number {
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

    const totalPoints = pointsSideA + pointsSideB;
    const divisor = (pointsSideA >= 10 && pointsSideB >= 10) ? 1 : 2;
    const remainder = totalPoints % divisor;
    // figure out if server and receiver changes (every 2 points or at 10 : 10)
    const changeServer = (remainder === 0) && (totalPoints > 0);
    let changeReceiver: boolean = changeServer;
    if (changeServer) {
      if (this.cyclingForward) {
        idxServer++;
        idxServer = (idxServer === orderOfPlayers.length) ? 0 : idxServer;
      } else {
        idxServer--;
        idxServer = (idxServer < 0) ? orderOfPlayers.length - 1 : idxServer;
      }
      this.currentServer = orderOfPlayers[idxServer];
    }

    // determine receiver
    if (this.doubles) {
      // if this is the last game
      if (this.currentGame === (this.gameServerAndReceiver.length - 1))
        // one of the teams won 5 points
        if ((pointsSideA === 5 && pointsSideB < 5) ||
          (pointsSideA < 5 && pointsSideB === 5)) {
          changeReceiver = true;
          this.cyclingForward = !this.cyclingForward;
          // adjust index so calculation below is properly cycling to the other receiver
          if (this.cyclingForward) {
            idxReceiver++;
          } else {
            idxReceiver--;
          }
        }
    }

    if (changeReceiver) {
      if (this.cyclingForward) {
        idxReceiver++;
        idxReceiver = (idxReceiver === orderOfPlayers.length) ? 0 : idxReceiver;
      } else {
        idxReceiver--;
        idxReceiver = (idxReceiver < 0) ? orderOfPlayers.length - 1 : idxReceiver;
      }
      this.currentReceiver = orderOfPlayers[idxReceiver];
    }
  }

  /**
   * Determines the next receiver given selected server based on the previous game's serving order
   * @param selectedServer
   */
  public determineNextReceiver(selectedServer: string) {
    // get serving direction of the previous game
    const previousGame = Math.max(0, this.currentGame - 1);
    const currentGameServerAndReceiver = this.gameServerAndReceiver[previousGame];
    const playerLetters: string [] = currentGameServerAndReceiver.split(',');
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
      currentGame: this.currentGame,
      gameServerAndReceiver: this.gameServerAndReceiver,
      currentServer: this.currentServer,
      currentReceiver: this.currentReceiver
    });
  }

  /**
   * reconstitute from string stored in a database
   * @param jsonString
   */
  public fromJson(jsonString: string) {
    const values = JSON.parse(jsonString);
    this.doubles = values.doubles;
    this.currentGame = values.currentGame;
    this.gameServerAndReceiver = values.gameServerAndReceiver;
    this.currentReceiver = values.currentReceiver;
    this.currentServer = values.currentServer;
  }

}
