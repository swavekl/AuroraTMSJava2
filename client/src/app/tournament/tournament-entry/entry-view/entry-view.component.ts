import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {MembershipType, TournamentEntry} from '../model/tournament-entry.model';
import {Tournament} from '../../tournament-config/tournament.model';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {EventEntryStatus} from '../model/event-entry-status.enum';
import {Subscription} from 'rxjs';
import {DateUtils} from '../../../shared/date-utils';
import {TodayService} from '../../../shared/today.service';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {PriceCalculator} from '../pricecalculator/price-calculator';
import {PricingMethod} from '../../model/pricing-method.enum';
import {StandardPriceCalculator} from '../pricecalculator/standard-price-calculator';
import {DiscountedPriceCalculator} from '../pricecalculator/discounted-price-calculator';
import {MembershipUtil} from '../../util/membership-util';
import {Profile} from '../../../profile/profile';
import {SummaryReportItem} from '../pricecalculator/summary-report.model';
import {PaymentRefund} from '../../../account/model/payment-refund.model';
import {ChangeRatingDialogComponent} from '../change-rating-dialog/change-rating-dialog.component';
import {PaymentRefundStatus} from '../../../account/model/payment-refund-status.enum';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {EventEntryType} from '../../tournament-config/model/event-entry-type.enum';
import {Team} from '../model/team.model';

@Component({
    selector: 'app-entry-view',
    templateUrl: './entry-view.component.html',
    styleUrls: ['./entry-view.component.scss'],
    standalone: false
})
export class EntryViewComponent implements OnInit, OnChanges, OnDestroy {
  @Input()
  entry: TournamentEntry;

  @Input()
  tournament: Tournament;

  @Input()
  allEventEntryInfos: TournamentEventEntryInfo[];

  @Input()
  playerProfile: Profile;

  @Input()
  paymentsRefunds: PaymentRefund[];

  @Input()
  canChangeRating: boolean;

  @Output()
  action: EventEmitter<string> = new EventEmitter<string>();

  @Output()
  updateEntry: EventEmitter<TournamentEntry> = new EventEmitter<TournamentEntry>();

  enteredEvents: TournamentEventEntryInfo[] = [];

  tournamentStartDate: Date;

  // calculator for calculating total
  public priceCalculator: PriceCalculator;
  private membershipUtil: MembershipUtil;
  public membershipOptions: any [] = [];
  tournamentCurrency: string;

  entryTotal: number = 0;
  summaryReportItems: SummaryReportItem[] = [];

  @Input()
  teams!: Team[] | null;

  private subscriptions = new Subscription ();

  playerAge: number;
  constructor(private todayService: TodayService,
              private messageDialog: MatDialog) {
    this.membershipUtil = new MembershipUtil();
    this.membershipOptions = this.membershipUtil.getMembershipOptions();
    this.tournamentCurrency = 'USD';
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.allEventEntryInfos != null) {
      this.allEventEntryInfos = changes.allEventEntryInfos.currentValue || [];
      this.enteredEvents = this.allEventEntryInfos.filter(this.enteredEventsFilter, this);
    }
    const tournamentChange: SimpleChange = changes.tournament;
    if (tournamentChange != null) {
      this.tournament = tournamentChange.currentValue;
      if (this.tournament != null) {
        this.tournamentStartDate = new DateUtils().convertFromString(this.tournament.startDate);
      }
    }

    const entryChanges: SimpleChange = changes.entry;
    if (entryChanges != null) {
      this.entry = entryChanges.currentValue;
    }

    const profileChanges: SimpleChange = changes.playerProfile;
    if (profileChanges != null) {
      this.playerProfile = profileChanges.currentValue;
    }
    if (this.playerProfile != null && this.tournament != null && this.entry && this.paymentsRefunds != null && this.teams != null) {
      this.priceCalculator = this.initPricingCalculator(this.tournament.configuration.pricingMethod);
      this.entryTotal = this.getTotal();
      this.summaryReportItems = this.getSummaryReportItems();
      this.playerAge = this.getPlayerAge();
    }
  }

  /**
   * Initializes pricing calculator
   * @param pricingMethod
   * @private
   */
  private initPricingCalculator(pricingMethod: PricingMethod) {
    const isJunior = this.membershipUtil.isPlayerAJunior(this.playerProfile.dateOfBirth, this.tournament.startDate);
    let showLateEntryFee = false;
    if (this.paymentsRefunds?.length > 0) {
      this.paymentsRefunds.forEach((paymentRefund: PaymentRefund) => {
        if (paymentRefund.status === PaymentRefundStatus.PAYMENT_COMPLETED) {
          const isPaymentOnTime = new DateUtils().isDateBefore(paymentRefund.transactionDate, this.tournament.configuration.lateEntryDate);
          showLateEntryFee = showLateEntryFee || !isPaymentOnTime;
        }
      });
    }
    switch (pricingMethod) {
      case PricingMethod.STANDARD:
        return new StandardPriceCalculator(this.membershipOptions,
          this.tournament.configuration.registrationFee,
          this.tournament.configuration.lateEntryFee, isJunior, showLateEntryFee,
          this.tournamentStartDate, this.tournamentCurrency);
      case PricingMethod.DISCOUNTED:
        return new DiscountedPriceCalculator(this.membershipOptions,
          this.tournament.configuration.registrationFee,
          this.tournament.configuration.lateEntryFee, isJunior, showLateEntryFee,
          this.tournamentStartDate, this.tournamentCurrency);
      // default:
      //   return new StandardPriceCalculator(this.membershipOptions);
    }
    return null;
  }


  getSummaryReportItems(): SummaryReportItem [] {
    if (this.priceCalculator) {
      return this.priceCalculator.getSummaryReportItems();
    } else {
      return [];
    }
  }

  /**
   * Gets current player total regardless of previous payments or refunds
   */
  getTotal(): number {
    const membershipOption: MembershipType = this.entry?.membershipOption;
    const usattDonation = this.entry?.usattDonation ?? 0;
    let total: number = 0;
    if (this.priceCalculator) {
      total = this.priceCalculator.getTotalPrice(membershipOption, usattDonation, this.enteredEvents, this.teams, false, []);
    }
    return total;
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  ngOnInit(): void {
  }

  enteredEventsFilter(eventEntryInfo: TournamentEventEntryInfo, index: number, array: TournamentEventEntryInfo[]): boolean {
    return this.filterEventEntries(eventEntryInfo.status,
      [EventEntryStatus.ENTERED,
        EventEntryStatus.ENTERED_WAITING_LIST,
        EventEntryStatus.PENDING_CONFIRMATION,
        EventEntryStatus.PENDING_WAITING_LIST
      ]);
  }

  filterEventEntries(eventEntryStatus: EventEntryStatus, statusList: EventEntryStatus []): boolean {
    return (statusList.indexOf(eventEntryStatus) !== -1);
  }

  private isBeforeEntryCutoffDate() {
    const entryCutoffDate = this.tournament?.configuration.entryCutoffDate;
    if (entryCutoffDate != null) {
      const today = this.todayService.todaysDate;
      return (new DateUtils().isDateSameOrBefore(today, entryCutoffDate));
    } else {
      return false;
    }
  }

  canModify() {
    const isBeforeEntryCutoffDate = this.isBeforeEntryCutoffDate();
    const hasEntry = this.entry?.id !== 0;
    return hasEntry && isBeforeEntryCutoffDate;
  }

  private isSameOrBeforeRefundDate() {
    const refundDate = this.tournament?.configuration.refundDate;
    if (refundDate) {
      const today = this.todayService.todaysDate;
      return (new DateUtils().isDateSameOrBefore(today, refundDate));
    } else {
      return false;
    }
  }

  canWithdraw() {
    const isSameOrBeforeRefundDate = this.isSameOrBeforeRefundDate();
    const hasEntry = this.entry?.id !== 0;
    const hasEvents = this.enteredEvents?.length > 0;
    return hasEntry && hasEvents && isSameOrBeforeRefundDate;
  }

  onModify() {
    this.action.emit('modify');
  }

  onWithdraw() {
    const message = "To withdraw from the tournament, please remove yourself from each event one by one. " +
      "Then continue through all the steps of the dialog and initiate a refund.  ";
      // "You will get 2 emails confirming your refund and withdrawal." +
      // "Thank you for your interest in our tournament.";
    const dialogRef = this.messageDialog.open(ConfirmationPopupComponent, {
      width: '300px', height: '320px',
      data: { message: message, title: 'Withdrawal Confirmation',
        showCancelButton: true, contentAreaHeight: '170px' }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed with result ', result);
      if (result === 'ok') {
        // tell them what to expect in dialog
        this.action.emit('withdraw');
      } else {

      }
    });

  }

  onBack() {
    this.action.emit('back');
  }

  getPlayerAge() {
    const dateOfBirth = this.playerProfile?.dateOfBirth;
    let age = 0;
    if (dateOfBirth != null && this.tournamentStartDate != null) {
      age = new DateUtils().getAgeOnDate(dateOfBirth, this.tournamentStartDate);
    }
    return age;
  }

  showChangeRatingDialog() {
    const dialogRef = this.messageDialog.open(ChangeRatingDialogComponent, {
      width: '350px', height: '230px',
      data: { eligibilityRating: this.entry.eligibilityRating, seedRating: this.entry.seedRating }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'ok') {
        const updatedEntry: TournamentEntry = {...this.entry,
          eligibilityRating: result.eligibilityRating,
          seedRating: result.seedRating
        };
        // tell them what to expect in dialog
        this.updateEntry.emit(updatedEntry);
      }
    });
  }

  protected getTeamEvents(): TournamentEvent [] {
    return (this.allEventEntryInfos != null) ? this.allEventEntryInfos.filter(
      teei => {
        return teei.event.eventEntryType === EventEntryType.TEAM;
      }).map(teei2 => {
      return teei2.event;
    }) : [];
  }
}
