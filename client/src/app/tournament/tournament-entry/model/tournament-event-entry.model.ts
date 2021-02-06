export enum EventEntryStatus {
 NOT_ENTERED = 'NOT_ENTERED',
 CONFIRMED = 'CONFIRMED',
 PENDING_CONFIRMATION = 'PENDING_CONFIRMATION',
 PENDING_DELETION = 'PENDING_DELETION',
 WAITING_LIST = 'WAITING_LIST',
 ENTERED_WAITING_LIST = 'ENTERED_WAITING_LIST',
 DISQUALIFIED_RATING = 'DISQUALIFIED_RATING',
 DISQUALIFIED_AGE = 'DISQUALIFIED_AGE',
 DISQUALIFIED_GENDER = 'DISQUALIFIED_GENDER',
 DISQUALIFIED_TIME_CONFLICT = 'DISQUALIFIED_TIME_CONFLICT'
}

export class TournamentEventEntry {
  id: number;

  tournamentFk: number;

  tournamentEntryFk: number;

  tournamentEventFk: number;

  dateEntered: Date;

  // status of event entry
  status: EventEntryStatus = EventEntryStatus.NOT_ENTERED;

  // session id for deleting
  entrySessionId: string;
}
