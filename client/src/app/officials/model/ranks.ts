import {UmpireRank} from './umpire-rank.enum';
import {RefereeRank} from './referee-rank.enum';

export class Ranks {
  private static umpireRanks: any [] = [
    {rank: UmpireRank.ClubUmpire, abbreviation: 'CU', rankName: 'Club Umpire'},
    {rank: UmpireRank.RegionalUmpire, abbreviation: 'RU', rankName: 'Regional Umpire'},
    {rank: UmpireRank.NationalUmpire, abbreviation: 'NU', rankName: 'National Umpire'},
    {rank: UmpireRank.InternationalUmpire, abbreviation: 'IU', rankName: 'International Umpire'}
  ];

  private static refereeRanks: any [] = [
    {rank: RefereeRank.CertifiedReferee, abbreviation: 'CR', rankName: 'Certified Referee'},
    {rank: RefereeRank.RegionalReferee, abbreviation: 'RR', rankName: 'Regional Referee'},
    {rank: RefereeRank.NationalReferee, abbreviation: 'NR', rankName: 'National Referee'},
    {rank: RefereeRank.InternationalReferee, abbreviation: 'IR', rankName: 'International Referee'}
  ];

  public static getUmpireRanks(): any[] {
    return this.umpireRanks;
  }

  public static getRefereeRanks(): any[] {
    return this.refereeRanks;
  }
}
