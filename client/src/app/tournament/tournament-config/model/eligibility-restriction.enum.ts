// Who can play in the tournament
export enum EligibilityRestriction {
  // open to all players
  OPEN = 'OPEN',
  // closed to players from outside the state where the venue is located e.g. Illinois
  CLOSED_STATE = "CLOSED_STATE",
  // closed to players from outside the region e.g. Midwest
  CLOSED_REGIONAL = "CLOSED_REGIONAL",
  // closed to players from outside the Nation e.g. USA
  CLOSED_NATIONAL = "CLOSED_NATIONAL",
}
