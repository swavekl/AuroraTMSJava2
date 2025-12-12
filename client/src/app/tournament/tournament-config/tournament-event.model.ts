import {GenderRestriction} from './model/gender-restriction.enum';
import {AgeRestrictionType} from './model/age-restriction-type.enum';
import {DrawMethod} from './model/draw-method.enum';
import {TournamentEventConfiguration} from './model/tournament-event-configuration.model';
import {EligibilityRestriction} from './model/eligibility-restriction.enum';
import {EventEntryType} from './model/event-entry-type.enum';
import {TournamentEventRound} from './model/tournament-event-round.model';
import {TournamentEventRoundDivision} from './model/tournament-event-round-division.model';
import {TournamentRoundsConfiguration} from './model/tournament-rounds-configuration.model';
import {FeeStructure} from './model/fee-structure.enum';
import {FeeScheduleItem} from './model/fee-schedule-item';
import {TeamRatingCalculationMethod} from './model/team-rating-calculation-method';

/**
 * Tournament event e.g. U-2200, Giant Round Robin etc.
 */
export class TournamentEvent {
  id: number;

  // name e.g. U-2200, Open Singles, Under 17, Over 40 etc.
  name: string;

  // foreign key back to tournament
  tournamentFk: number;

  // event number listed on blank entry form (used for sorting)
  ordinalNumber: number;

  // day of the tournament on which this event is played 1, 2, 3 etc
  day: number;

  // fractional start time e.g. 9.5 = 9:30 am, 17.0 = 5:00 pm, -1.0 = To be Determined
  startTime: number;

  // single elimination (true), round robin (false)
  singleElimination: boolean;

  // doubles (true) or singles (false)
  doubles: boolean;

  // determines how we sign up for it - individually or as a team
  // must be at event level because some tournament offer team and individual team events
  eventEntryType: EventEntryType = EventEntryType.INDIVIDUAL;

  // maximum entries, 0 if no limit
  maxEntries: number;
  // current number of entries
  numEntries: number;

  minPlayerRating: number;
  maxPlayerRating: number;

  minPlayerAge: number;
  maxPlayerAge: number;

  ageRestrictionType: AgeRestrictionType = AgeRestrictionType.NONE;
  ageRestrictionDate: Date;

  // flag indicating if event has any gender restrictions (men's or women's only event)
  genderRestriction: GenderRestriction = GenderRestriction.NONE;

  // who can play in the tournament
  eligibilityRestriction: EligibilityRestriction = EligibilityRestriction.OPEN;

  // round robin options
  playersPerGroup: number;

  // draw method
  drawMethod: DrawMethod;

  // number of tables per group
  numTablesPerGroup: number;

  // points per game - 11 but sometimes 21
  pointsPerGame: number;

  // best of 3, 5, 7 or 9 games per match
  numberOfGames: number;

  // in single elimination round or if event is a single elimination only
  // number of games in rounds prior to quarter finals e.g. 5
  numberOfGamesSEPlayoffs: number;

  // number of games in quarter, semi finals and 3rd/4th place matches
  numberOfGamesSEQuarterFinals: number;
  numberOfGamesSESemiFinals: number;
  numberOfGamesSEFinals: number;

  // indicates if a match for 3rd adn 4th place is to be played
  play3rd4thPlace: boolean;

  // number of players to advance, 0, 1 or 2
  playersToAdvance: number;

  // if this event advances player to another event or round - indicate if unrated players are to be advanced
  // typically not but in Open Singles they usually are
  advanceUnratedWinner: boolean;

  // number of players to seed directly into next round
  playersToSeed: number;

  // method of calculating a fee for event entry
  feeStructure: FeeStructure = FeeStructure.FIXED;

  // individual fees
  feeAdult: number;
  feeJunior: number;

  // fixed team fees
  perTeamFee: number;
  perPlayerFee: number;

  // fee schedule items with progressively more expensive as event date nears
  feeScheduleItems: FeeScheduleItem[] = [];

  // applies to all tournaments i.e. withdrawal penalty
  cancellationFee: number;

  // team size is a range 2 to 3, or 3 to  5.
  minTeamPlayers: number;
  maxTeamPlayers: number;

  // method used to calculate team rating
  teamRatingCalculationMethod: TeamRatingCalculationMethod = TeamRatingCalculationMethod.SUM_TOP_TWO;

  // if true match scores were entered for this event so redoing draws should be prohibited
  matchScoresEntered: boolean;

  configuration: TournamentEventConfiguration;

  roundsConfiguration: TournamentRoundsConfiguration;

  static convert(tournamentEvent: TournamentEvent): TournamentEvent {
    return tournamentEvent;
  }

  static toTournamentEvent(formValues: any): TournamentEvent {
    const tournamentEvent = new TournamentEvent();
    Object.assign(tournamentEvent, formValues);
    const eventWithDefaults = {
      ...tournamentEvent,
      numberOfGames: tournamentEvent.numberOfGames || 5,
      numberOfGamesSEPlayoffs: tournamentEvent.numberOfGamesSEPlayoffs || 5,
      numberOfGamesSEQuarterFinals: tournamentEvent.numberOfGamesSEQuarterFinals || 5,
      numberOfGamesSESemiFinals: tournamentEvent.numberOfGamesSESemiFinals || 5,
      numberOfGamesSEFinals: tournamentEvent.numberOfGamesSEFinals || 5,
      advanceUnratedWinner: tournamentEvent.advanceUnratedWinner || false,
      pointsPerGame: tournamentEvent.pointsPerGame || 11,
      play3rd4thPlace: tournamentEvent.play3rd4thPlace || false,
      eligibilityRestriction: tournamentEvent.eligibilityRestriction || EligibilityRestriction.OPEN,
      configuration: tournamentEvent.configuration || new TournamentEventConfiguration(),
      roundsConfiguration: tournamentEvent.roundsConfiguration || new TournamentRoundsConfiguration()
    };
    return eventWithDefaults;
  }

  static fromDefaults(tournamentId: number, selectedEvent: any): TournamentEvent {
    const tournamentEvent = this.toTournamentEvent(selectedEvent);
    tournamentEvent.id = null;
    tournamentEvent.tournamentFk = tournamentId;
    tournamentEvent.singleElimination = false;
    tournamentEvent.day = 1;
    tournamentEvent.startTime = 9.0;
    tournamentEvent.playersPerGroup = (selectedEvent.playersPerGroup == null) ? 4 : selectedEvent.playersPerGroup;
    tournamentEvent.drawMethod = (selectedEvent.drawMethod == null) ? DrawMethod.SNAKE : selectedEvent.drawMethod;
    tournamentEvent.eventEntryType = (selectedEvent.eventEntryType == null) ? EventEntryType.INDIVIDUAL : selectedEvent.eventEntryType;
    tournamentEvent.numTablesPerGroup = 1;
    tournamentEvent.pointsPerGame = 11;
    tournamentEvent.numberOfGames = 5;
    tournamentEvent.numberOfGamesSEPlayoffs = 5;
    tournamentEvent.numberOfGamesSEQuarterFinals = 5;
    tournamentEvent.numberOfGamesSESemiFinals = 5;
    tournamentEvent.numberOfGamesSEFinals = 5;
    tournamentEvent.play3rd4thPlace = false;
    tournamentEvent.playersToAdvance = (selectedEvent.playersToAdvance == null) ? 1 : selectedEvent.playersToAdvance;
    tournamentEvent.advanceUnratedWinner = false;
    tournamentEvent.playersToSeed = 0;
    tournamentEvent.feeAdult = 30;
    tournamentEvent.feeJunior = 30;
    tournamentEvent.maxEntries = 32;
    tournamentEvent.matchScoresEntered = false;
    if (selectedEvent.maxPlayerAge > 0) {
      tournamentEvent.ageRestrictionType = AgeRestrictionType.AGE_UNDER_OR_EQUAL_ON_DAY_EVENT;
    } else if (selectedEvent.minPlayerAge > 0) {
      tournamentEvent.ageRestrictionType = AgeRestrictionType.AGE_OVER_AT_THE_END_OF_YEAR;
    } else {
      tournamentEvent.ageRestrictionType = AgeRestrictionType.NONE;
    }
    tournamentEvent.eligibilityRestriction = EligibilityRestriction.OPEN;
    // tournamentEvent.genderRestriction = GenderRestriction[selectedEvent.genderRestriction];
    tournamentEvent.configuration = new TournamentEventConfiguration();
    tournamentEvent.configuration.prizeInfoList = [];
    tournamentEvent.roundsConfiguration = new TournamentRoundsConfiguration();
    tournamentEvent.roundsConfiguration.rounds = TournamentEvent.formRoundsConfiguration(
      tournamentEvent.configuration, false, selectedEvent);
    return tournamentEvent;
  }

  private static formRoundsConfiguration (configuration: TournamentEventConfiguration,
                                  singleElimination: boolean,
                                  selectedEvent: TournamentEvent): TournamentEventRound [] {
    const rounds: TournamentEventRound [] = [];
    let ordinalNum = 0;
    if (!singleElimination) {
      const drawMethod: DrawMethod = (selectedEvent.drawMethod == null) ? DrawMethod.SNAKE : selectedEvent.drawMethod;
      let rrRound = this.makeRound('Round Robin', selectedEvent, drawMethod, "");
      rrRound.ordinalNum = ++ordinalNum;
      rrRound.divisions[0].divisionName = "Round Robin";
      rrRound.divisions[0].previousRoundPlayerRanking = 0;
      rrRound.divisions[0].previousRoundPlayerRankingEnd = 0;
      rounds.push(rrRound);
    }

    // single elimination round follows round robin round
    if (selectedEvent.drawMethod != DrawMethod.DIVISION) {
      let seRound = this.makeRound('Single Elimination', selectedEvent, DrawMethod.SINGLE_ELIMINATION);
      seRound.ordinalNum = ++ordinalNum;
      seRound.divisions[0].divisionName = "Single Elimination";
      seRound.divisions[0].playersPerGroup = 1;
      seRound.divisions[0].previousRoundPlayerRanking = 1;
      seRound.divisions[0].previousRoundPlayerRankingEnd = 1;
      rounds.push(seRound);
      seRound.startTime = 12.0;
    }
    return rounds;
  }

  public static makeRound(roundName: string, selectedEvent: TournamentEvent, drawMethod: DrawMethod, divisionName: string = 'Championship') {
    let round: TournamentEventRound = new TournamentEventRound();
    round.roundName = roundName;
    round.day = 1;
    round.startTime = 9.0;
    round.divisions = [];
    let division: TournamentEventRoundDivision = new TournamentEventRoundDivision();
    round.divisions.push(division);
    division.divisionName = divisionName;
    division.playersPerGroup = (selectedEvent?.playersPerGroup == null) ? 4 : selectedEvent.playersPerGroup;
    division.drawMethod = drawMethod;
    division.playersToAdvance = (selectedEvent?.playersToAdvance == null) ? 1 : selectedEvent.playersToAdvance;
    return round;
  }
}
