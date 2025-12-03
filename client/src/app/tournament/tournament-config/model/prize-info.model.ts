/**
 * Class holding information about the prize money and/or trophies awarded for each place
 */
export class PrizeInfo {

  // if draw type is division (e.e. it is a giant round robin event)
  // each group of 8 players designates a division for which this prize info is configured e.g.
  // Division 1 (or A) fist place winner gets $150, but division 2 (or B) gets only $100
  // for snake draw type this is ignored i.e. 0
  division: string;

  // place for which this prize is awarded
  awardedForPlace: number;

  // if prizes are awarded for range of places e.g. 3 - 4, 5 -8, this will be the range end
  // if it is not a range it will be the same as the awardedForPlace value
  awardedForPlaceRangeEnd: number;

  // prize money awarded or null if nothing
  prizeMoneyAmount: number;

  // if true trophy is awarded, see type below
  awardTrophy: boolean;

  readonly AWARD_TYPE_NONE = "None";
  readonly AWARD_TYPE_TROPHY = "Trophy";
  readonly AWARD_TYPE_MEDAL = "Medal";
  awardType : string;

}
