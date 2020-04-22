import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Tournament} from '../tournament.model';
import {TournamentConfigService} from '../tournament-config.service';

@Component({
  selector: 'app-tournament-config-container-list',
  template: `
    <mat-progress-bar *ngIf="loading$ | async; else elseblock" mode="indeterminate"
                      color="primary"></mat-progress-bar>
    <ng-template #elseblock>
      <mat-progress-bar mode="determinate" color="primary" value="0"></mat-progress-bar>
    </ng-template>
    <app-tournament-config-list [tournaments]="tournaments$ | async"></app-tournament-config-list>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentConfigListContainerComponent implements OnInit {

  tournaments$: Observable<Tournament[]>;
  loading$: Observable<boolean>;

  constructor(private tournamentConfigService: TournamentConfigService) {
    this.tournaments$ = this.tournamentConfigService.entities$;
    this.loading$ = this.tournamentConfigService.loading$;
  }

  ngOnInit(): void {
    this.tournamentConfigService.getAll();
  }

}
