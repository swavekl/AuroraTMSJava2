import {TeamRatingCalculator} from './team-rating-calculator';
import {TeamRatingCalculationMethod} from '../tournament-config/model/team-rating-calculation-method';
import {PositionsRecorder} from '../../shared/serve-order/positions-recorder';

describe('TeamRatingCalculator', () => {
  it('should create an instance', () => {
    const teamRatingCalculator = new TeamRatingCalculator(TeamRatingCalculationMethod.AVERAGE_ALL_PLAYERS);
    expect(teamRatingCalculator).toBeTruthy();
  });

  it('should calculate average', () => {
    const teamRatingCalculator = new TeamRatingCalculator(TeamRatingCalculationMethod.AVERAGE_ALL_PLAYERS);
    expect(teamRatingCalculator).toBeTruthy();
    const memberRatings: number [] = [1300, 1200, 1400];
    const teamRating = teamRatingCalculator.calculateRating(memberRatings);
    expect(teamRating).toEqual(1300);
  });

  it('should calculate sum top 2', () => {
    const teamRatingCalculator = new TeamRatingCalculator(TeamRatingCalculationMethod.SUM_TOP_TWO);
    expect(teamRatingCalculator).toBeTruthy();
    const memberRatings: number [] = [1300, 1200, 1400];
    const teamRating = teamRatingCalculator.calculateRating(memberRatings);
    expect(teamRating).toEqual(2700);

    const memberRatings2: number [] = [1200, 1400];
    const teamRating2 = teamRatingCalculator.calculateRating(memberRatings2);
    expect(teamRating2).toEqual(2600);

    const memberRatings1: number [] = [1400];
    const teamRating1 = teamRatingCalculator.calculateRating(memberRatings1);
    expect(teamRating1).toEqual(1400);

    const memberRatings0: number [] = [];
    const teamRating0 = teamRatingCalculator.calculateRating(memberRatings0);
    expect(teamRating0).toEqual(0);
  });

  it('should calculate sum top 3', () => {
    const teamRatingCalculator = new TeamRatingCalculator(TeamRatingCalculationMethod.SUM_TOP_THREE);
    expect(teamRatingCalculator).toBeTruthy();
    const memberRatings: number [] = [1300, 1200, 1400];
    const teamRating = teamRatingCalculator.calculateRating(memberRatings);
    expect(teamRating).toEqual(3900);

    const memberRatings4: number [] = [1300, 1200, 1400, 1500];
    const teamRating4 = teamRatingCalculator.calculateRating(memberRatings4);
    expect(teamRating4).toEqual(4200);

    const memberRatings5: number [] = [1300, 1200, 1400, 1500, 1600];
    const teamRating5 = teamRatingCalculator.calculateRating(memberRatings5);
    expect(teamRating5).toEqual(4500);

    const memberRatings2: number [] = [1200, 1400];
    const teamRating2 = teamRatingCalculator.calculateRating(memberRatings2);
    expect(teamRating2).toEqual(2600);

    const memberRatings1: number [] = [1400];
    const teamRating1 = teamRatingCalculator.calculateRating(memberRatings1);
    expect(teamRating1).toEqual(1400);

    const memberRatings0: number [] = [];
    const teamRating0 = teamRatingCalculator.calculateRating(memberRatings0);
    expect(teamRating0).toEqual(0);
  });
});
