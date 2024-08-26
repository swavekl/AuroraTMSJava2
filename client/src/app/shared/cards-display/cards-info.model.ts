/**
 * The cards issued to player.  This class is used for storing as JSON
 */
export class CardsInfo {

  // cards issued to player
  timeoutCard: boolean = false;

  yellowCard: boolean = false;

  yellowAndRed1: boolean = false;

  yellowAndRed2: boolean = false;

  redCard: boolean = false;

  // yellow card issued to a coach
  yellowCardForCoach: boolean = false;

  // red card issued to a coach
  redCardForCoach: boolean = false;

  public toJson () {
    return JSON.stringify(this);
  }

  public fromJson(cardsJSON: string) {
    if (cardsJSON != null && cardsJSON != '') {
      const parsed = JSON.parse(cardsJSON);
      this.timeoutCard = parsed.timeoutCard;
      this.yellowCard = parsed.yellowCard;
      this.yellowAndRed1 = parsed.yellowAndRed1;
      this.yellowAndRed2 = parsed.yellowAndRed2;
      this.redCard = parsed.redCard;
      this.redCardForCoach = parsed.redCardForCoach;
      this.yellowCardForCoach = parsed.yellowCardForCoach;
    }
  }
}
