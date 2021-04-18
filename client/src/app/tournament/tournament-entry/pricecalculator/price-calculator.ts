import {MembershipType} from '../model/tournament-entry.model';
import {SummaryReportItem} from './summary-report.model';

export interface PriceCalculator {

  getTotalPrice(membershipOption: MembershipType, usattDonation: number, enteredEvents: any[]): number;

  getSummaryReportItems(): SummaryReportItem [];
}
