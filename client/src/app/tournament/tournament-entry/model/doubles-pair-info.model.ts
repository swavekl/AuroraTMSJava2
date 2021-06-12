import {DoublesPair} from './doubles-pair.model';

/**
 * Info about doubles pairs
 */
export class DoublesPairInfo {

  id: number;

  doublesPair: DoublesPair;

  playerAProfileId: string;
  playerBProfileId: string;

  playerAName: string;
  playerBName: string;

  playerAEligibilityRating: number;
  playerBEligibilityRating: number;

  playerASeedRating: number;
  playerBSeedRating: number;
}
