enum EntryType {
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

  tournamentId: number;

  type: EntryType = EntryType.INDIVIDUAL;

  membership: MembershipType = MembershipType.INDIVIDUAL_ONE_YEAR;
}
