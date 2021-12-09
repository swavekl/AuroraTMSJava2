import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ClubAffiliationApplication} from '../model/club-affiliation-application.model';
import {StatesList} from '../../shared/states/states-list';
import {PlayingSite} from '../model/playing-site';
import {Router} from '@angular/router';
import {ClubAffiliationApplicationStatus} from '../model/club-affiliation-application-status';
import {UserRoles} from '../../user/user-roles.enum';
import {AuthenticationService} from '../../user/authentication.service';
import {PaymentRefund} from '../../account/model/payment-refund.model';
import {PaymentRefundStatus} from '../../account/model/payment-refund-status.enum';

@Component({
  selector: 'app-club-affiliation-application',
  templateUrl: './club-affiliation-application.component.html',
  styleUrls: ['./club-affiliation-application.component.scss']
})
export class ClubAffiliationApplicationComponent implements OnInit {

  @Input()
  public clubAffiliationApplication: ClubAffiliationApplication;

  @Input()
  public paymentsRefunds: PaymentRefund[] = [];

  @Output()
  public saved: EventEmitter<ApplicationAndPayment> = new EventEmitter<ApplicationAndPayment>();

  statesList: any[] = [];
  associationCurrency: string;

  constructor(private router: Router,
              private authenticationService: AuthenticationService) {
    this.statesList = StatesList.getCountryStatesList('US');
    this.associationCurrency = 'USD';
  }

  ngOnInit(): void {
  }

  onApproveApplication() {
    this.clubAffiliationApplication.status = ClubAffiliationApplicationStatus.Approved;
    this.onSave(false);
  }

  onRejectApplication() {
    this.clubAffiliationApplication.status = ClubAffiliationApplicationStatus.Rejected;
    this.onSave(false);
  }

  onSubmitApplication() {
    this.clubAffiliationApplication.status = ClubAffiliationApplicationStatus.Submitted;
    this.onSave(false);
  }

  onSave(payFee: boolean) {
    const applicationAndPayment: ApplicationAndPayment = {
      clubAffiliationApplication: this.clubAffiliationApplication,
      payFee: payFee
    };
    this.saved.emit(applicationAndPayment);
  }

  onCancel() {
    this.router.navigateByUrl('/club/affiliationlist');
  }

  isApproveRejectEnabled() {
    const statusOK = this.clubAffiliationApplication.status === ClubAffiliationApplicationStatus.Submitted;
    const isPermitted = this.authenticationService.hasCurrentUserRole(
      [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_CLUB_MANAGERS]);
    return isPermitted && statusOK;
  }

  isSubmitEnabled() {
    return this.clubAffiliationApplication.status === ClubAffiliationApplicationStatus.New ||
           this.clubAffiliationApplication.status === ClubAffiliationApplicationStatus.Rejected;
  }

  isPaymentEnabled(): boolean {
    return this.clubAffiliationApplication.status === ClubAffiliationApplicationStatus.Approved;
  }

  onAddPlayingSite() {
    const clone: ClubAffiliationApplication = JSON.parse(JSON.stringify(this.clubAffiliationApplication));
    const alternatePlayingSites: PlayingSite [] = clone.alternatePlayingSites || [];
    alternatePlayingSites.push(new PlayingSite());
    clone.alternatePlayingSites = alternatePlayingSites;
    this.clubAffiliationApplication = clone;
  }

  onRemovePlayingSite(playingSiteIndex: number) {
    const clone: ClubAffiliationApplication = JSON.parse(JSON.stringify(this.clubAffiliationApplication));
    const alternatePlayingSites: PlayingSite [] = clone.alternatePlayingSites || [];
    alternatePlayingSites.splice(playingSiteIndex, 1);
    clone.alternatePlayingSites = alternatePlayingSites;
    this.clubAffiliationApplication = clone;
  }

  canSetExpirationDate() {
    const statusOK = this.clubAffiliationApplication.status === ClubAffiliationApplicationStatus.Submitted ||
      this.clubAffiliationApplication.status === ClubAffiliationApplicationStatus.Approved ||
      this.clubAffiliationApplication.status === ClubAffiliationApplicationStatus.Completed;
    const isPermitted = this.authenticationService.hasCurrentUserRole(
      [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_CLUB_MANAGERS]);
    return isPermitted && statusOK;
  }

  isPayment(paymentRefund: PaymentRefund) {
    return paymentRefund.status === PaymentRefundStatus.PAYMENT_COMPLETED;
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
}

export interface ApplicationAndPayment {
  clubAffiliationApplication: ClubAffiliationApplication;
  payFee: boolean;
}
