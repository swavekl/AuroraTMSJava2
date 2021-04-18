import {AbstractPriceCalculator} from './abstract-price-calculator';
import {PriceCalculator} from './price-calculator';
import {MembershipType} from '../model/tournament-entry.model';

export class DiscountedPriceCalculator extends AbstractPriceCalculator implements PriceCalculator {

  // todo - implement
  getTotalPrice(membershipOption: MembershipType, usattDonation: number, enteredEvents: any[]): number {
    return 0;
  }

}
