import {GenderRestriction} from './model/gender-restriction.enum';
import {AgeRestrictionType} from './model/age-restriction-type.enum';
import {DrawMethod} from './model/draw-method.enum';
import {TournamentEventConfiguration} from './model/tournament-event-configuration.model';
import {EligibilityRestriction} from './model/eligibility-restriction.enum';

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

  // fees
  feeAdult: number;
  feeJunior: number;

  // if true match scores were entered for this event so redoing draws should be prohibited
  matchScoresEntered: boolean;

  configuration: TournamentEventConfiguration;

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
      configuration: tournamentEvent.configuration || new TournamentEventConfiguration()
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
    return tournamentEvent;
  }
}
