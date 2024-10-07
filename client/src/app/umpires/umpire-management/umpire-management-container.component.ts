import {Component, OnDestroy, OnInit} from '@angular/core';
import {DateUtils} from '../../shared/date-utils';
import {first, map} from 'rxjs/operators';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {TodayService} from '../../shared/today.service';
import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {Personnel} from '../../tournament/tournament-config/model/personnel.model';
import {UserRoles} from '../../user/user-roles.enum';
import {UmpiringService} from '../service/umpiring.service';

@Component({
  selector: 'app-umpire-management-container',
  template: `
    <app-umpire-management [tournamentId]="tournamentId"
    [tournamentDay]="tournamentDay"
    [tournamentName]="tournamentName"
    [umpireList]="umpireList"
    ></app-umpire-management>
  `,
  styles: ``
})
export class UmpireManagementContainerComponent implements OnInit, OnDestroy {
  tournamentId: number;
  tournamentDay: number;
  tournamentName: string;
  umpireList: Personnel[] = [];

  private subscriptions: Subscription = new Subscription();
  private loading$: Observable<boolean>;


  constructor(private todayService: TodayService,
              private tournamentService: TournamentConfigService,
              private umpiringService: UmpiringService,
              private linearProgressBarService: LinearProgressBarService) {
    this.setupProgressIndicator();
    this.loadTodaysTournament();
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.tournamentService.store.select(this.tournamentService.selectors.selectLoading),
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


  private loadTodaysTournament() {
    const todaysDate: Date = this.todayService.todaysDate;
    const todaysDateUtc = new DateUtils().convertFromLocalToUTCDate(todaysDate);
    const subscription = this.tournamentService.getTodaysTournaments(todaysDateUtc)
      .pipe(
        first(),
        map((todaysTournaments: Tournament[], index: number) => {
            if (todaysTournaments?.length > 0) {
              const tournament: Tournament = todaysTournaments[0];
              this.tournamentId = tournament.id;
              this.tournamentName = tournament.name;
              const difference = new DateUtils().daysBetweenDates(tournament.startDate, todaysDate);
              this.tournamentDay = difference + 1;
              // get available umpires list
              const personnelList: Personnel[] = tournament.configuration.personnelList;
              let availableUmpires = [];
              if (personnelList?.length > 0) {
                availableUmpires = personnelList.filter((personnel: Personnel) => {
                  return personnel.role === UserRoles.ROLE_UMPIRES;
                });
              }
              this.umpireList = availableUmpires;
            }
          }
        ))
      .subscribe();
    this.subscriptions.add(subscription);
  }
}
