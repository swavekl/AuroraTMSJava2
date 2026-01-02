import {TeamEntryStatus} from './team-entry-status.enum';

export class TeamMember {

  id: number;

  teamFk: number;

  // member's profile id
  profileId: string;

  // if true then member is a captain
  isCaptain: boolean;

  status: TeamEntryStatus;

  tournamentEntryFk: number;

  tournamentEventFk: number;

  cartSessionId: string;

  playerName: string;

  playerRating: number;
}
