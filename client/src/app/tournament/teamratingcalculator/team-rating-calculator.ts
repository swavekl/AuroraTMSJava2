import {TeamRatingCalculationMethod} from '../tournament-config/model/team-rating-calculation-method';

export class TeamRatingCalculator {

  constructor(private teamRatingCalculationMethod: TeamRatingCalculationMethod) {
  }

  public calculateRating (memberRatings: number []): number {
    let teamRating = 0;
    const sortedMemberRatings = memberRatings.sort((t1, t2) => t1 > t2 ? -1 : 1);
    switch (this.teamRatingCalculationMethod) {
      case TeamRatingCalculationMethod.AVERAGE_ALL_PLAYERS:
        teamRating = this.average(sortedMemberRatings);
        break;

      case TeamRatingCalculationMethod.SUM_TOP_TWO:
        teamRating = this.sumTop(sortedMemberRatings, 2);
        break;

      case TeamRatingCalculationMethod.SUM_TOP_THREE:
        teamRating = this.sumTop(sortedMemberRatings, 3);
        break;

      case TeamRatingCalculationMethod.NA_JOOLA_TEAMS:
        teamRating = this.basedOnTeamSize(sortedMemberRatings);
        break;
    }

    return teamRating;
  }

  private average(sortedMemberRatings: number[]) {
    let sumRatings: number = 0;
    for (let i = 0; i < sortedMemberRatings.length; i++) {
      sumRatings += sortedMemberRatings[i];
    }
    if (sortedMemberRatings.length > 0) {
      return Math.floor(sumRatings / sortedMemberRatings.length);
    }
    return 0;
  }

  private sumTop(memberRatings: number [], topN: number): number {
    let sum = 0;
    memberRatings.forEach((memberRating, index) => {
      sum += (index < topN) ? memberRating : 0;
    });
    return sum;
  }

  private basedOnTeamSize(sortedMemberRatings: number[]) {
    let teamRating = 0;
    if (sortedMemberRatings.length === 5) {

    } else if (sortedMemberRatings.length === 4) {

    } else if (sortedMemberRatings.length <= 3) {

    }
    return teamRating;
  }
}
