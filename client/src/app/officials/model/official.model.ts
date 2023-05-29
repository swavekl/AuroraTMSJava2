import {UmpireRank} from './umpire-rank.enum';
import {RefereeRank} from './referee-rank.enum';

export class Official {
  id: number;

  firstName: string;
  lastName: string;

  // Okta profile id
  profileId: string;

  // umpire and referee rank of this official
  umpireRank: UmpireRank;
  refereeRank: RefereeRank;

  // umpire and referee number - international ?
  umpireNumber: number;
  refereeNumber: number;

  // wheelchair certification ? e.g. IPTTC or number
  wheelchair: string;

  // USATT membership id of this official, fetched from userprofileext
  membershipId: number;

  state: string;

}
