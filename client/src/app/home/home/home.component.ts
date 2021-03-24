import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthenticationService} from '../../user/authentication.service';
import {UsattPlayerRecordService} from '../../profile/service/usatt-player-record.service';
import {first, map} from 'rxjs/operators';
import {UsattPlayerRecord} from '../../profile/model/usatt-player-record.model';
import {DateUtils} from '../../shared/date-utils';
import {Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {PaymentData, PaymentRefundFor} from '../../account/payment-dialog/payment-data';
import {MatDialog} from '@angular/material/dialog';
import {PaymentDialogService} from '../../account/service/payment-dialog.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, OnDestroy {

  playerFirstName: string;
  playerRating: string;
  membershipExpirationDate: Date;
  membershipExpired: boolean;
  ratedPlayer: boolean;

  private subscriptions: Subscription = new Subscription();

  constructor(private authenticationService: AuthenticationService,
              private usattPlayerRecordService: UsattPlayerRecordService,
              private linearProgressBarService: LinearProgressBarService,
              private dialog: MatDialog,
              private paymentDialogService: PaymentDialogService) {
    this.playerRating = '...';
    this.membershipExpirationDate = new Date();
    this.membershipExpired = false;
    this.ratedPlayer = false;
  }

  ngOnInit(): void {
    // subscription for indicating progress on global toolbar
    const subscription = this.usattPlayerRecordService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(subscription);

    // if user is not fully registered (no profile is completed) he/she won't have membership Id
    // so then lookup information by first and last name. This will also let us deal with name changes.
    // Ed vs Edward
    const membershipId = this.authenticationService.getCurrentUserMembershipId();
    this.playerFirstName = this.authenticationService.getCurrentUserFirstName();
    const lastName = this.authenticationService.getCurrentUserLastName();
    const today = new Date();
    if (membershipId) {
      this.usattPlayerRecordService.getByMembershipId(membershipId)
        .pipe(first(),
          map((usattPlayerRecord: UsattPlayerRecord) => {
            this.processPlayerRecord(usattPlayerRecord, today);
          }))
        .subscribe();
    } else {
      this.usattPlayerRecordService.getByNames(this.playerFirstName, lastName)
        .pipe(first(),
          map((usattPlayerRecord: UsattPlayerRecord) => {
            this.processPlayerRecord(usattPlayerRecord, today);
          }))
        .subscribe();
    }
  }

  private processPlayerRecord(record: UsattPlayerRecord, today: Date) {
    if (record) {
      this.membershipExpirationDate = record.membershipExpirationDate;
      const rating = record.tournamentRating;
      this.ratedPlayer = (rating != null && rating > 0);
      this.playerRating = this.ratedPlayer ? ('' + rating) : 'Unrated';
    } else {
      this.membershipExpirationDate = today;
    }
    this.membershipExpired = new DateUtils().isDateBefore(this.membershipExpirationDate, today);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  // onPayNow() {
  //   const currentUser = this.authenticationService.getCurrentUser();
  //   const paymentData: PaymentData = {
  //     paymentRefundFor: PaymentRefundFor.TOURNAMENT_ENTRY,
  //     itemId: 11,
  //     amount: 12056,
  //     fullName: 'Julia Lorenc',
  //     postalCode: '60444',
  //     successCallbackFn: this.onPaymentSuccessful,
  //     cancelCallbackFn: this.onPaymentCanceled,
  //     stripeInstance: null
  //   };
  //
  //   this.paymentDialogService.showPaymentDialog(paymentData);
  // }
  //
  // onPaymentSuccessful (scope: any) {
  //
  // }
  //
  // onPaymentCanceled (scope: any) {
  //
  // }
}
