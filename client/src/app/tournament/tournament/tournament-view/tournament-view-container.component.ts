import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {TournamentInfo} from '../tournament-info.model';
import {ActivatedRoute} from '@angular/router';
import {TournamentService} from '../../tournament.service';

@Component({
  selector: 'app-tournament-view-container',
  template: `
    <mat-progress-bar *ngIf="loading$ | async; else elseblock" mode="indeterminate"
                      color="primary"></mat-progress-bar>
    <ng-template #elseblock>
      <mat-progress-bar mode="determinate" color="primary" value="0"></mat-progress-bar>
    </ng-template>
    <app-tournament-view [tournament]="tournament$ | async"></app-tournament-view>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentViewContainerComponent implements OnInit {

  tournament$: Observable<TournamentInfo>;
  loading$: Observable<boolean>;
  private editedId: number;

  constructor(public tournamentService: TournamentService,
              private activatedRoute: ActivatedRoute) { }

  ngOnInit(): void {
    this.editedId = this.activatedRoute.snapshot.params['id'] || 0;
    this.tournament$ = this.tournamentService.getByKey(this.editedId);
    this.tournament$.subscribe(data => {
      console.log('got tournament info data ' + JSON.stringify(data));
      return data;
    });
  }

}
