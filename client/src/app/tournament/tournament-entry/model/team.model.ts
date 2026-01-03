import { TeamMember } from './team-member.model';

export class Team {
  // team id
  id: number;

  // teams event which this is team entered
  tournamentEventFk: number;

  // team name
  name: string;

  // team rating
  teamRating: number;

  dateEntered: Date;

  // price paid in case it changes according to schedule
  entryPricePaid: number;

  // list of team members of this team
  teamMembers: TeamMember[] = [];

  cartSessionId: string;
}
