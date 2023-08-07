export class Audit {
  // unique id
  id: number;

  // profile id of user who generated this event
  profileId: string;

  // date and time when this event occurred
  eventTimestamp: Date;

  // type of event e.g. score entry, entry or withdrawal into/from tournament
  eventType: string;

  // some combination of ids which group several events together e.g. matchCardId-matchId
  eventIdentifier: string;

  // JSON string representing details of this event
  detailsJSON: string;
}
