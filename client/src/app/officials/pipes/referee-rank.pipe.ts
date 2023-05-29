import { Pipe, PipeTransform } from '@angular/core';
import {Ranks} from '../model/ranks';
import {RefereeRank} from '../model/referee-rank.enum';

@Pipe({
  name: 'refereeRank'
})
export class RefereeRankPipe implements PipeTransform {

  private refereeRanks: any [] = Ranks.getRefereeRanks();

  transform(rank: RefereeRank, ...args: unknown[]): string {
    for (const refereeRankObj of this.refereeRanks) {
      if (rank === refereeRankObj.rank) {
        const style = (args.length > 0) ? args[0] : 'abbrev';
        if (style === 'abbrev') {
          return refereeRankObj.abbreviation;
        } else if (style === 'name') {
          return refereeRankObj.rankName;
        } else if (style === 'combined') {
          return `(${refereeRankObj.abbreviation}) ${refereeRankObj.rankName}`
        } else {
          return refereeRankObj.abbreviation;
        }
      }
    }
    return rank;
  }
}
