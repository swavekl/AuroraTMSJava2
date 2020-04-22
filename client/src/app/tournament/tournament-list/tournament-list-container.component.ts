import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Tournament} from '../tournament.model';
import {TournamentService} from '../tournament.service';


@Component({
  selector: 'app-tournament-list-container',
  template: `
    <mat-progress-bar *ngIf="loading$ | async; else elseblock" mode="indeterminate"
                      color="primary"></mat-progress-bar>
    <ng-template #elseblock>
      <mat-progress-bar mode="determinate" color="primary" value="0"></mat-progress-bar>
    </ng-template>
    <app-tournament-list [tournaments]="tournaments$ | async"></app-tournament-list>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentListContainerComponent implements OnInit, OnDestroy  {
  tournaments$: Observable<Tournament[]>;
  loading$: Observable<boolean>;

  constructor(public tournamentService: TournamentService) {
    this.tournaments$ = this.tournamentService.entities$;
    this.loading$ = this.tournamentService.loading$;
  }

  ngOnInit() {
    this.tournamentService.getAll();
  }

  ngOnDestroy(): void {
  }
}
