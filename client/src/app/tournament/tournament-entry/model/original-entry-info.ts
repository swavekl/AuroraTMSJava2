import {MembershipType} from './tournament-entry.model';

/**
 * Class representing information needed to restore entry to its previous state
 */
export class OriginalEntryInfo {
  // tournament entry id
  entryId: number;

  // cart session with which the modified entries are associated
  cartSessionId: string;

  // if true we are withdrawing entirely from the tournament
  withdrawing: boolean;

  // original membership type
  membershipType: MembershipType;

  // original USATT donation
  usattDonation: number;

  doublesEventToPartnerMap: any;
}
