import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Tournament} from '../tournament.model';
import {TournamentConfigService} from '../tournament-config.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-tournament-config-container-list',
  template: `
    <app-linear-progress-bar [loading]="loading$ | async"></app-linear-progress-bar>
    <app-tournament-config-list [tournaments]="tournaments$ | async"
                                (add)="onAdd($event)"
    ></app-tournament-config-list>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentConfigListContainerComponent implements OnInit {

  tournaments$: Observable<Tournament[]>;
  loading$: Observable<boolean>;

  constructor(private tournamentConfigService: TournamentConfigService,
              private router: Router) {
    this.tournaments$ = this.tournamentConfigService.entities$;
    this.loading$ = this.tournamentConfigService.loading$;
  }

  ngOnInit(): void {
    this.tournamentConfigService.getAll();
  }

  onAdd(event: any) {
    this.router.navigateByUrl('tournament/create');
  }
}
