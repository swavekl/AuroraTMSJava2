import {ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {MembershipType, TournamentEntry} from '../model/tournament-entry.model';
import {ProfileFindPopupComponent} from '../../../profile/profile-find-popup/profile-find-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {BehaviorSubject} from 'rxjs';
import {FormGroup} from '@angular/forms';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {EventEntryStatus} from '../model/event-entry-status.enum';
import {AvailabilityStatus} from '../model/availability-status.enum';
import {EventEntryCommand} from '../model/event-entry-command.enum';
import {Profile} from '../../../profile/profile';
import {DateUtils} from '../../../shared/date-utils';
import {PaymentRefund} from '../../../account/model/payment-refund.model';
import {CallbackData} from '../../../account/model/callback-data';
import {PaymentDialogData} from '../../../account/payment-dialog/payment-dialog-data';
import {PaymentRefundFor} from '../../../account/model/payment-refund-for.enum';
import {PaymentRefundStatus} from '../../../account/model/payment-refund-status.enum';
import {PaymentRequest} from '../../../account/model/payment-request.model';
import {RefundRequest} from '../../../account/model/refund-request.model';
import {PaymentDialogService} from '../../../account/service/payment-dialog.service';
import {RefundDialogService} from '../../../account/service/refund-dialog.service';
import {first} from 'rxjs/operators';
import {CurrencyService} from '../../../account/service/currency.service';
import {getCurrencySymbol} from '@angular/common';
import {Tournament} from '../../tournament-config/tournament.model';
import {PriceCalculator} from '../pricecalculator/price-calculator';
import {PricingMethod} from '../../model/pricing-method.enum';
import {StandardPriceCalculator} from '../pricecalculator/standard-price-calculator';
import {DiscountedPriceCalculator} from '../pricecalculator/discounted-price-calculator';

@Component({
  selector: 'app-entry-wizard',
  templateUrl: './entry-wizard.component.html',
  styleUrls: ['./entry-wizard.component.scss']
})
export class EntryWizardComponent implements OnInit, OnChanges {

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

  columnsToDisplay: string[] = ['name', 'action'];

  tournamentStartDate: Date;

  teamsTournament: boolean;
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

  // calculator for calculating total
  public priceCalculator: PriceCalculator;

  // balance actions
  public readonly BALANCE_ACTION_PAY = 1;
  public readonly BALANCE_ACTION_REFUND = 2;
  public readonly BALANCE_ACTION_CONFIRM = 3;
  public readonly BALANCE_ACTION_NOCHANGE = 4;

  // indicates if user made any changes
  private dirty: boolean;

  public membershipOptions: any [] = [
    {value: MembershipType.NO_MEMBERSHIP_REQUIRED.valueOf(), label: 'My Membership is up to date', cost: 0, available: true},
    {value: MembershipType.TOURNAMENT_PASS_JUNIOR.valueOf(), label: 'Tournament Pass Junior (17 and under)', cost: 20, available: true},
    {value: MembershipType.TOURNAMENT_PASS_ADULT.valueOf(), label: 'Tournament Pass Adult', cost: 50, available: true},
    {value: MembershipType.BASIC_PLAN.valueOf(), label: 'Basic Plan 1 year (0 – 4 star)', cost: 25, available: true},
    {value: MembershipType.PRO_PLAN.valueOf(), label: 'Pro Plan 1 year', cost: 75, available: true},
    {value: MembershipType.LIFETIME.valueOf(), label: 'Lifetime', cost: 1300, available: true}
  ];

  constructor(private dialog: MatDialog,
              private _change: ChangeDetectorRef,
              private paymentDialogService: PaymentDialogService,
              private refundDialogService: RefundDialogService,
              private currencyService: CurrencyService) {
    this.paymentsRefunds = [];
    this.tournamentCurrency = 'USD';
    this.playerCurrency = 'USD';
    this.currencyExchangeRate = 1.0;
    this.dirty = false;
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.allEventEntryInfos != null) {
      this.allEventEntryInfos = changes.allEventEntryInfos.currentValue || [];
      this.enteredEvents = this.allEventEntryInfos.filter(this.enteredEventsFilter, this);
      this.availableEvents = this.allEventEntryInfos.filter(this.availableEventsFilter, this);
      this.unavailableEvents = this.allEventEntryInfos.filter(this.unavailableEventsFilter, this);
    }

    const playerProfileChange: SimpleChange = changes.playerProfile;
    if (playerProfileChange != null) {
      this.playerProfile = playerProfileChange.currentValue;
    }

    const tournamentChange: SimpleChange = changes.tournament;
    if (tournamentChange != null) {
      this.tournament = tournamentChange.currentValue;
      if (this.tournament != null) {
        this.teamsTournament = this.tournament.configuration.tournamentType === 'Teams';
        console.log('tournament start date is ' + this.tournament.startDate);
        this.tournamentStartDate = new DateUtils().convertFromString(this.tournament.startDate);
        this.tournamentStarLevel = this.tournament.starLevel;
      }
    }

    // when both are ready proceed with some calculations
    if (this.playerProfile != null && this.tournament != null) {
      const dateOfBirth = this.playerProfile.dateOfBirth;
      this.hideMembershipOptions(dateOfBirth, this.tournamentStartDate, this.tournamentStarLevel);
      // fetch account information in case they want to pay so the payment dialog comes up faster
      this.prepareForPayment(this.tournament.id);
      // check if membership is expired
      this.membershipIsExpired = new DateUtils().isDateBefore(
        this.playerProfile.membershipExpirationDate, this.tournamentStartDate);

      this.priceCalculator = this.initPricingCalculator(this.tournament.configuration.pricingMethod);
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
    console.log('formValues', formValues);
    this.finish.emit(null);
  }

  editEntry(profileId: number) {

  }

  onChange(form: FormGroup) {
    console.log('in onChange');
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
        this.eventEntryChanged.emit(eventEntryInfo);
      }
    }
  }

  private isPlayerAJunior(dateOfBirth: Date, tournamentStartDate: Date) {
    const ageOnTournamentStartDate = new DateUtils().getAgeOnDate(dateOfBirth, tournamentStartDate);
    return ageOnTournamentStartDate < 18;
  }

  /**
   * Initializes pricing calculator
   * @param pricingMethod
   * @private
   */
  private initPricingCalculator(pricingMethod: PricingMethod) {
    const isJunior = this.isPlayerAJunior(this.playerProfile.dateOfBirth, this.tournament.startDate);
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

  /**
   * Gets current player total regardless of previous payments or refunds
   */
  getTotal(): number {
    const membershipOption: MembershipType = this.entry?.membershipOption;
    const usattDonation = this.entry?.usattDonation ?? 0;
    let total = 0;
    if (this.priceCalculator) {
      total = this.priceCalculator.getTotalPrice(membershipOption, usattDonation, this.enteredEvents);
    }
    return total;
  }

  /**
   * Gets a list of report items to be painted on the summary screen
   */
  getSummaryReportItems() {
    if (this.priceCalculator) {
      const totalPrice = this.priceCalculator.getTotalPrice(this.entry?.membershipOption, this.entry?.usattDonation, this.enteredEvents);
      return this.priceCalculator.getSummaryReportItems();
    } else {
      return [];
    }
  }

  /**
   * Gets balance due (positive - payment due, negative - refund due, zero - just confirm)
   */
  getBalance(): number {
    let total = this.getTotal();

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

  /**
   * Show payment dialog for amount and currency to pay in
   * @param balanceInPlayerCurrency balance in currency to pay in
   * @param balanceInTournamentCurrency balance in currency to pay in tournament currency
   * @param currencyOfPayment 3 letter currency code of currency to use
   */
  onPayPlayerTotalInCurrency(balanceInPlayerCurrency: number, balanceInTournamentCurrency: number, currencyOfPayment: string) {
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
    this.paymentDialogService.showPaymentDialog(paymentDialogData, callbackData);
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
    this.confirmEntry();
  }

  onIssueRefundInPlayerCurrency(refundAmountInPlayerCurrency: number, refundAmountInTournamentCurrency: number) {
    this.onIssueRefund(refundAmountInPlayerCurrency, refundAmountInTournamentCurrency, this.playerCurrency);
  }

  onIssueRefundInTournamentCurrency(refundAmountInTournamentCurrency: number) {
    this.onIssueRefund(refundAmountInTournamentCurrency, refundAmountInTournamentCurrency, this.tournamentCurrency);
  }

  /**
   * Issues a refund
   */
  onIssueRefund(refundAmountInPlayerCurrency: number, refundAmountInTournamentCurrency: number, refundCurrency: string) {
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
    this.refundDialogService.showRefundDialog(refundRequest, callbackData);
  }

  public onRefundSuccessful(scope: any): void {
    console.log('refund successful');
    if (scope != null) {
      scope.confirmEntry();
    }
  }

  public onRefundCanceled(scope: any): void {
    console.log('refund cancelled');
  }

  confirmEntry() {
    console.log('confirming entry');
    const confirmEntry = {
      ...this.entry,
      confirm: true
    };
    this.dirty = false;
    this.confirmEntries.emit(confirmEntry);
  }

  /**
   *
   * @param dateOfBirth
   * @param tournamentStartDate
   * @param tournamentStarLevel
   * @private
   */
  private hideMembershipOptions(dateOfBirth: Date, tournamentStartDate: Date, tournamentStarLevel: number) {
    if (dateOfBirth != null && tournamentStartDate != null) {
      const isJunior = this.isPlayerAJunior(dateOfBirth, this.tournamentStartDate);
      this.membershipOptions.forEach((membershipOption: any) => {
        switch (membershipOption.value) {
          case MembershipType.TOURNAMENT_PASS_JUNIOR:
            membershipOption.available = isJunior;
            break;
          case MembershipType.TOURNAMENT_PASS_ADULT:
            membershipOption.available = !isJunior;
            break;
          case MembershipType.PRO_PLAN:
            break;
          case MembershipType.BASIC_PLAN:
            membershipOption.available = (tournamentStarLevel >= 0 && tournamentStarLevel <= 4);
            break;
        }
      });
    }
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
      this.paymentDialogService.prepareForPayment(tournamentId)
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
            console.log('Unable to prepare for payment ' + JSON.stringify(error));
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
        },
        (error: any) => {
          console.log('Unable to get currency exchange rate for player' + JSON.stringify(error));
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
        return 'Enter W.L.';
      case EventEntryCommand.DROP:
        return 'Drop';
      case EventEntryCommand.DROP_WAITING_LIST:
        return 'Drop W.L.';
      case EventEntryCommand.REVERT_DROP:
        return 'Revert Drop';
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

}
