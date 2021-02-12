import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from '../../user/authentication.service';
import {UsattPlayerRecordService} from '../../profile/service/usatt-player-record.service';
import {first, map} from 'rxjs/operators';
import {UsattPlayerRecord} from '../../profile/model/usatt-player-record.model';
import {DateUtils} from '../../shared/date-utils';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  playerFirstName: string;
  playerRating: string;
  membershipExpirationDate: Date;
  membershipExpired: boolean;
  loading$: Observable<boolean>;
  ratedPlayer: boolean;

  constructor(private authenticationService: AuthenticationService,
              private usattPlayerRecordService: UsattPlayerRecordService) {
    this.playerRating = '...';
    this.membershipExpirationDate = new Date();
    this.membershipExpired = false;
    this.ratedPlayer = false;
  }

  ngOnInit(): void {
    this.playerFirstName = this.authenticationService.getCurrentUserFirstName();
    const lastName = this.authenticationService.getCurrentUserLastName();
    this.loading$ = this.usattPlayerRecordService.loading$;
    const today = new Date();
    this.usattPlayerRecordService.searchByNames(this.playerFirstName, lastName)
      .pipe(first(),
        map((records: UsattPlayerRecord[]) => {
          if (records?.length > 0) {
            this.membershipExpirationDate = records[0].membershipExpiration;
            const rating = records[0].tournamentRating;
            this.ratedPlayer = (rating != null && rating > 0);
            this.playerRating =  this.ratedPlayer ? ('' + rating) : 'Unrated';
          } else {
            this.membershipExpirationDate = today;
          }
          this.membershipExpired = new DateUtils().isDateBefore(this.membershipExpirationDate, today);
        }))
      .subscribe();
  }
}
