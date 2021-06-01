/**
 * Represents information about doubles pair
 */
export class DoublesPair {

  // unique id
  id: number;

  // event id for which this doubles pair is created
  tournamentEventFk: number;

  // event entry id for player A
  playerAEventEntryFk: number;

  // event entry id for player B (if null the profileB indicates which player is requested)
  playerBEventEntryFk: number;

  // team eligibility rating (rating on a particular cut off date)
  eligibilityRating: number;

  // team seed rating (rating used for making draws)
  seedRating: number;
}
