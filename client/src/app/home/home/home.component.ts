import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthenticationService} from '../../user/authentication.service';
import {UsattPlayerRecordService} from '../../profile/service/usatt-player-record.service';
import {first, map} from 'rxjs/operators';
import {UsattPlayerRecord} from '../../profile/model/usatt-player-record.model';
import {DateUtils} from '../../shared/date-utils';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {TournamentEntry} from '../../tournament/tournament-entry/model/tournament-entry.model';
import {TournamentEntryService} from '../../tournament/tournament-entry/service/tournament-entry.service';
import * as moment from 'moment';
import {Router} from '@angular/router';
import {TodayService} from '../../shared/today.service';
import {createSelector} from '@ngrx/store';
import {TournamentInfo} from '../../tournament/model/tournament-info.model';
import {TournamentInfoService} from '../../tournament/service/tournament-info.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, OnDestroy {

  playerFirstName: string;
  // current user rating
  playerRating: string;
  // USATT membership expiration date
  membershipExpirationDate: Date;
  // true if membership expired
  membershipExpired: boolean;
  // true if player has a rating
  ratedPlayer: boolean;
  // if true player players in a tournament today
  tournamentDay: boolean;
  // this players entry into today's tournament
  todaysTournamentEntryId: number;
  // today's tournament id in which player participates
  todaysTournamentId: number;

  private loading$: Observable<boolean>;

  /**
   * ctor
   * @param authenticationService
   * @param router
   * @param usattPlayerRecordService
   * @param tournamentEntryService
   * @param todayService
   * @param linearProgressBarService
   */
  constructor(private authenticationService: AuthenticationService,
              private router: Router,
              private usattPlayerRecordService: UsattPlayerRecordService,
              private tournamentEntryService: TournamentEntryService,
              private tournamentInfoService: TournamentInfoService,
              private todayService: TodayService,
              private linearProgressBarService: LinearProgressBarService) {
    this.playerRating = '...';
    this.membershipExpirationDate = new Date();
    this.membershipExpired = false;
    this.ratedPlayer = false;
    this.tournamentDay = false;
    this.todaysTournamentId = 0;
    this.todaysTournamentEntryId = 0;
    this.setupProgressIndicator();
    this.loadUsattPlayerRecord();
    this.loadTodaysTournamentEntry();
  }

  private subscriptions: Subscription = new Subscription();

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  /**
   *
   * @private
   */
  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.usattPlayerRecordService.loading$,
      this.tournamentEntryService.loading$,
      this.tournamentInfoService.loading$,
      (playerRecordLoading: boolean, tournamentEntryLoading: boolean, tournamentInfoLoading: boolean) => {
        return playerRecordLoading || tournamentEntryLoading || tournamentInfoLoading;
      }
    );
    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  /**
   *
   * @private
   */
  private loadUsattPlayerRecord() {
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

  /**
   *
   * @param record
   * @param today
   * @private
   */
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

  /**
   *
   * @private
   */
  private loadTodaysTournamentEntry() {
    // see if player is playing in any tournament today.
    const profileId = this.authenticationService.getCurrentUserProfileId();
    // const today = new Date();
    const utcMoment = moment([2022, 0, 14, 0, 0, 0]).utc();
    const today = utcMoment.toDate();
    const params = `tournamentId=0&profileId=${profileId}&date=${today}`;
    const subscription: Subscription = this.tournamentEntryService.getWithQuery(params)
      .pipe(
        first(),
        map(
          (tournamentEntries: TournamentEntry[]) => {
            // console.log('got today tournament entries', tournamentEntries);
            this.tournamentDay = (tournamentEntries.length > 0);
            if (this.tournamentDay) {
              for (const tournamentEntry of tournamentEntries) {
                if (tournamentEntry.tournamentFk === 153) {
                  this.todaysTournamentId = tournamentEntry.tournamentFk;
                  this.todaysTournamentEntryId = tournamentEntry.id;
                  // get tournament start and end date
                  // console.log('loading todays tournament information ');
                  this.loadTournamentInfo(this.todaysTournamentId, today);
                  break;
                }
              }
            }
            this.todayService.hasTournamentToday = this.tournamentDay;
          })
      ).subscribe();
    this.subscriptions.add(subscription);
  }

  private loadTournamentInfo(tournamentId: number, today: Date) {
    // create a selector for fast lookup in cache
    const tournamentInfoSelector = this.tournamentInfoService.selectors.selectEntityMap;
    const selectedTournamentSelector = createSelector(
      tournamentInfoSelector,
      (entityMap) => {
        return entityMap[tournamentId];
      });

    const subscription = this.tournamentInfoService.store.select(selectedTournamentSelector)
      .subscribe(
        (tournamentInfo: TournamentInfo) => {
          if (tournamentInfo) {
            const difference = new DateUtils().daysBetweenDates(tournamentInfo.startDate, today);
            this.todayService.tournamentDay = difference + 1;
            // console.log('setting tournamentDay to ', this.todayService.tournamentDay);
            this.todayService.todayUrl = `/today/landing/${this.todaysTournamentId}/${this.todayService.tournamentDay}/${this.todaysTournamentEntryId}`;
            // console.log('setting today url', this.todayService.todayUrl);
          } else {
            // console.log('tournamentInfo not in cache. getting from SERVER');
            // not in cache so get it. Since it is an entity collection it will be
            // piped to the above selector and processed by if branch
            this.tournamentInfoService.getByKey(tournamentId);
          }
        });
    this.subscriptions.add(subscription);
  }
}
