import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
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
import {SanctionCategory, SanctionRequest, SanctionRequestStatus} from '../model/sanction-request.model';
import {MatDialog} from '@angular/material/dialog';
import {StatesList} from '../../shared/states/states-list';
import {CoordinatorInfo, coordinatorList} from '../../shared/coordinator-info';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';
import {AuthenticationService} from '../../user/authentication.service';
import {UserRoles} from '../../user/user-roles.enum';
import {PaymentRefund} from '../../account/model/payment-refund.model';
import {PaymentRefundStatus} from '../../account/model/payment-refund-status.enum';
import {SanctionRequestAndPayment} from './sanction-request-edit-container.component';
import {MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';
import {debounceTime, distinctUntilChanged, filter, first, map, skip, switchMap} from 'rxjs/operators';
import {Observable, Subscription} from 'rxjs';
import {UntypedFormControl} from '@angular/forms';
import {ClubAffiliationApplicationService} from '../../club/club-affiliation/service/club-affiliation-application.service';
import {ClubAffiliationApplication} from '../../club/club-affiliation/model/club-affiliation-application.model';
import {DateUtils} from '../../shared/date-utils';
import {OfficialSearchDialogComponent} from '../../officials/official-search-dialog/official-search-dialog.component';
import {Official} from '../../officials/model/official.model';
import {UmpireRankPipe} from '../../officials/pipes/umpire-rank.pipe';
import {RefereeRankPipe} from '../../officials/pipes/referee-rank.pipe';
import {UsattPlayerRecordService} from '../../profile/service/usatt-player-record.service';
import {UsattPlayerRecord} from '../../profile/model/usatt-player-record.model';
import {ErrorMessagePopupService} from '../../shared/error-message-dialog/error-message-popup.service';

@Component({
  selector: 'app-sanction-request-edit',
  templateUrl: './sanction-request-edit.component.html',
  styleUrls: ['./sanction-request-edit.component.scss']
})
export class SanctionRequestEditComponent implements OnInit, OnChanges, AfterViewInit, OnDestroy {
  @ViewChild('ESD') ESD: ElementRef;

  // this is what we edit
  @Input() sanctionRequest: SanctionRequest;

  @Input()
  public paymentsRefunds: PaymentRefund[] = [];

  // save and cancel
  @Output() saved = new EventEmitter();
  @Output() canceled = new EventEmitter();

  statesList: any [];
  associationCurrency: string;

  // current category index (lights, flooring etc)
  currentCategory: number;

  // total rating points
  totalPoints: number;

  // updated in case it changes
  venueState: string;

  minStartDate = new Date();
  maxStartDate = new Date();
  minAltStartDate = new Date();
  maxAltStartDate = new Date();

  minEndDate: Date;
  maxEndDate: Date;
  minAltEndDate: Date;
  maxAltEndDate: Date;

  endDateEnabled = false;
  altEndDateEnabled = false;

  // sanction fee to pay
  sanctionFee: number;
  // level for which tournament qualifies
  qualifiedStarLevel: number;

  @ViewChild('clubNameCtrl')
  private clubNameCtrl: UntypedFormControl;

  filteredClubs: any[] = [];

  sanctionFeeSchedule: any [] = [
    {low: 0, high: null, starLevel: 0, sanctionFee: 0},
    {low: 0, high: 400, starLevel: 1, sanctionFee: 40},
    {low: 401, high: 1000, starLevel: 2, sanctionFee: 80},
    {low: 1001, high: 3000, starLevel: 3, sanctionFee: 150},
    {low: 3001, high: 6000, starLevel: 3, sanctionFee: 300},
    {low: 6001, high: null, fee: 400, starLevel: 4, sanctionFee: 400},
  ];

  // sanctionFeeSchedule = [
  //   {starLevel: 0, sanctionFee: 0},
  //   {starLevel: 1, sanctionFee: 40},
  //   {starLevel: 2, sanctionFee: 80},
  //   {starLevel: 3, sanctionFee: 150},
  //   {starLevel: 4, sanctionFee: 400}
  // ];

  private subscriptions: Subscription = new Subscription();

  constructor(private messageDialog: MatDialog,
              private authenticationService: AuthenticationService,
              private cdr: ChangeDetectorRef,
              private clubAffiliationApplicationService: ClubAffiliationApplicationService,
              private usattPlayerRecordService: UsattPlayerRecordService,
              private errorMessagePopupService: ErrorMessagePopupService) {
    this.currentCategory = 0;
    this.totalPoints = 0;
    this.statesList = StatesList.getList();
    this.associationCurrency = 'USD';
    this.sanctionFee = 0;

    this.minStartDate.setDate(this.minStartDate.getDate() + 30);
    this.maxStartDate.setDate(this.maxStartDate.getDate() + 365);
    this.minAltStartDate.setDate(this.minStartDate.getDate() + 30);
    this.maxAltStartDate.setDate(this.maxAltStartDate.getDate() + 365);
  }

  ngOnInit() {
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  // called after Input changes
  ngOnChanges (simpleChanges: SimpleChanges) {
    if (simpleChanges.sanctionRequest != null) {
      const sanctionRequestSimpleChange: SimpleChange = simpleChanges.sanctionRequest;
      const sanctionRequestToEdit: SanctionRequest = sanctionRequestSimpleChange.currentValue;
      if (sanctionRequestToEdit != null) {
        // convert configurationJSON from JSON string into object
        const sanctionRequest: SanctionRequest = new SanctionRequest();
        sanctionRequest.clone(sanctionRequestToEdit);
        this.sanctionRequest = sanctionRequest;

        this.calculateTotalAndQualifiedStarLevel();

        this.sanctionFee = this.determineSanctionFee(sanctionRequest.starLevel, sanctionRequest.totalPrizeMoney);

        this.onEnableEndDate(sanctionRequest.startDate);
        this.onEnableAltEndDate(sanctionRequest.alternateStartDate);
      }
    }
  }

  onEnableEndDate(date: Date) {
    this.endDateEnabled = true;
    this.minEndDate = new Date(this.sanctionRequest.startDate.getTime());
    this.minEndDate.setDate(this.minEndDate.getDate());
    this.maxEndDate = new Date(this.sanctionRequest.startDate.getTime());
    this.maxEndDate.setDate(this.maxEndDate.getDate() + 7);
  }

  onEnableAltEndDate(date: Date) {
    this.altEndDateEnabled = true;
    this.minAltEndDate = new Date(this.sanctionRequest.alternateStartDate.getTime());
    this.minAltEndDate.setDate(this.minAltEndDate.getDate());
    this.maxAltEndDate = new Date(this.sanctionRequest.alternateStartDate.getTime());
    this.maxAltEndDate.setDate(this.maxAltEndDate.getDate() + 7);
  }

  setCategory(index: number) {
    this.currentCategory = index;
  }


  nextCategory() {
    this.currentCategory++;
  }

  prevCategory() {
    this.currentCategory--;
  }

  isCurrentCategory(index: number) {
    return this.currentCategory === index;
  }

  notFirstCategory(index: number) {
    return this.currentCategory > 0;
  }

  notLastCategory(index: number) {
    const totalCategories = this.sanctionRequest.categories.length;
    return (index !== (totalCategories - 1));
  }

  hasNextStep () {
    return this.isApprovingCoordinator();
  }

  /**
   * Save the sanction request
   */
  save(formValues: any, payFee: boolean) {
    const sanctionRequestToSave: SanctionRequest = this.sanctionRequest;
    const sanctionRequestAndPayment: SanctionRequestAndPayment = {
      sanctionRequest: sanctionRequestToSave,
      payFee: payFee
    };
    this.saved.emit (sanctionRequestAndPayment);
  }

  onCancel () {
    this.canceled.emit('cancelled');
  }

  // save and submit for sanction
  onSubmitApplication (formValues: any) {
    const sanctionRequestToSave: SanctionRequest = this.sanctionRequest;

    // find coordinator who will receive this request and set it in the request.
    // translate long name to short state name
    const longStateName = this.translateStateName(formValues.venueState);
    const starLevel = sanctionRequestToSave.starLevel;
    const coordinatorInfo: CoordinatorInfo = this.findCoordinator(longStateName, starLevel);

    // notify user about who will be getting this request
    let message = '';
    if (coordinatorInfo != null) {
      sanctionRequestToSave.starLevel = starLevel;
      sanctionRequestToSave.sanctionFee = this.determineSanctionFee(starLevel, sanctionRequestToSave.totalPrizeMoney);
      sanctionRequestToSave.coordinatorFirstName = coordinatorInfo.firstName;
      sanctionRequestToSave.coordinatorLastName = coordinatorInfo.lastName;
      sanctionRequestToSave.coordinatorEmail = coordinatorInfo.email;

      message += 'Your request will be submitted to ';
      message += coordinatorInfo.firstName + ' ' + coordinatorInfo.lastName;
      message += ' who is the ' + coordinatorInfo.region + ' region Sanction Coordinator.';
      message += ' You may follow up with him by phone ' + coordinatorInfo.phone;
      message += ' or email ' + coordinatorInfo.email;

      const dialogRef = this.messageDialog.open(ConfirmationPopupComponent, {
        width: '450px', height: '240px',
        data: { message: message, title: 'Submission Information', contentAreaHeight: 200 }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result == 'ok') {
          sanctionRequestToSave.status = SanctionRequestStatus.Submitted;
          const sanctionRequestAndPayment: SanctionRequestAndPayment = {
            sanctionRequest: sanctionRequestToSave,
            payFee: false
          };

          // mark it a submitted and save
          this.saved.emit (sanctionRequestAndPayment);
        }
      });
    } else {
      this.errorMessagePopupService.showError(`Unable to find Sanctioning Coordinator for state ${longStateName}`);
    }
  }

  onApproveApplication(formValues: any) {
    this.sanctionRequest.status = SanctionRequestStatus.Approved;
    this.save(formValues, false);
  }

  onRejectApplication(formValues: any) {
    this.sanctionRequest.status = SanctionRequestStatus.Rejected;
    this.save(formValues, false);
  }

  isSubmitEnabled() {
    return this.sanctionRequest.status === SanctionRequestStatus.New ||
      this.sanctionRequest.status === SanctionRequestStatus.Rejected;
  }

  isApproveRejectEnabled() {
    const statusOK = this.sanctionRequest.status === SanctionRequestStatus.Submitted;
    const isPermitted = this.authenticationService.hasCurrentUserRole(
      [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_SANCTION_COORDINATORS]);
    return isPermitted && statusOK;
  }

  onPay(formValues: any) {
    this.save(formValues, true);
  }

  isPaymentEnabled(): boolean {
    return this.sanctionRequest.status === SanctionRequestStatus.Approved;
  }

  isPayment(paymentRefund: PaymentRefund) {
    return paymentRefund.status === PaymentRefundStatus.PAYMENT_COMPLETED;
  }

  /**
   * Translate IL to Illinois
   */
  translateStateName (stateAbbreviation: string): string {
    const stateList: any [] = StatesList.getList();
    for (let i = 0; i <  stateList.length; i++) {
      const stateObj = stateList[i];
      if (stateObj.abbreviation === stateAbbreviation) {
        return stateObj.name;
      }
    }
    return null;
  }

  /**
   * Finds regional or national coordinator
   */
  findCoordinator (state: string, starLevel: number): CoordinatorInfo {
    let coordinatorInfo: CoordinatorInfo = null;
    if (starLevel >= 4) {
      // national coordinator
      for (let i = 0; i < coordinatorList.length; i++) {
        if (coordinatorList[i].region === 'National') {
          coordinatorInfo = coordinatorList[i];
          break;
        }
      }
    } else {
      // regional coordinator
      for (let i = 0; i < coordinatorList.length; i++) {
        const cinfo: CoordinatorInfo = coordinatorList[i];
        let found = false;
        const states = cinfo.states;
        for (let k = 0; k < states.length; k++) {
          if (states[k] === state) {
            found = true;
            break;
          }
        }

        if (found) {
          coordinatorInfo = cinfo;
          break;
        }
      }
    }

    return coordinatorInfo;
  }

  isApprovingCoordinator () {
    // check if the current user is a sanction coordinator
    // if not don't show the approve/reject step
    return false;
  }

  // event handler for when radio button is clicked
  onRadioGroupChange(event) {
    this.calculateTotalAndQualifiedStarLevel();
  }

  // event handler for when checkbox button is clicked
  onCheckBoxChange (event) {
    this.calculateTotalAndQualifiedStarLevel();
  }

  private calculateTotalAndQualifiedStarLevel() {
    this.totalPoints = this.calculateTotal();
    this.qualifiedStarLevel = this.getQualifiedStarLevel();
  }

  // recalculates total rating points when selection changes
  calculateTotal () {
    let total = 0;
    if (this.sanctionRequest) {
      const categories = this.sanctionRequest.categories;
      for (let i = 0; i < categories.length; i++) {
        const category: SanctionCategory = categories[i];
        total += category.getSubTotal();
      }
    }
    return total;
  }

  // calculates which star level user qualifies for based on the rating points total
  getQualifiedStarLevel () {
    let qualifiedStarLevel = 0;
    if (this.totalPoints <= 10) {
      qualifiedStarLevel = 0;
    } else if (this.totalPoints > 10 && this.totalPoints <= 20) {
      qualifiedStarLevel = 1;
    } else if (this.totalPoints > 20 && this.totalPoints <= 30) {
      qualifiedStarLevel = 2;
    } else if (this.totalPoints > 30 && this.totalPoints <= 40) {
      qualifiedStarLevel = 3;
    } else if (this.totalPoints > 40 && this.totalPoints <= 50) {
      qualifiedStarLevel = 4;
    } else {
      qualifiedStarLevel = 5;
    }
    return qualifiedStarLevel;
  }

  onVenueStateChange (event) {
    //   console.log ('in onVenueStateChange ', event);
    this.venueState = event.value;
  }

  starLevelChanged(event) {
    console.log ('in starLevelChanged ', event);
    const starLevel = event.srcElement.value;
    const stateName = this.venueState;
//      console.log ('starLevel ', starLevel);
    //     console.log ('stateName ', stateName);
    // find coordinator who will receive this request and set it in the request.
    const longStateName = this.translateStateName(stateName);
    const coordinatorInfo: CoordinatorInfo = this.findCoordinator(longStateName, starLevel);
//      console.log ('coordinatorInfo ', coordinatorInfo);
    this.sanctionFee = this.determineSanctionFee(starLevel, this.sanctionRequest.totalPrizeMoney);
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

  private determineSanctionFee(strStarLevel: any, totalPrizeMoney: any) {
    const starLevel = Number(strStarLevel);
    let sanctionFee = 0;
    if (totalPrizeMoney != null) {
      for (let i = 0; i < this.sanctionFeeSchedule.length; i++) {
        const sanctionFeeScheduleElement = this.sanctionFeeSchedule[i];
        if (sanctionFeeScheduleElement.low <= totalPrizeMoney &&
          totalPrizeMoney <= sanctionFeeScheduleElement.high) {
          sanctionFee = sanctionFeeScheduleElement.sanctionFee;
          break;
        }
      }
    }
    // console.log('starLevel ' + starLevel + ' => sanctionFee: ' + sanctionFee);
    return sanctionFee;
  }

  ngAfterViewInit(): void {
    this.initClubFilter();
  }

  initClubFilter() {
    if (this.clubNameCtrl != null) {
      // whenever the home club name changes reload the list of clubs matching this string
      // for auto completion
      const subscription = this.clubNameCtrl.valueChanges
        .pipe(
          distinctUntilChanged(),
          debounceTime(250),
          skip(1), // skip form initialization phase - to save one trip to the server
          filter(clubName => {
            // don't query until you have a few characters
            return clubName && clubName.length >= 3;
          }),
          switchMap((clubName): Observable<ClubAffiliationApplication []> => {
            const params = `?nameContains=${clubName}&sort=name,ASC&latest=true`;
            return this.clubAffiliationApplicationService.getWithQuery(params);
          })
        ).subscribe((clubAffiliationApplications: ClubAffiliationApplication []) => {
          this.filteredClubs = clubAffiliationApplications.map((clubAffiliationApplication: ClubAffiliationApplication) => {
            return {
              clubName: clubAffiliationApplication.name,
              affiliationExpirationDate: clubAffiliationApplication.affiliationExpirationDate
            }
          });
          console.log('got filtered clubs', this.filteredClubs);
          // // refresh the drop down contents and show it
          // this.cdr.markForCheck();
        });
      this.subscriptions.add(subscription);
    }
  }

  clearClubName() {
    this.updateClubName(null, null);
  }

  onClubSuggestionSelected($event: MatAutocompleteSelectedEvent) {
    const clubName: string = $event.option.value;
    let expirationDate = null;
    for (let i = 0; i < this.filteredClubs.length; i++) {
      const filteredClub = this.filteredClubs[i];
      if (filteredClub.clubName === clubName) {
        expirationDate = new DateUtils().convertFromString(filteredClub.affiliationExpirationDate);
        break;
      }
    }
    this.updateClubName(clubName, expirationDate);
  }

  private updateClubName(clubName: string, expirationDate: Date) {
    const cloneSanctionRequest: SanctionRequest = new SanctionRequest();
    cloneSanctionRequest.clone(this.sanctionRequest);
    cloneSanctionRequest.clubName = clubName;
    cloneSanctionRequest.clubAffiliationExpiration = expirationDate;
    this.sanctionRequest = cloneSanctionRequest;
    this.cdr.markForCheck();
  }

  showRefereeSearchDialog() {
    const dialogRef = this.messageDialog.open(OfficialSearchDialogComponent, {
      width: '400px',
      data: {officialType: 'referee'}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'ok') {
        const official: Official = result.official;
        let subscription = this.getRefereeMembershipExpirationDate (official.membershipId, official.firstName, official.lastName)
          .pipe(first()).subscribe((membershipExpirationDate: Date) => {
          const cloneSanctionRequest: SanctionRequest = new SanctionRequest();
          cloneSanctionRequest.clone(this.sanctionRequest);
          cloneSanctionRequest.tournamentRefereeName = official.firstName + ' ' + official.lastName;
          cloneSanctionRequest.tournamentRefereeRank = this.getRefereeRank(official);
          cloneSanctionRequest.tournamentRefereeMembershipExpires = membershipExpirationDate;
          this.sanctionRequest = cloneSanctionRequest;
        }, error => {
            const message: string = `Unable to find membership expiration date for
            ${official.firstName} ${official.lastName} with membership id ${official.membershipId}.\nError: ${error}`;
           this.errorMessagePopupService.showError(message, "400px", "200px");
          });
        this.subscriptions.add(subscription);
      }
    });
  }

  private getRefereeRank(official: Official): string {
    const strUmpireRank: string = (official.umpireRank != null) ? new UmpireRankPipe().transform(official.umpireRank) : '';
    const strRefereeRank: string = (official.refereeRank != null) ? new RefereeRankPipe().transform(official.refereeRank) : '';
    let combinedRank = '';
    if (strUmpireRank != '' && strRefereeRank != '') {
      combinedRank = `${strUmpireRank} / ${strRefereeRank}`;
    } else if (strUmpireRank != '') {
      combinedRank = strUmpireRank;
    } else if (strRefereeRank != '') {
      combinedRank = strRefereeRank;
    }
    return combinedRank;
  }

  private getRefereeMembershipExpirationDate(membershipId: number, firstName: string, lastName: string): Observable<Date> {
    if (membershipId != 0) {
      return this.usattPlayerRecordService.getByMembershipId(membershipId)
        .pipe(first(),
          map((playerRecord: UsattPlayerRecord) => {
            return new DateUtils().convertFromString(playerRecord.membershipExpirationDate);
          }));
    } else {
      return this.usattPlayerRecordService.getByNames(firstName, lastName)
        .pipe(first(),
          map((playerRecord: UsattPlayerRecord) => {
            return (playerRecord != null) ? new DateUtils().convertFromString(playerRecord.membershipExpirationDate) : null;
          }));
    }
  }

  refreshRefereeMembershipExpirationDate() {
    if (this.sanctionRequest?.tournamentRefereeName != null) {
      const nameParts = this.sanctionRequest.tournamentRefereeName.split(" ");
      if (nameParts.length >= 2) {
        const firstName = nameParts[0];
        const lastName = nameParts[1];
        const subscription = this.getRefereeMembershipExpirationDate(0, firstName, lastName)
          .subscribe((membershipExpirationDate: Date) => {
            if (membershipExpirationDate != null) {
              const cloneSanctionRequest: SanctionRequest = new SanctionRequest();
              cloneSanctionRequest.clone(this.sanctionRequest);
              cloneSanctionRequest.tournamentRefereeMembershipExpires = membershipExpirationDate;
              this.sanctionRequest = cloneSanctionRequest;
            } else {
              const message: string = `Unable to find membership expiration date for
              ${firstName} ${lastName} referee.\nPlayer record not found`;
              this.errorMessagePopupService.showError(message, "400px", "200px");
            }
          }, error => {
            const message: string = `Unable to find membership expiration date for
              ${firstName} ${lastName} referee.\nError: ${error}`;
            this.errorMessagePopupService.showError(message, "400px", "200px");
          });
        this.subscriptions.add(subscription);
      }
    }
  }

  onTotalPrizeMoneyChanged($event: any) {
    const totalPrizeMoney = $event?.target?.value;
    if (this.sanctionRequest != null && totalPrizeMoney!= null && totalPrizeMoney >= 0) {
      const categories: SanctionCategory [] = this.sanctionRequest.categories;
      for (let i = 0; i < categories.length; i++) {
        const category: SanctionCategory = categories[i];
        if (category.name === 'prizeMoney') {
          let points = 0;
          if (totalPrizeMoney >= 100 && totalPrizeMoney <= 400) {
            points = 1;
          } else if (totalPrizeMoney >= 401 && totalPrizeMoney <= 1000) {
            points = 3;
          } else if (totalPrizeMoney >= 1001 && totalPrizeMoney <= 3000) {
            points = 5;
          } else if (totalPrizeMoney >= 3001 && totalPrizeMoney <= 6000) {
            points = 7;
          } else if (totalPrizeMoney >= 6001 && totalPrizeMoney <= 10000) {
            points = 10;
          } else {
            points = 15;
          }
          if (points != category.selectedValue) {
            const cloneSanctionRequest: SanctionRequest = new SanctionRequest();
            cloneSanctionRequest.clone(this.sanctionRequest);
            cloneSanctionRequest.categories[i].selectedValue = points;
            this.sanctionRequest = cloneSanctionRequest;
          }
          break;
        }
      }
    }
  }

  isUploadDisabled(): boolean {
    return this.sanctionRequest?.status != SanctionRequestStatus.New
      && this.sanctionRequest?.status != SanctionRequestStatus.Rejected;
  }

  onUploadFinished(downloadUrl: string) {
    this.sanctionRequest.blankEntryFormUrl = downloadUrl;
  }

  getStoragePath() {
    return (this.sanctionRequest?.id != null)
      ? `sanction_request/${this.sanctionRequest.id}/blankentryform`
      : null;
  }
}
