import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {TournamentInfoService} from '../tournament-info.service';
import {Observable} from 'rxjs';
import {TournamentInfo} from '../tournament-info.model';

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
export class TournamentListContainerComponent implements OnInit {

  tournaments$: Observable<TournamentInfo[]>;
  loading$: Observable<boolean>;

  constructor(private tournamentInfoService: TournamentInfoService) {
    this.tournaments$ = this.tournamentInfoService.entities$;
    this.loading$ = this.tournamentInfoService.loading$;
  }

  ngOnInit(): void {
    this.tournamentInfoService.getAll();
  }

}
