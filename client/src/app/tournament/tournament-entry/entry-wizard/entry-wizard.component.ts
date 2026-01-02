import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChange,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {MembershipType, TournamentEntry} from '../model/tournament-entry.model';
import {getCurrencySymbol} from '@angular/common';
import {NgForm, UntypedFormGroup} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {STEPPER_GLOBAL_OPTIONS} from '@angular/cdk/stepper';

import {Profile} from '../../../profile/profile';
import {ProfileFindPopupComponent, ProfileSearchData} from '../../../profile/profile-find-popup/profile-find-popup.component';
import {DateUtils} from '../../../shared/date-utils';

import {first} from 'rxjs/operators';
import {Subscription} from 'rxjs';

import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {EventEntryStatus} from '../model/event-entry-status.enum';
import {AvailabilityStatus} from '../model/availability-status.enum';
import {EventEntryCommand} from '../model/event-entry-command.enum';
import {PaymentRefund} from '../../../account/model/payment-refund.model';
import {CallbackData} from '../../../account/model/callback-data';
import {PaymentDialogData} from '../../../account/payment-dialog/payment-dialog-data';
import {PaymentRefundFor} from '../../../account/model/payment-refund-for.enum';
import {PaymentRefundStatus} from '../../../account/model/payment-refund-status.enum';
import {PaymentRequest} from '../../../account/model/payment-request.model';
import {RefundRequest} from '../../../account/model/refund-request.model';
import {PaymentDialogService} from '../../../account/service/payment-dialog.service';
import {RefundDialogService} from '../../../account/service/refund-dialog.service';
import {CurrencyService} from '../../../account/service/currency.service';
import {Tournament} from '../../tournament-config/tournament.model';
import {PriceCalculator} from '../pricecalculator/price-calculator';
import {PricingMethod} from '../../model/pricing-method.enum';
import {StandardPriceCalculator} from '../pricecalculator/standard-price-calculator';
import {DiscountedPriceCalculator} from '../pricecalculator/discounted-price-calculator';
import {MembershipUtil} from '../../util/membership-util';
import {MatStepper} from '@angular/material/stepper';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {CheckCashPaymentDialogService} from '../../../account/service/check-cash-payment-dialog.service';
import {Team} from '../model/team.model';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {EventEntryType} from '../../tournament-config/model/event-entry-type.enum';
import {TeamMember} from '../model/team-member.model';
import {TeamEntryStatus} from '../model/team-entry-status.enum';

@Component({
    selector: 'app-entry-wizard',
    templateUrl: './entry-wizard.component.html',
    styleUrls: ['./entry-wizard.component.scss'],
    providers: [
        PaymentDialogService,
        {
            provide: STEPPER_GLOBAL_OPTIONS,
            useValue: { showError: true },
        },
    ],
    standalone: false
})
export class EntryWizardComponent implements OnInit, OnChanges, OnDestroy {

  @Input()
  entry: TournamentEntry;

  @Input()
  tournament: Tournament;

  @Input()
  playerProfile: Profile;

  @Input()
  paymentsRefunds: PaymentRefund[];

  @Input()
  allEventEntryInfos: TournamentEventEntryInfo[];

  @Input()
  isWithdrawing: boolean = false;

  @Input()
  allowPaymentByCheck: boolean = false;

  @Input()
  teams: Team [] = [];

  enteredEvents: TournamentEventEntryInfo[] = [];
  availableEvents: TournamentEventEntryInfo[] = [];
  unavailableEvents: TournamentEventEntryInfo[] = [];

  @Output()
  tournamentEntryChanged: EventEmitter<TournamentEntry> = new EventEmitter<TournamentEntry>();

  @Output()
  eventEntryChanged: EventEmitter<TournamentEventEntryInfo> = new EventEmitter<TournamentEventEntryInfo>();

  @Output()
  confirmEntries: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  finish: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  discard: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  teamChanged: EventEmitter<any> = new EventEmitter<any>();

  tournamentStartDate: Date;

  hasTeamEvents: boolean;

  tournamentStarLevel: number;
  // if true membership is expired for tournament purposes
  membershipIsExpired: boolean;

  // player and tournament currencies if different
  tournamentCurrency: string;
  playerCurrency: string;
  // currency exchange rate if player is registering in another country and may be charged in this country's currency
  private currencyExchangeRate: number;
  // currency symbol in current locale
  tournamentCurrencySymbol: string;
  // if true rate was retrieved successfully
  currencyRateRetrieved: boolean;

  // calculator for calculating total
  public priceCalculator: PriceCalculator;

  // Define variables to hold the results
  totalPrice: number = 0;
  summaryItems: any[] = [];

  // balance actions
  public readonly BALANCE_ACTION_PAY = 1;
  public readonly BALANCE_ACTION_REFUND = 2;
  public readonly BALANCE_ACTION_CONFIRM = 3;
  public readonly BALANCE_ACTION_NOCHANGE = 4;

  // indicates if user made any changes
  private dirty: boolean;

  // last time cart session was updated - to be able to prevent paying for expired sessions
  private cartSessionLastUpdate: Date = null;

  private membershipUtil: MembershipUtil;
  public membershipOptions: any [] = [];

  // players entries into doubles
  doublesEntries: any [] = [];

  private subscriptions: Subscription;

  @ViewChild(MatStepper)
  stepper: MatStepper;

  public visitedEvents: boolean = false;

  constructor(private dialog: MatDialog,
              private _change: ChangeDetectorRef,
              private paymentDialogService: PaymentDialogService,
              private refundDialogService: RefundDialogService,
              private currencyService: CurrencyService,
              private checkCashPaymentDialogService: CheckCashPaymentDialogService) {
    this.paymentsRefunds = [];
    this.tournamentCurrency = 'USD';
    this.playerCurrency = 'USD';
    this.currencyExchangeRate = 1.0;
    this.currencyRateRetrieved = false;
    this.dirty = false;
    this.cartSessionLastUpdate = null;
    this.visitedEvents = false;
    this.subscriptions = new Subscription();
    this.membershipUtil = new MembershipUtil();
    this.membershipOptions = this.membershipUtil.getMembershipOptions();
  }

  ngOnInit(): void {
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.allEventEntryInfos != null) {
      this.allEventEntryInfos = changes.allEventEntryInfos.currentValue || [];
      this.enteredEvents = this.allEventEntryInfos.filter(this.enteredEventsFilter, this);
      this.availableEvents = this.allEventEntryInfos.filter(this.availableEventsFilter, this);
      this.unavailableEvents = this.allEventEntryInfos.filter(this.unavailableEventsFilter, this);
      this.doublesEntries = this.makeDoublesEntries(this.enteredEvents);
      this.hasTeamEvents = this.anyTeamEventsInTournament();
    }

    const playerProfileChange: SimpleChange = changes.playerProfile;
    if (playerProfileChange != null) {
      this.playerProfile = playerProfileChange.currentValue;
    }

    const tournamentChange: SimpleChange = changes.tournament;
    if (tournamentChange != null) {
      this.tournament = tournamentChange.currentValue;
      if (this.tournament != null) {
        // console.log('tournament start date is ' + this.tournament.startDate);
        this.tournamentStartDate = new DateUtils().convertFromString(this.tournament.startDate);
        this.tournamentStarLevel = this.tournament.starLevel;
      }
    }

    const entryChange: SimpleChange = changes.entry;
    if (entryChange != null) {
      const entry = entryChange.currentValue;
      if (entry != null) {
        // change option to not required when player is withdrawing
        const membershipOption = (!this.isWithdrawing) ? entry.membershipOption : MembershipType.NO_MEMBERSHIP_REQUIRED;
        this.entry = {
          ...entry,
          membershipOption: membershipOption
        }
      }
    }

    // make a copy of teams so their names are modifiable
    const teamsChange: SimpleChange = changes.teams;
    if (teamsChange != null) {
      const teams = teamsChange.currentValue;
      if (teams != null) {
        this.teams = JSON.parse(JSON.stringify(teams));
      }
    }

    // when both are ready proceed with some calculations
    if (this.playerProfile != null && this.tournament != null) {
      const dateOfBirth = this.playerProfile.dateOfBirth;
      this.membershipUtil.hideMembershipOptions(dateOfBirth, this.tournament.startDate, this.tournamentStarLevel);
      // fetch account information in case they want to pay so the payment dialog comes up faster
      this.prepareForPayment(this.tournament.id);
      // check if membership is expired
      this.membershipIsExpired = new DateUtils().isDateBefore(
        this.playerProfile.membershipExpirationDate, this.tournamentStartDate);

      this.priceCalculator = this.initPricingCalculator(this.tournament.configuration.pricingMethod);
      this.updatePricing();
    }
  }

  enteredEventsFilter(eventEntryInfo: TournamentEventEntryInfo, index: number, array: TournamentEventEntryInfo[]): boolean {
    return this.filterEventEntries(eventEntryInfo.status,
      [EventEntryStatus.ENTERED,
        EventEntryStatus.ENTERED_WAITING_LIST,
        EventEntryStatus.PENDING_CONFIRMATION,
        EventEntryStatus.PENDING_WAITING_LIST
      ]);
  }

  availableEventsFilter(eventEntryInfo: TournamentEventEntryInfo, index: number, array: TournamentEventEntryInfo[]): boolean {
    const availableStatusOK = (eventEntryInfo.availabilityStatus === AvailabilityStatus.AVAILABLE_FOR_ENTRY)
      || (eventEntryInfo.availabilityStatus === AvailabilityStatus.EVENT_FULL);
    return this.filterEventEntries(eventEntryInfo.status,
      [
        EventEntryStatus.NOT_ENTERED,
        EventEntryStatus.PENDING_DELETION,
        EventEntryStatus.RESERVED_WAITING_LIST
      ]) && availableStatusOK;
  }

  unavailableEventsFilter(eventEntryInfo: TournamentEventEntryInfo, index: number, array: TournamentEventEntryInfo[]): boolean {
    const notAvailableStatus = !((eventEntryInfo.availabilityStatus === AvailabilityStatus.AVAILABLE_FOR_ENTRY)
      || (eventEntryInfo.availabilityStatus === AvailabilityStatus.EVENT_FULL));
    return this.filterEventEntries(eventEntryInfo.status,
      [
        EventEntryStatus.NOT_ENTERED,
        EventEntryStatus.PENDING_DELETION
      ]) && notAvailableStatus;
  }

  filterEventEntries(eventEntryStatus: EventEntryStatus, statusList: EventEntryStatus []): boolean {
    const foundIndex = statusList.indexOf(eventEntryStatus);
    // console.log('eventEntryStatus', eventEntryStatus);
    // console.log ('foundIndex', foundIndex);
    return (statusList.indexOf(eventEntryStatus) !== -1);
  }

  onSave(formValues: any) {
    // console.log('formValues', formValues);
    this.finish.emit(null);
  }

  editEntry(profileId: number) {

  }

  onChange(form: UntypedFormGroup) {
    // console.log('in onChange');
    if (this.entry != null) {
      const updatedEntry = {
        ...this.entry,
        ...form.value
      };
      this.dirty = true;
      this.tournamentEntryChanged.emit(updatedEntry);
    }
    this._change.markForCheck();
  }

  onEventWithdraw(eventEntryId: number) {
    // console.log ('onEventWithdraw ', eventEntryId);
    for (let i = 0; i < this.enteredEvents.length; i++) {
      const enteredEvent = this.enteredEvents[i];
      if (enteredEvent.eventEntryFk === eventEntryId) {
        const withdrawEntry: TournamentEventEntryInfo = {
          ...enteredEvent,
          event: undefined
        };
        this.dirty = true;
        this.updateCartSessionLastUpdate(new Date());
        this.eventEntryChanged.emit(withdrawEntry);
      }
    }
  }

  onEventEnter(eventId: number, eventEntryCommand: EventEntryCommand): void {
    // console.log('onEventEnter eventId ', eventId);
    // console.log('onEventEnter command ', eventEntryCommand);
    for (let i = 0; i < this.availableEvents.length; i++) {
      const availableEvent = this.availableEvents[i];
      if (availableEvent.eventFk === eventId) {
        const eventEntryInfo: TournamentEventEntryInfo = {
          ...availableEvent,
          event: undefined
        };
        this.dirty = true;
        this.updateCartSessionLastUpdate(new Date());
        this.eventEntryChanged.emit(eventEntryInfo);
      }
    }
  }

  /**
   * Initializes pricing calculator
   * @param pricingMethod
   * @private
   */
  private initPricingCalculator(pricingMethod: PricingMethod) {
    const isJunior = this.membershipUtil.isPlayerAJunior(this.playerProfile.dateOfBirth, this.tournament.startDate);
    const isLateEntry = new DateUtils().isDateBefore(this.tournament.configuration.lateEntryDate, new Date());
    switch (pricingMethod) {
      case PricingMethod.STANDARD:
        return new StandardPriceCalculator(this.membershipOptions,
          this.tournament.configuration.registrationFee,
          this.tournament.configuration.lateEntryFee, isJunior, isLateEntry,
          this.tournamentStartDate, this.tournamentCurrency);
      case PricingMethod.DISCOUNTED:
        return new DiscountedPriceCalculator(this.membershipOptions,
          this.tournament.configuration.registrationFee,
          this.tournament.configuration.lateEntryFee, isJunior, isLateEntry,
          this.tournamentStartDate, this.tournamentCurrency);
      // default:
      //   return new StandardPriceCalculator(this.membershipOptions);
    }
    return null;
  }

  // Create a single method to refresh the data
  private updatePricing(): void {
    const membershipOption: MembershipType = this.entry?.membershipOption;
    const usattDonation = this.entry?.usattDonation ?? 0;
    if (this.priceCalculator) {
      console.log('updating pricing');
      this.totalPrice = this.priceCalculator.getTotalPrice(membershipOption, usattDonation, this.enteredEvents, this.teams, this.isWithdrawing, this.availableEvents);
      this.summaryItems = this.priceCalculator.getSummaryReportItems();
    } else {
      console.log('updating pricing to ZERO');
      this.totalPrice = 0;
      this.summaryItems = [];
    }
  }

  /**
   * Gets balance due (positive - payment due, negative - refund due, zero - just confirm)
   */
  getBalance(): number {
    // let total = this.getTotal();
    let total = this.totalPrice;

    // take into account payments and refunds
    total -= this.getPaymentsRefundsTotal();

    return total;
  }

  getPaymentsRefundsTotal(): number {
    let paymentsRefundsTotal = 0;
    if (this.paymentsRefunds != null) {
      this.paymentsRefunds.forEach((paymentRefund: PaymentRefund) => {
        const amount: number = paymentRefund.amount / 100;
        if (paymentRefund.status === PaymentRefundStatus.PAYMENT_COMPLETED) {
          paymentsRefundsTotal += amount;
        } else if (paymentRefund.status === PaymentRefundStatus.REFUND_COMPLETED) {
          paymentsRefundsTotal -= amount;
        }
      });
    }
    return paymentsRefundsTotal;
  }

  /**
   * Gets balance without negative sign
   */
  getAbsoluteBalance(): number {
    return Math.abs(this.getBalance());
  }

  getAbsoluteBalanceInPlayerCurrency(): number {
    return (this.getAbsoluteBalance() * this.currencyExchangeRate);
  }

  getTournamentId(): number {
    return this.entry?.tournamentFk;
  }

  getStatusClass(status: EventEntryStatus) {
    switch (status) {
      case EventEntryStatus.ENTERED:
        return 'status-confirmed';
      case EventEntryStatus.PENDING_CONFIRMATION:
      case EventEntryStatus.PENDING_DELETION:
        return 'status-pending-confirmation';
      case EventEntryStatus.ENTERED_WAITING_LIST:
        return 'status-waiting-list';
      case EventEntryStatus.NOT_ENTERED:
      case EventEntryStatus.PENDING_WAITING_LIST:
        return 'status-not-entered';
      default:
        return 'status-disqualified';
    }
  }

  /**
   * Experimental - balance to pay in player's currency
   * @param balanceInPlayerCurrency to pay
   * @param balanceInTournamentCurrency to pay
   */
  onPayPlayerTotalInPlayerCurrency(balanceInPlayerCurrency: number, balanceInTournamentCurrency: number) {
    this.onPayPlayerTotalInCurrency(balanceInPlayerCurrency, balanceInTournamentCurrency, this.playerCurrency);
  }

  /**
   * Pay for balance in tournament currency
   * @param balanceInPlayerCurrency balance in tournament & player currency
   */
  onPayPlayerTotal(balanceInPlayerCurrency: number) {
    this.onPayPlayerTotalInCurrency(balanceInPlayerCurrency, balanceInPlayerCurrency, this.tournamentCurrency);
  }

  onPayPlayerTotalByCheck(balanceInPlayerCurrency: number) {
    this.onPayPlayerTotalInCurrency(balanceInPlayerCurrency, balanceInPlayerCurrency, this.tournamentCurrency, false);
  }

  /**
   * Show payment dialog for amount and currency to pay in
   * @param balanceInPlayerCurrency balance in currency to pay in
   * @param balanceInTournamentCurrency balance in currency to pay in tournament currency
   * @param currencyOfPayment 3 letter currency code of currency to use
   * @param payByCreditCard if true show payment by credit card, otherwise by check/cash
   */
  onPayPlayerTotalInCurrency(balanceInPlayerCurrency: number, balanceInTournamentCurrency: number, currencyOfPayment: string, payByCreditCard: boolean = true) {
    if (this.isCartSessionExpired()) {
      this.showExpiredSessionWarning();
      return;
    }
    const amount: number = balanceInPlayerCurrency * 100;
    const amountInAccountCurrency: number = balanceInTournamentCurrency * 100;
    const fullName = this.playerProfile.firstName + ' ' + this.playerProfile.lastName;
    const postalCode = this.playerProfile.zipCode;
    const email = this.playerProfile.email;
    const tournamentName = this.tournament.name;
    const paymentRequest: PaymentRequest = {
      paymentRefundFor: PaymentRefundFor.TOURNAMENT_ENTRY,
      accountItemId: this.getTournamentId(),
      transactionItemId: this.entry.id,
      amount: amount,
      currencyCode: currencyOfPayment,
      amountInAccountCurrency: amountInAccountCurrency,
      statementDescriptor: tournamentName,
      fullName: fullName,
      postalCode: postalCode,
      receiptEmail: email,
    };

    const paymentDialogData: PaymentDialogData = {
      paymentRequest: paymentRequest,
      stripeInstance: null
    };

    const callbackData: CallbackData = {
      successCallbackFn: this.onPaymentSuccessful,
      cancelCallbackFn: this.onPaymentCanceled,
      callbackScope: this
    };
    if (payByCreditCard == true) {
      this.paymentDialogService.showPaymentDialog(paymentDialogData, callbackData);
    } else {
      this.checkCashPaymentDialogService.showPaymentDialog(paymentDialogData, callbackData);
    }
  }

  /**
   *
   */
  onPaymentCanceled(scope: any) {
    console.log('in onPaymentCanceled');
  }

  /**
   * Callback from payment dialog when payment is successful
   */
  onPaymentSuccessful(scope: any) {
    if (scope != null) {
      scope.confirmEntry();
    }
  }

  /**
   * Confirms changes when there is no balance due
   */
  onConfirmWithoutPayment() {
    if (this.isCartSessionExpired()) {
      this.showExpiredSessionWarning();
      return;
    }

    this.confirmEntry();
  }

  onIssueRefundInPlayerCurrency(refundAmountInPlayerCurrency: number, refundAmountInTournamentCurrency: number) {
    this.onIssueRefund(refundAmountInPlayerCurrency, refundAmountInTournamentCurrency, this.playerCurrency);
  }

  onIssueRefundInTournamentCurrency(refundAmountInTournamentCurrency: number) {
    this.onIssueRefund(refundAmountInTournamentCurrency, refundAmountInTournamentCurrency, this.tournamentCurrency);
  }

  onIssueRefundByCheck(refundAmountInTournamentCurrency: number) {
    this.onIssueRefund(refundAmountInTournamentCurrency, refundAmountInTournamentCurrency, this.tournamentCurrency, false);
  }

  /**
   * Issues a refund
   */
  onIssueRefund(refundAmountInPlayerCurrency: number, refundAmountInTournamentCurrency: number, refundCurrency: string, refundByCreditCard: boolean = true) {
    const amount: number = refundAmountInPlayerCurrency * 100;
    const amountInAccountCurrency: number = refundAmountInTournamentCurrency * 100;
    const refundRequest: RefundRequest = {
      paymentRefundFor: PaymentRefundFor.TOURNAMENT_ENTRY,
      accountItemId: this.getTournamentId(),
      transactionItemId: this.entry.id,
      amount: amount,
      amountInAccountCurrency: amountInAccountCurrency,
      exchangeRate: this.currencyExchangeRate,
      currencyCode: refundCurrency
    };
    const callbackData: CallbackData = {
      successCallbackFn: this.onRefundSuccessful,
      cancelCallbackFn: this.onRefundCanceled,
      callbackScope: this
    };
    if (refundByCreditCard) {
      this.refundDialogService.showRefundDialog(refundRequest, callbackData);
    } else {
      this.checkCashPaymentDialogService.showRefundDialog(refundRequest, callbackData)
    }
  }

  public onRefundSuccessful(scope: any): void {
    // console.log('refund successful');
    if (scope != null) {
      scope.confirmEntry();
    }
  }

  public onRefundCanceled(scope: any): void {
    console.log('refund cancelled');
  }

  confirmEntry() {
    const confirmEntry = {
      ...this.entry,
      confirm: true
    };
    this.dirty = false;
    this.updateCartSessionLastUpdate(null);
    this.confirmEntries.emit(confirmEntry);
  }

  /**
   * Decides what action to do to confirm the entry
   */
  balanceAction(): number {
    const balance: number = this.getBalance();
    if (balance > 0) {
      return this.BALANCE_ACTION_PAY;
    } else if (balance < 0) {
      return this.BALANCE_ACTION_REFUND;
    } else if (this.dirty) {
      return this.BALANCE_ACTION_CONFIRM;
    } else {
      return this.BALANCE_ACTION_NOCHANGE;
    }
  }

  isPayment(paymentRefund: PaymentRefund) {
    return paymentRefund.status === PaymentRefundStatus.PAYMENT_COMPLETED;
  }

  /**
   * Prepares data used for payment such as account number for this tournament id and currency exchange rate
   * if player is buying from foreign address
   * and exchange rates if any
   * @param tournamentId
   * @private
   */
  private prepareForPayment(tournamentId: number) {
    // console.log('in prepareForPayment');
    // check if we have the player profile
    if (this.playerProfile?.countryCode != null) {
      // console.log ('profile is ready let\'s get currency exchange rates');
      this.currencyExchangeRate = 1.0;
      // fetch account information in case they want to pay so the payment dialog comes up faster
      this.paymentDialogService.prepareForPayment(PaymentRefundFor.TOURNAMENT_ENTRY, tournamentId)
        .pipe(first())
        .subscribe(
          (tournamentCurrency: string) => {
            // console.log ('got tournamentCurrency ' + tournamentCurrency);
            this.playerCurrency = this.currencyService.getCountryCurrencyId(this.playerProfile?.countryCode);
            this.tournamentCurrency = tournamentCurrency;
            this.tournamentCurrencySymbol = this.getTournamentCurrencySymbol();
            // console.log ('got playerCurrency ' + this.playerCurrency);
            if (this.tournamentCurrency !== this.playerCurrency) {
              this.getCurrencyExchangeRate(this.tournamentCurrency, this.playerCurrency);
            } else {
              this.currencyExchangeRate = 1.0;
            }
          },
          (error: any) => {
            const config = {
              width: '450px', height: '220px', data: {
                contentAreaHeight: '100px',
                title: 'Error',
                showCancel: false,
                okText: 'Close',
                message: `Payments are not configured for this tournament. ${error.error}`,
              }
            };
            const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
            dialogRef.afterClosed().subscribe(result => {
            });
          });
    }
  }

  /**
   * Gets the exchange rate
   *
   * @param tournamentCurrency
   * @param playerCurrency
   * @private
   */
  private getCurrencyExchangeRate(tournamentCurrency: string, playerCurrency: string) {
    this.currencyService.getExchangeRate(tournamentCurrency, playerCurrency)
      .pipe(first())
      .subscribe(
        (exchangeRate: number) => {
          // console.log('got currency exchange rate from ' + tournamentCurrency + ' to ' + playerCurrency + ' = ' + exchangeRate);
          this.currencyExchangeRate = exchangeRate;
          this.currencyRateRetrieved = true;
        },
        (error: any) => {
          console.log('Unable to get currency exchange rate for player' + JSON.stringify(error));
          this.currencyRateRetrieved = false;
        }
      );
  }

  /**
   * Gets button label corresponding to the command
   * @param eventEntryCommand
   */
  getEnterButtonLabel(eventEntryCommand: EventEntryCommand) {
    switch (eventEntryCommand) {
      case EventEntryCommand.ENTER:
        return 'Enter';
      case EventEntryCommand.ENTER_WAITING_LIST:
        return 'Wait List';
      case EventEntryCommand.DROP:
        return 'Drop';
      case EventEntryCommand.DROP_WAITING_LIST:
        return 'Drop W.L.';
      case EventEntryCommand.REVERT_DROP:
        return 'Revert';
    }
  }

  getEnterButtonIcon(eventEntryCommand: EventEntryCommand) {
    switch (eventEntryCommand) {
      case EventEntryCommand.ENTER:
        return 'add';
      case EventEntryCommand.ENTER_WAITING_LIST:
        return 'playlist_add';
      case EventEntryCommand.DROP:
        return 'remove';
      case EventEntryCommand.DROP_WAITING_LIST:
        return 'remove';
      case EventEntryCommand.REVERT_DROP:
        return 'undo';
    }
  }

  /**
   * Gets currency symbol of tournament currency appropriate for displaying in the current locale
   */
  getTournamentCurrencySymbol() {
    const usersLocale = this.getUsersLocale('en-US');
    return getCurrencySymbol(this.tournamentCurrency, 'narrow', usersLocale);
  }

  /**
   *
   * @param defaultValue
   * @private
   */
  private getUsersLocale(defaultValue: string): string {
    if (typeof window === 'undefined' || typeof window.navigator === 'undefined') {
      return defaultValue;
    }
    const wn = window.navigator as any;
    let lang = wn.languages ? wn.languages[0] : defaultValue;
    lang = lang || wn.language || wn.browserLanguage || wn.userLanguage;
    return lang;
  }

  /**
   *
   * @param enteredEvents
   * @private
   */
  private makeDoublesEntries(enteredEvents: TournamentEventEntryInfo[]): any [] {
    const doublesEntries: any [] = [];
    for (let i = 0; i < enteredEvents.length; i++) {
      const enteredEvent = enteredEvents[i];
      if (enteredEvent?.event?.doubles) {
        const doublesEntryInfo = {
          eventFk: enteredEvent.eventFk,
          eventName: enteredEvent.event.name,
          partnerName: enteredEvent.doublesPartnerName,
          partnerProfileId: enteredEvent.doublesPartnerProfileId,
          varName: 'doublesEvent_' + enteredEvent.eventFk
        };
        doublesEntries.push(doublesEntryInfo);
      }
    }
    return doublesEntries;
  }

  /**
   *
   * @param eventFk
   */
  public onFindDoublesPartnerByName(eventFk: number) {
    const profileSearchData: ProfileSearchData = {
      firstName: null,
      lastName: null
    };
    const config = {
      width: '400px', height: '550px', data: profileSearchData
    };

    const doublesEntries = this.doublesEntries;
    const enteredEvents = this.enteredEvents;
    const dialogRef = this.dialog.open(ProfileFindPopupComponent, config);
    const subscription = dialogRef.afterClosed().subscribe(result => {
      if (result?.action === 'ok') {
        const partnerName = result.selectedPlayerRecord.lastName + ', ' + result.selectedPlayerRecord.firstName;
        const partnerProfileId = result.selectedPlayerRecord.id;
        // update object used by UI
        doublesEntries.forEach(doublesEntry => {
          if (eventFk === doublesEntry.eventFk) {
            doublesEntry.partnerName = partnerName;
            if (doublesEntry.partnerProfileId !== partnerProfileId) {
              this.dirty = true;
            }
            doublesEntry.partnerProfileId = partnerProfileId;
          }
        });

        // update entered event
        enteredEvents.forEach(enteredEvent => {
          if (eventFk === enteredEvent.eventFk) {
            const eventEntryInfo: TournamentEventEntryInfo = {
              ...enteredEvent,
              eventEntryCommand: EventEntryCommand.UPDATE_DOUBLES,
              doublesPartnerProfileId: partnerProfileId,
              doublesPartnerName: partnerName
            };
            this.eventEntryChanged.emit(eventEntryInfo);
          }
        });
      }
    });
    this.subscriptions.add(subscription);
  }

  public isDirty(): boolean {
    return this.dirty;
  }

  public clean() {
    this.dirty = false;
  }

  hasEventsError(): boolean {
    return this.isWithdrawing ? false : (this.enteredEvents?.length === 0);
  }

  onLeavingEvents() {
    this.visitedEvents = true;
    this.stepper.next();
  }

  showNoEventsError(): boolean {
    return this.visitedEvents && this.hasEventsError();
  }

  discardChanges() {
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, {
      width: '300px', height: '200px',
      data: {message: "Discard changes?", title: 'Confirmation', showCancelButton: true, cancelText: 'No', okText: 'Yes'}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
        // tell them what to expect in dialog
        this.discard.emit(true);
      }
    });
  }

  public updateCartSessionLastUpdate(newDate: Date) {
    this.cartSessionLastUpdate = newDate;
  }

  public isCartSessionExpired(): boolean {
    const now = new Date();
    const timeDifference = new DateUtils().getTimeDifference(this.cartSessionLastUpdate, now);
    return timeDifference >= 30;
  }

  private showExpiredSessionWarning() {
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, {
      width: '300px', height: '300px',
      data: {
        message: "Expired cart sessions are removed by the system after 30 minutes of inactivity, " +
          " so you must complete your entry with payment or confirmation before that time.",
        contentAreaHeight: '180px', title: 'Expired Cart Session', okText: 'Start Over', showCancel: false
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
        // finish without saving, cleanup process already cleaned up
        this.clean();
        this.finish.emit(null);
      }
    });
  }

  protected getTeamEvents(): TournamentEvent [] {
    return (this.allEventEntryInfos != null) ? this.allEventEntryInfos.filter(
      teei => {
        return teei.event?.eventEntryType === EventEntryType.TEAM;
      }).map(teei2 => {
      return teei2.event;
    }) : [];
  }

  protected anyTeamEventsInTournament(): boolean {
    const teamEvents = this.getTeamEvents();
    if (teamEvents != null) {
      const teamEventIds: number [] = teamEvents.map(te => {
        return te.id;
      });
      return (teamEventIds?.length > 0);
    } else {
      return false;
    }
  }

  /**
   *
   * @protected
   */
  protected getEnteredTeamEvents(): TournamentEvent [] {
    const teamEvents: TournamentEvent [] = this.getTeamEvents();
    let enteredTeamEvents: TournamentEvent [] = [];
    if (teamEvents?.length > 0 && this.enteredEvents?.length > 0) {
      this.enteredEvents.filter(teei => {
        for (let i = 0; i < teamEvents.length; i++) {
          const teamEvent = teamEvents[i];
          if (teamEvent.id === teei.eventFk) {
            enteredTeamEvents.push(teamEvent);
          }
        }
        return enteredTeamEvents;
      });
    }
    return enteredTeamEvents;
  }

  /**
   *
   * @protected
   */
  protected enteredAnyTeamEvent(): boolean {
    const enteredTeamEvents = this.getEnteredTeamEvents();
    return enteredTeamEvents.length > 0;
  }

  protected getTeamForEvent(eventId: number): Team[] {
    let teamsForEvent: Team [] = (this.teams?.length > 0) ? this.teams.filter(team => team.tournamentEventFk === eventId) : [];
    // team not configured for this event, create it
    if (teamsForEvent?.length === 0 && this.playerProfile != null) {
      // const playerName = `${this.playerProfile?.lastName}, ${this.playerProfile?.firstName}`;
      // const playerRating = this.entry.eligibilityRating || 0;
      const entryPricePaid = this.getEntryPricePaid(eventId);
      // const teamMember: TeamMember = {
      //   id: null, teamFk: null, tournamentEntryFk: this.entry.id, tournamentEventFk: eventId,
      //   status: TeamEntryStatus.INVITED, isCaptain: true, playerName: playerName,
      //   playerRating: playerRating, profileId: this.playerProfile.userId, cartSessionId: null
      // };
      const teamMembers: TeamMember[] = []; // = [teamMember];
      const team: Team = {
        id: null, tournamentEventFk: eventId, teamMembers: teamMembers,
        name: 'my team name', teamRating: 0, entryPricePaid: entryPricePaid, dateEntered: new Date()
      };
      // return this
      teamsForEvent = [team];
      // update teams
      this.teams = teamsForEvent;
    } else {
      teamsForEvent = [...teamsForEvent];  // make a modifiable copy
    }
    return teamsForEvent;
  }

  private getEntryPricePaid(eventId: number): number {
    const entry = this.enteredEvents?.find(teei => teei.eventFk === eventId);
    return entry ? entry.price : 0;
  }

  saveTeamsIfDirty(f: NgForm) {
    // 1. Check if the global form is dirty
    if (!f.dirty) return;

    const teamsToUpdate: Team[] = [];

    this.teams.forEach(team => {
      const control = this.getTeamNameControl(team, f);

      // 2. Only update if THIS specific team name input was touched/changed
      if (control && control.dirty && control.valid) {
        teamsToUpdate.push(team);
      }
    });

    if (teamsToUpdate.length > 0) {
      this.performBulkUpdate(teamsToUpdate, f);
    }
  }

  private getTeamNameControl(team: Team, f: NgForm) {
    const controlName = `teamName_${team.tournamentEventFk}`;
    return f.controls[controlName];
  }

  private performBulkUpdate(teams: Team[], f: NgForm) {
    // Call your service - ideally a bulk update endpoint or individual calls
    // Once the service call is successful:
    teams.forEach(team => {
      const control = this.getTeamNameControl(team, f);
      control.markAsPristine();
    });

    teams.forEach(team => {
      this.onTeamChanged(team);
    });
  }

  protected onTeamChanged(updatedTeam: Team) {
    this.dirty = true;

    const teamEvents = this.getTeamEvents();
    if (teamEvents != null) {
      const teamEventIds: number [] = teamEvents.map(te => {
        return te.id;
      });
      this.teamChanged.emit({team: updatedTeam, teamEventIds: teamEventIds});
    }
  }
}
