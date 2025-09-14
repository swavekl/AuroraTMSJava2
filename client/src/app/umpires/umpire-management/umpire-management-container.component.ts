import {Component, OnDestroy, OnInit} from '@angular/core';
import {DateUtils} from '../../shared/date-utils';
import {first} from 'rxjs/operators';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {TodayService} from '../../shared/today.service';
import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {Personnel} from '../../tournament/tournament-config/model/personnel.model';
import {UserRoles} from '../../user/user-roles.enum';
import {UmpiringService} from '../service/umpiring.service';
import {ActivatedRoute} from '@angular/router';
import {createSelector} from '@ngrx/store';

@Component({
    selector: 'app-umpire-management-container',
    template: `
    <app-umpire-management [tournamentId]="tournamentId"
                           [tournamentDay]="tournamentDay"
                           [tournamentName]="tournamentName"
                           [umpireList]="umpireList"
                           [refereeList]="refereeList"
    ></app-umpire-management>
  `,
    styles: ``,
    standalone: false
})
export class UmpireManagementContainerComponent implements OnInit, OnDestroy {
  tournamentId: number;
  tournamentDay: number;
  tournamentName: string;
  umpireList: Personnel[] = [];
  refereeList: Personnel[] = [];

  private subscriptions: Subscription = new Subscription();
  private loading$: Observable<boolean>;


  constructor(private todayService: TodayService,
              private tournamentConfigService: TournamentConfigService,
              private umpiringService: UmpiringService,
              private linearProgressBarService: LinearProgressBarService,
              private activatedRoute: ActivatedRoute) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    this.tournamentName = null;
    this.tournamentDay = 1;
    this.setupProgressIndicator();
    this.loadTournament(this.tournamentId);
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.tournamentConfigService.store.select(this.tournamentConfigService.selectors.selectLoading),
      this.umpiringService.loading$,
      (tournamentsLoading: boolean, umpireInformationLoading: boolean) => {
        return tournamentsLoading || umpireInformationLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadTournament(tournamentId: number) {
    const todaysDate: Date = this.todayService.todaysDate;
    const selectedTournamentSelector = createSelector(
      this.tournamentConfigService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[tournamentId];
      });
    const localTournament$ = this.tournamentConfigService.store.select(selectedTournamentSelector);
    const subscription = localTournament$
      .pipe(first())
      .subscribe({
        next: (tournament: Tournament) => {
          if (!tournament) {
            this.tournamentConfigService.getByKey(tournamentId);
          } else {
            this.tournamentName = tournament.name;
            const difference = new DateUtils().daysBetweenDates(tournament.startDate, todaysDate);
            this.tournamentDay = difference + 1;
            // get available umpires list
            const personnelList: Personnel[] = tournament.configuration.personnelList;
            let availableUmpires = [];
            let refereeList: Personnel [] = [];
            if (personnelList?.length > 0) {
              availableUmpires = personnelList.filter((personnel: Personnel) => {
                return personnel.role === UserRoles.ROLE_UMPIRES;
              });
              refereeList = personnelList.filter((personnel: Personnel) => {
                return personnel.role === UserRoles.ROLE_REFEREES;
              });
            }
            this.umpireList = availableUmpires;
            this.refereeList = refereeList;
          }
        },
        error: (error) => {
          console.error('error getting tournament', error);
        }
      });
    this.subscriptions.add(subscription);
  }
}
