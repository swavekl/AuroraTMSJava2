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

    this.playerFirstName = this.authenticationService.getCurrentUserFirstName();
    const lastName = this.authenticationService.getCurrentUserLastName();
    const today = new Date();
    this.usattPlayerRecordService.getByNames(this.playerFirstName, lastName)
      .pipe(first(),
        map((record: UsattPlayerRecord) => {
          if (record) {
            this.membershipExpirationDate = record.membershipExpiration;
            const rating = record.tournamentRating;
            this.ratedPlayer = (rating != null && rating > 0);
            this.playerRating =  this.ratedPlayer ? ('' + rating) : 'Unrated';
          } else {
            this.membershipExpirationDate = today;
          }
          this.membershipExpired = new DateUtils().isDateBefore(this.membershipExpirationDate, today);
        }))
      .subscribe();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
