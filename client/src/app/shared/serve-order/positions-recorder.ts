/**
 * Class for recording player positions
 */
export class PositionsRecorder {

  // if true it is a doubles event
  private doubles: boolean = false;

  // map of letters A, B, X and Y to profileIds of players
  // established once at the beginning
  private letterToProfileIdMap: any;

  // position on the table of each player at the moment
  //     +---------+---------+
  //    | L1      | R1      |
  //    +---------+---------+
  //    | L2      | R2      |
  //    +---------+---------+
  private playerPositions: any;

  /**
   *
   * @param doubles
   * @param playerAProfileId
   * @param playerBProfileId
   */
  constructor(doubles: boolean, playerAProfileId: string, playerBProfileId: string) {
    this.doubles = doubles;
    this.letterToProfileIdMap = this.makePlayerProfilesMap(playerAProfileId, playerBProfileId);
    this.playerPositions = this.getInitialPlayerPositions();
  }

  public from(other: PositionsRecorder) {
    this.doubles = other.doubles
    this.letterToProfileIdMap = other.letterToProfileIdMap;
    this.playerPositions = other.playerPositions;
  }
  /**
   *
   * @param playerAProfileId
   * @param playerBProfileId
   */
  private makePlayerProfilesMap(playerAProfileId: string, playerBProfileId: string) {
    let letterToProfileIdMap = {};
    if (!this.doubles) {
      letterToProfileIdMap['A'] = playerAProfileId;
      letterToProfileIdMap['X'] = playerBProfileId;
    } else {
      const aProfileIds = playerAProfileId.split(';');
      letterToProfileIdMap['A'] = aProfileIds[0];
      letterToProfileIdMap['B'] = aProfileIds[1];
      const bProfileIds = playerBProfileId.split(';');
      letterToProfileIdMap['X'] = bProfileIds[0];
      letterToProfileIdMap['Y'] = bProfileIds[1];
    }
    return letterToProfileIdMap;
  }

  /**
   * Player positions on the table L - left, R - right
   *
   * @private
   */
  private getInitialPlayerPositions () {
    let playerPositions = {};
    if (this.doubles) {
      playerPositions['L1'] = 'B';
      playerPositions['L2'] = 'A';
      playerPositions['R1'] = 'X';
      playerPositions['R2'] = 'Y';
    } else {
      playerPositions['L1'] = '';
      playerPositions['L2'] = 'A';
      playerPositions['R1'] = 'X';
      playerPositions['R2'] = '';
    }
    return playerPositions;
  }

  public getPlayerPositions(): string {
    return this.playerPositions;
  }

  /**
   * looks up player profile by letter
   * @param letter A, B, X or Y
   */
  public lookupPlayerProfile(letter: string): string {
    return this.letterToProfileIdMap[letter];
  }

  /**
   * switches sides
   */
  public switchSides() {
    let tempPlayerPositions = {}
    if (this.doubles) {
      tempPlayerPositions['L1'] = this.playerPositions['R1'];
      tempPlayerPositions['L2'] = this.playerPositions['R2'];
      tempPlayerPositions['R1'] = this.playerPositions['L1'];
      tempPlayerPositions['R2'] = this.playerPositions['L2'];
    } else {
      tempPlayerPositions['L2'] = this.playerPositions['R1'];
      tempPlayerPositions['R1'] = this.playerPositions['L2'];
      tempPlayerPositions['L1'] = "";
      tempPlayerPositions['R2'] = "";

    }
    this.playerPositions = tempPlayerPositions;
  }

  /**
   * Switches players on either left or right side
   * @param left
   */
  public switchPlayers(left: boolean) {
    if (this.doubles) {
      let tempPlayerPositions = {};
      if (left) {
        // switch
        tempPlayerPositions['L1'] = this.playerPositions['L2'];
        tempPlayerPositions['L2'] = this.playerPositions['L1'];
        // keep the same
        tempPlayerPositions['R1'] = this.playerPositions['R1'];
        tempPlayerPositions['R2'] = this.playerPositions['R2'];
      } else {
        // keep the same
        tempPlayerPositions['L1'] = this.playerPositions['L1'];
        tempPlayerPositions['L2'] = this.playerPositions['L2'];
        // switch
        tempPlayerPositions['R1'] = this.playerPositions['R2'];
        tempPlayerPositions['R2'] = this.playerPositions['R1'];
      }
      this.playerPositions = tempPlayerPositions;
    }
  }

  /**
   *
   * @param currentServer
   * @param currentReceiver
   */
  public recordPlayerPositions(currentServer: string, currentReceiver: string) {
    if ((this.playerPositions['L2'] === currentServer   && this.playerPositions['R1'] == currentReceiver) ||
        (this.playerPositions['L2'] === currentReceiver && this.playerPositions['R1'] == currentServer)) {
      // change is not needed
    } else {
      // need to change players
      if (currentReceiver === this.playerPositions['L1']) {
        // on the left side of the table
        this.switchPlayers(true);
      } else if (currentReceiver === this.playerPositions['R2']) {
        // or the right side
        this.switchPlayers(false);
      }
    }
  }

  public getPlayerProfile (sideAndNumber: string) {
    const letter = this.playerPositions[sideAndNumber];
    return this.lookupPlayerProfile(letter);
  }

  public lookupPlayerLetter(playerProfileId: string): string {
    for (const playerLetter in this.letterToProfileIdMap) {
      if (this.letterToProfileIdMap[playerLetter] === playerProfileId) {
        return playerLetter;
      }
    }
    return '';
  }

  /**
   * Check if player with given letter is on the left side
   * @param playerLetter A, B X or Y
   */
  public isPlayerOnLeftSide (playerLetter: string): boolean {
    return (this.playerPositions['L1'] === playerLetter || this.playerPositions['L2'] === playerLetter);
  }
}
