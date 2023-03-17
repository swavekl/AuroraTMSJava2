import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subscription} from 'rxjs';
import {Tournament} from '../tournament.model';
import {TournamentConfigService} from '../tournament-config.service';
import {Router} from '@angular/router';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';

@Component({
  selector: 'app-tournament-config-container-list',
  template: `
    <app-tournament-config-list [tournaments]="tournaments$ | async"
                                (add)="onAdd($event)"
    ></app-tournament-config-list>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentConfigListContainerComponent implements OnInit, OnDestroy {

  tournaments$: Observable<Tournament[]>;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentConfigService: TournamentConfigService,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService) {
    this.tournaments$ = this.tournamentConfigService.entities$;
    const subscription = this.tournamentConfigService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(subscription);
  }

  ngOnInit(): void {
    this.tournamentConfigService.getAll();
  }

  onAdd(event: any) {
    this.router.navigateByUrl('/ui/tournamentsconfig/tournament/create');
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
