export enum EntryType {
  INDIVIDUAL = 'INDIVIDUAL',
  FAMILY = 'FAMILY',
  GROUP = 'GROUP'
}

export enum MembershipType {
  NO_MEMBERSHIP_REQUIRED,
  TOURNAMENT_PASS,
  JUNIOR_ONE_YEAR,
  JUNIOR_THREE_YEARS,
  COLLEGIATE_ONE_YEAR,
  ADULT_ONE_YEAR,
  ADULT_THREE_YEARS,
  ADULT_FIVE_YEARS,
  HOUSEHOLD_ONE_YEAR,
  LIFETIME
  // NO_MEMBERSHIP_REQUIRED = 'NO_MEMBERSHIP_REQUIRED',
  // TOURNAMENT_PASS = 'TOURNAMENT_PASS',
  // JUNIOR_ONE_YEAR = 'JUNIOR_ONE_YEAR',
  // JUNIOR_THREE_YEARS = 'JUNIOR_THREE_YEARS',
  // COLLEGIATE_ONE_YEAR = 'COLLEGIATE_ONE_YEAR',
  // ADULT_ONE_YEAR = 'ADULT_ONE_YEAR',
  // ADULT_THREE_YEARS = 'ADULT_THREE_YEARS',
  // ADULT_FIVE_YEARS = 'ADULT_FIVE_YEARS',
  // HOUSEHOLD_ONE_YEAR = 'HOUSEHOLD_ONE_YEAR',
  // LIFETIME = 'LIFETIME'
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
  membershipOption: MembershipType = MembershipType.TOURNAMENT_PASS;

  // profile id of the player who owns this entry
  profileId: string;

  entryType: EntryType = EntryType.INDIVIDUAL;
}
