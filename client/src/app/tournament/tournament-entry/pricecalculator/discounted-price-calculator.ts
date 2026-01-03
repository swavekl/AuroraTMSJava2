import {AbstractPriceCalculator} from './abstract-price-calculator';
import {PriceCalculator} from './price-calculator';
import {MembershipType} from '../model/tournament-entry.model';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {Team} from '../model/team.model';

export class DiscountedPriceCalculator extends AbstractPriceCalculator implements PriceCalculator {

  // todo - implement
    getTotalPrice(membershipOption: MembershipType, usattDonation: number, enteredEvents: TournamentEventEntryInfo[],
                  teams: Team[], isWithdrawing: boolean, availableEvents: TournamentEventEntryInfo[], tournamentEntryId: number): number {
    return 0;
  }

}
