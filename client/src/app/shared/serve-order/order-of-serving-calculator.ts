/**
 * Calculator for determining the order of serving
 */
export class OrderOfServingCalculator {

  doubles: boolean = false;

  // we move in these arrays in circular fashion
  // either forward e.g. A -> X, X -> B, B -> Y, Y -> A  or
  // backwards e.g. A -> Y, Y -> B, B -> X, X -> A
  doublesOrderOfPlayers: string [] = ['A', 'X', 'B', 'Y'];
  // trivial case for singles A -> X, X -> A
  singlesOrderOfPlayers: string [] = ['A', 'X'];

  currentGame: number = 0;

  gameServerAndReceiver: string [] = [];

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

  startNextGame(): number {
    return ++this.currentGame;
  }

  recordServerAndReceiver(serverLetter: string, receiverLetter: string): void  {
    this.gameServerAndReceiver[this.currentGame] = `${serverLetter},${receiverLetter}`;
    this.currentServer = serverLetter;
    this.currentReceiver = receiverLetter;
  }

  public determineNextServerAndReceiver(pointsSideA: number, pointsSideB: number) {
    const orderOfPlayers = this.getOrderOfPlayers()
    // determine direction of cycling through A X B Y order or A X
    let idxServer = orderOfPlayers.indexOf(this.currentServer);
    let idxReceiver = orderOfPlayers.indexOf(this.currentReceiver);
    let cycleForward = (idxServer + 1 === idxReceiver) ||
      (idxServer === (orderOfPlayers.length - 1) && idxReceiver === 0);

    const totalPoints = pointsSideA + pointsSideB;
    const divisor = (pointsSideA >= 10 && pointsSideB >= 10) ? 1 : 2;
    const remainder = totalPoints % divisor;
    const changeServer = (remainder === 0) && (totalPoints > 0);
    console.log('changeServer', changeServer);
    let changeReceiver: boolean = changeServer;
    if (changeServer) {
      if (cycleForward) {
        idxServer++;
        idxServer = (idxServer === orderOfPlayers.length) ? 0 : idxServer;
      } else {
        idxServer--;
        idxServer = (idxServer < 0) ? orderOfPlayers.length - 1 : idxServer;
      }
      console.log('idxServer', idxServer);
      this.currentServer = orderOfPlayers[idxServer];
    }

    // determine receiver
    if (this.doubles) {
      // if this is the last game
      if (this.currentGame === (this.gameServerAndReceiver.length - 1))
        // one of the teams won 5 points
        if ((pointsSideA === 5 && pointsSideB !== 5) ||
            (pointsSideA !== 5 && pointsSideB === 5)) {
        changeReceiver = true;
          cycleForward = !cycleForward;
      }

      // // change cyclind direction for the rest of the last deciding game
      // if (pointsSideA >= 5 || pointsSideB >= 5) {
      // }
    }

    if (changeReceiver) {
      if (cycleForward) {
        idxReceiver++;
        idxReceiver = (idxReceiver === orderOfPlayers.length) ? 0 : idxReceiver
      } else {
        idxReceiver--;
        idxReceiver = (idxReceiver < 0) ? orderOfPlayers.length - 1 : idxReceiver;
      }
      console.log('idxReceiver', idxReceiver);
      this.currentReceiver = orderOfPlayers[idxReceiver];
    }
  }

  public determineNextReceiverFromServer(selectedServer: string) {
    const previousGame = Math.max(0, this.currentGame - 1);
    const currentGameServerAndReceiver = this.gameServerAndReceiver[previousGame];
    const playerLetters: string [] = currentGameServerAndReceiver.split(",");
    const serverPlayer = playerLetters[0];
    const receiverPlayer = playerLetters[1];

    const orderOfPlayers = this.getOrderOfPlayers()
    // determine direction of cycling through A X B Y order or A X
    let idxServer = orderOfPlayers.indexOf(serverPlayer);
    let idxReceiver = orderOfPlayers.indexOf(receiverPlayer);
    let cycleForward = (idxServer + 1 === idxReceiver) ||
      (idxServer === (orderOfPlayers.length - 1) && idxReceiver === 0);

    // reverse the direction
    cycleForward = !cycleForward;
    let idxNewServer = orderOfPlayers.indexOf(selectedServer);
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
  public toJson (): string {
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
