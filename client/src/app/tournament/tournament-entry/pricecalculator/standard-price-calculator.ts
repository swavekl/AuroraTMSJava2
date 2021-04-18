import {PriceCalculator} from './price-calculator';
import {AbstractPriceCalculator} from './abstract-price-calculator';
import {EventEntryStatus} from '../model/event-entry-status.enum';
import {MembershipType} from '../model/tournament-entry.model';
import {SummaryReportItem} from './summary-report.model';

export class StandardPriceCalculator extends AbstractPriceCalculator implements PriceCalculator {

  getTotalPrice(membershipOption: MembershipType, usattDonation: number, enteredEvents: any[]): number {
    let total = 0;
    this.initiateReport();
    for (let i = 0; i < this.membershipOptions.length; i++) {
      const option = this.membershipOptions[i];
      if (option.value === membershipOption) {
        total += option.cost;
        this.addMembershipOptionLine(option);
        break;
      }
    }
    this.addEventsHeader();
    // add for those events that were entered in this session and subtract for those that were dropped
    for (let i = 0; i < enteredEvents.length; i++) {
      const enteredEvent = enteredEvents[i];
      if (enteredEvent.status === EventEntryStatus.PENDING_CONFIRMATION ||
        enteredEvent.status === EventEntryStatus.ENTERED) {
        total += enteredEvent.price;
        this.addEvent(enteredEvent);
      }
    }

    // waited events
    this.addWaitedEventsHeader();
    for (let i = 0; i < enteredEvents.length; i++) {
      const enteredEvent = enteredEvents[i];
      if (enteredEvent.status === EventEntryStatus.ENTERED_WAITING_LIST ||
        enteredEvent.status === EventEntryStatus.PENDING_WAITING_LIST) {
        this.addWaitedEvent(enteredEvent);
      }
    }

    // various fees
    this.addFeesSection();

    total += usattDonation;
    this.addUsattDonation(usattDonation);

    if (total > 0) {
      // add registration fee
      total += this.registrationFee;
      this.addRegistrationFeeLine();

      if (this.isLateEntry && this.lateEntryFee > 0) {
        total += this.lateEntryFee;
        this.addLateEntryFeeLine();
      }
    }

    return total;
  }
}
