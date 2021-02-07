import {GenderRestriction} from './model/gender-restriction.enum';

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

  // flag indicating if event has any gender restrictions (men's or women's only event)
  genderRestriction: GenderRestriction = GenderRestriction.NONE;

  // round robin options
  playersPerGroup: number;
  drawMethod: number;

  // number of tables per group
  numTablesPerGroup: number;

  // best of 3, 5, 7 or 9 games per match
  numberOfGames: number;

  // number of players to advance, 0, 1 or 2
  playersToAdvance: number;

  // number of players to seed directly into next round
  playersToSeed: number;

  // fees
  feeAdult: number;
  feeJunior: number;

  static convert(tournamentEvent: TournamentEvent): TournamentEvent {
    return tournamentEvent;
  }

  static toTournamentEvent(formValues: any): TournamentEvent {
    const tournamentEvent = new TournamentEvent();
    Object.assign(tournamentEvent, formValues);
    return tournamentEvent;
  }

  static fromDefaults(tournamentId: number, selectedEvent: any): TournamentEvent {
    const tournamentEvent = this.toTournamentEvent(selectedEvent);
    tournamentEvent.id = null;
    tournamentEvent.tournamentFk = tournamentId;
    tournamentEvent.singleElimination = false;
    tournamentEvent.day = 1;
    tournamentEvent.startTime = 9.0;
    // tournamentEvent.genderRestriction = GenderRestriction[selectedEvent.genderRestriction];
    return tournamentEvent;
  }
}
