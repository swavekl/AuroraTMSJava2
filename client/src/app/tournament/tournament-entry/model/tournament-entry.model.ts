export enum EntryType {
  INDIVIDUAL,
  FAMILY,
  GROUP
}

enum MembershipType {
  NO_MEMBERSHIP_REQUIRED,
  INDIVIDUAL_ONE_YEAR,
  INDIVIDUAL_THREE_YEARS,
  INDIVIDUAL_FIVE_YEARS
}

export class TournamentEntry {
  id: number;

  tournamentFk: number;

  // date user entered the tournament
  dateEntered: Date;

  // rating as of the Tournament.ratingCutoffDate date  it may go up after that date but will not effect eligibility for entered events
  // can be null i.e. unrated
  eligibilityRating: number;

  // current rating used for seeding within events
  seedRating: number;

  // selected USATT membership option (from 1 through 8)
  membershipOption: MembershipType = MembershipType.INDIVIDUAL_ONE_YEAR;

  // profile id of the player who owns this entry
  profileId: string;

  type: EntryType = EntryType.INDIVIDUAL;
}
