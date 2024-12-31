import {MembershipType} from '../../tournament/tournament-entry/model/tournament-entry.model';

export class MembershipInfo {
  playerName: string;
  profileId: string;
  expirationDate: Date;
  membershipType: MembershipType;
  membershipId: number;
  entryId: number;
}
