export enum EntryType {
  INDIVIDUAL = 'INDIVIDUAL',
  FAMILY = 'FAMILY',
  GROUP = 'GROUP'
}

export enum MembershipType {
  NO_MEMBERSHIP_REQUIRED,
  TOURNAMENT_PASS_JUNIOR,
  TOURNAMENT_PASS_ADULT,
  BASIC_PLAN,
  PRO_PLAN,
  LIFETIME
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
  membershipOption: MembershipType = MembershipType.TOURNAMENT_PASS_ADULT;

  // profile id of the player who owns this entry
  profileId: string;

  entryType: EntryType = EntryType.INDIVIDUAL;

  // if family or group entry, will contain owning entry fk
  owningTournamentEntryFk: number;

  // mandatory usattDonation line - optional for user
  usattDonation: number;

}
