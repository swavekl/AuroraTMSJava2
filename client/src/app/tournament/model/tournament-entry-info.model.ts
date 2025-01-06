/**
 * Minimal information about the player entry into the tournament to be displayed in a list of participants
 */
export class TournamentEntryInfo {
  // tournament entry id
  entryId: number;

  profileId: string;

  // player name
  firstName: string;
  lastName: string;

  // player gender
  gender: string;

  // rating as of eligibility date
  eligibilityRating: number;

  // current rating
  seedRating: number;

  // ids of events player entered
  eventIds: number [];

  // ids of events player is waiting on
  waitingListEventIds: number [];

  // date when entered this event waiting list
  waitingListEnteredDates: Date [];

  // ids of events in pending confirmation state
  pendingEventIds: number [];

  clubName: string;

  state: string;
}
