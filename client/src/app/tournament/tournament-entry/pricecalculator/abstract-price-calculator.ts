import {MembershipType} from '../model/tournament-entry.model';
import {SummaryReportItem} from './summary-report.model';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {EventDayPipePipe} from '../../../shared/pipes/event-day-pipe.pipe';
import {StartTimePipe} from '../../../shared/pipes/start-time.pipe';
import {CurrencyPipe} from '@angular/common';
import {EventEntryStatus} from '../model/event-entry-status.enum';

export class AbstractPriceCalculator {

  protected membershipOptions: any [];

  protected registrationFee: number;

  protected lateEntryFee: number;

  protected isJunior: boolean;

  protected isLateEntry: boolean;

  protected tournamentStartDate: Date;

  protected tournamentCurrency: string;

  protected reportItems: SummaryReportItem[];

  constructor(membershipOptions: any[], registrationFee: number, lateEntryFee: number, isJunior: boolean,
              isLateEntry: boolean, tournamentStartDate: Date, tournamentCurrency: string) {
    this.membershipOptions = membershipOptions;
    this.registrationFee = registrationFee;
    this.lateEntryFee = lateEntryFee;
    this.isJunior = isJunior;
    this.isLateEntry = isLateEntry;
    this.tournamentStartDate = tournamentStartDate;
    this.tournamentCurrency = tournamentCurrency;
  }

  /**
   * Gets membership option
   * @param membershipOption
   * @protected
   */
  protected getMembershipOption(membershipOption: MembershipType): any {
    for (let i = 0; i < this.membershipOptions.length; i++) {
      const option = this.membershipOptions[i];
      if (option.value === membershipOption) {
        return option;
      }
    }
    return {value: 0, label: '', cost: 0};
  }

  protected getMembershipPrice(membershipOption: MembershipType): number {
    const membershipOptionObject = this.getMembershipOption(membershipOption);
    return membershipOptionObject.cost;
  }

  public getSummaryReportItems(): SummaryReportItem [] {
    return this.reportItems;
  }

  protected initiateReport() {
    this.reportItems = [];

    const mainHeader = new SummaryReportItem();
    mainHeader.isHeader = true;
    mainHeader.itemText = 'Membership';
    mainHeader.rightColumnText = 'Price';
    this.reportItems.push(mainHeader);
  }

  protected addMembershipOptionLine(membershipOptionObject: any) {
    const usattMembershipLine = new SummaryReportItem();
    usattMembershipLine.isHeader = false;
    usattMembershipLine.itemText = membershipOptionObject.label;
    usattMembershipLine.rightColumnText = this.getFormattedPrice(membershipOptionObject.cost);
    this.reportItems.push(usattMembershipLine);
  }

  protected addEventsHeader() {
    const eventsHeader = new SummaryReportItem();
    eventsHeader.isHeader = true;
    eventsHeader.itemText = 'Events';
    eventsHeader.rightColumnText = '';
    this.reportItems.push(eventsHeader);
  }

  protected addWaitedEventsHeader() {
    const eventsHeader = new SummaryReportItem();
    eventsHeader.isHeader = true;
    eventsHeader.itemText = 'Waited Events';
    eventsHeader.rightColumnText = '';
    this.reportItems.push(eventsHeader);
  }

  protected addEvent(enteredEvent: TournamentEventEntryInfo, price: number) {
    const eventLine = this.addEventCommon(enteredEvent);
    eventLine.rightColumnText = this.getFormattedPrice(price);
    this.reportItems.push(eventLine);
  }

  protected addWaitedEvent (enteredEvent: TournamentEventEntryInfo) {
    const eventLine = this.addEventCommon(enteredEvent);
    eventLine.rightColumnText = '';
    this.reportItems.push(eventLine);
  }

  /**
   * common code for event line
   * @param enteredEvent
   * @private
   */
  private addEventCommon (enteredEvent: TournamentEventEntryInfo): SummaryReportItem {
    const eventLine = new SummaryReportItem();
    eventLine.isHeader = false;
    const isPending = (enteredEvent?.status !== EventEntryStatus.ENTERED);
    eventLine.itemText =  enteredEvent?.event?.name + ((isPending) ? " (*)" : "");
    // enteredEvent?.event?.day | eventDay: tournamentStartDate }}
    const eventDayPipe: EventDayPipePipe = new EventDayPipePipe();
    const strEventDay = eventDayPipe.transform(enteredEvent?.event?.day, this.tournamentStartDate);
    // {{ enteredEvent?.event?.startTime | startTime }}
    const startTimePipe: StartTimePipe = new StartTimePipe();
    const strStartTime = startTimePipe.transform(enteredEvent?.event?.startTime);
    eventLine.subItemText = `${strEventDay} ${strStartTime}`;
    eventLine.rightColumnText = this.getFormattedPrice(enteredEvent?.price);
    return eventLine;
  }

  // ======================================================================
  // Fees section
  // ======================================================================
  protected addFeesSection () {
    const feesHeader = new SummaryReportItem();
    feesHeader.isHeader = true;
    feesHeader.itemText = 'Fees';
    feesHeader.rightColumnText = '';
    this.reportItems.push(feesHeader);
  }

  protected addUsattDonation (usattDonation: number) {
    const usattDonationLine = new SummaryReportItem();
    usattDonationLine.isHeader = false;
    usattDonationLine.itemText = 'USATT National Team Donation';
    usattDonationLine.rightColumnText = this.getFormattedPrice(usattDonation);
    this.reportItems.push(usattDonationLine);
  }

  protected addRegistrationFeeLine () {
    // tournament registration fee
    const registrationFeeLine = new SummaryReportItem();
    registrationFeeLine.isHeader = false;
    registrationFeeLine.itemText = 'Tournament Registration Fee';
    registrationFeeLine.rightColumnText = this.getFormattedPrice(this.registrationFee);
    this.reportItems.push(registrationFeeLine);
  }

  protected addPerPlayerFeeLine (perPlayerFee: number, eventName: string) {
    // team event per player fee
    const registrationFeeLine = new SummaryReportItem();
    registrationFeeLine.isHeader = false;
    registrationFeeLine.itemText = eventName + ' Event Per Player Fee';
    registrationFeeLine.rightColumnText = this.getFormattedPrice(perPlayerFee);
    this.reportItems.push(registrationFeeLine);
  }

  protected addCancellationFeeLine (cancellationFee: number, eventName: string) {
    const registrationFeeLine = new SummaryReportItem();
    registrationFeeLine.isHeader = false;
    registrationFeeLine.itemText = 'Cancellation Fee ' + eventName;
    registrationFeeLine.rightColumnText = this.getFormattedPrice(cancellationFee);
    this.reportItems.push(registrationFeeLine);
  }

  protected addLateEntryFeeLine () {
      const lateEntryFeeLine = new SummaryReportItem();
      lateEntryFeeLine.isHeader = false;
      lateEntryFeeLine.itemText = 'Late Entry Fee';
      lateEntryFeeLine.rightColumnText = this.getFormattedPrice(this.lateEntryFee);
      this.reportItems.push(lateEntryFeeLine);
  }

  private getFormattedPrice(price: number): string {
    // todo get current locale
    const currencyPipe: CurrencyPipe = new CurrencyPipe('en-US', this.tournamentCurrency);
    return currencyPipe.transform(price);
  }

  protected isEnteredInAnyEvents(enteredEvents: TournamentEventEntryInfo[]): boolean {
    for (let i = 0; i < enteredEvents.length; i++) {
      const enteredEvent = enteredEvents[i];
      if (enteredEvent.status === EventEntryStatus.PENDING_CONFIRMATION ||
        enteredEvent.status === EventEntryStatus.ENTERED) {
        return true;
      }
    }
    return false;
  }
}
