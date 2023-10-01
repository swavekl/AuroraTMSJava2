export enum ConflictType {
  NO_CONFLICT= 'NO_CONFLICT',

  SCORES_ENTERED = 'SCORES_ENTERED',


  // This player lives near another player in the group, possibly playing them often.
  LIVES_NEARBY = 'LIVES_NEARBY',

  //  This player is in a first round match with another player in the group in another event.
  PLAYS_IN_OTHER_EVENT_FIRST_ROUND = 'PLAYS_IN_OTHER_EVENT_FIRST_ROUND',

  // This player is in the same club as another player in the group in the first round
  SAME_CLUB_FIRST_ROUND = 'SAME_CLUB_FIRST_ROUND',

  // Two players from the same club might meet in the 2nd round
  SAME_CLUB_SECOND_ROUND = 'SAME_CLUB_SECOND_ROUND'
}
