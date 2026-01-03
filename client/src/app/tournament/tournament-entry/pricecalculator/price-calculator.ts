import {MembershipType} from '../model/tournament-entry.model';
import {SummaryReportItem} from './summary-report.model';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {Team} from '../model/team.model';

export interface PriceCalculator {

  getTotalPrice(membershipOption: MembershipType,
                usattDonation: number,
                enteredEvents: TournamentEventEntryInfo[],
                teams: Team[],
                isWithdrawing: boolean,
                availableEvents: TournamentEventEntryInfo[],
                tournamentEntryId: number): number;

  getSummaryReportItems(): SummaryReportItem [];
}
