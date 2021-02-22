import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthenticationService} from '../../user/authentication.service';
import {UsattPlayerRecordService} from '../../profile/service/usatt-player-record.service';
import {first, map} from 'rxjs/operators';
import {UsattPlayerRecord} from '../../profile/model/usatt-player-record.model';
import {DateUtils} from '../../shared/date-utils';
import {Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';

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
              private linearProgressBarService: LinearProgressBarService) {
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
}
