export class Team {
  // team id
  id: number;

  // teams event which this is team entered
  tournamentEventFk: number;

  // team name
  name: string;

  // team rating
  rating: number;

  // indicates the team captain's profile id
  captainProfileId: string;
}
