import { Pipe, PipeTransform } from '@angular/core';
import {Ranks} from '../model/ranks';
import {UmpireRank} from '../model/umpire-rank.enum';

@Pipe({
    name: 'umpireRank',
    standalone: false
})
export class UmpireRankPipe implements PipeTransform {

  private umpireRanks: any [] = Ranks.getUmpireRanks();

  transform(rank: UmpireRank, ...args: unknown[]): string {
    for (const umpireRankObj of this.umpireRanks) {
      if (rank === umpireRankObj.rank) {
        const style = (args.length > 0) ? args[0] : 'abbrev';
        if (style === 'abbrev') {
          return umpireRankObj.abbreviation;
        } else if (style === 'name') {
          return umpireRankObj.rankName;
        } else if (style === 'combined') {
          return `(${umpireRankObj.abbreviation}) ${umpireRankObj.rankName}`
        } else {
          return umpireRankObj.abbreviation;
        }
      }
    }
    return rank;
  }
}
