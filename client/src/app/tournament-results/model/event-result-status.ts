export class EventResultStatus {
  eventId: number;
  eventName: string;
  resultsAvailable: boolean;

  // names of players (or doubles teams partners) who took 1st, 2nd 3rd and 4th place
  firstPlacePlayer: string;
  secondPlacePlayer: string;
  thirdPlacePlayer: string;
  fourthPlacePlayer: string;

  // indicates if a match for 3rd and 4th place is to be played
  play3rd4thPlace: boolean;
}
