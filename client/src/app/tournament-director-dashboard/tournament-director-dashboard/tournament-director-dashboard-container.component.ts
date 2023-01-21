import {Component, OnDestroy, OnInit} from '@angular/core';
import {TournamentEntryInfoService} from '../../tournament/service/tournament-entry-info.service';
import {Observable, of, Subscription} from 'rxjs';
import {TournamentEntryInfo} from '../../tournament/model/tournament-entry-info.model';
import {ActivatedRoute} from '@angular/router';
import {createSelector} from '@ngrx/store';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';

@Component({
  selector: 'app-tournament-director-dashboard-container',
  template: `
    <app-tournament-director-dashboard
      [tournamentId]="tournamentId$ | async"
      [tournamentName]="tournamentName$ | async">
    </app-tournament-director-dashboard>
  `,
  styles: [
  ]
})
export class TournamentDirectorDashboardContainerComponent implements OnInit, OnDestroy {

  public tournamentId$: Observable<number>;
  public tournamentName$: Observable<string>;

  private subscriptions: Subscription = new Subscription();

  constructor(private activatedRoute: ActivatedRoute,
              private tournamentConfigService: TournamentConfigService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    const tournamentId = Number(strTournamentId);
    this.loadTournamentName(tournamentId);
  }

  private loadTournamentName(tournamentId: number) {
    const selectedTournamentSelector = createSelector(
      this.tournamentConfigService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[tournamentId];
      });
    const localTournament$ = this.tournamentConfigService.store.select(selectedTournamentSelector);
    const subscription = localTournament$
      .subscribe((tournament: Tournament) => {
        if (!tournament) {
          this.tournamentConfigService.getByKey(tournamentId);
        } else {
          this.tournamentId$ = of(tournament.id);
          this.tournamentName$ = of(tournament.name);
        }
      });
    this.subscriptions.add(subscription);
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

}
