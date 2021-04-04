/**
 * Minimal information about the player entry into the tournament to be displayed in a list of participants
 */
export class TournamentEntryInfo {
  profileId: string;

  // player name
  firstName: string;
  lastName: string;

  // rating as of eligibility date
  eligibilityRating: number;

  // current rating
  seedRating: number;

  // ids of events player entered
  eventIds: number [];
}
