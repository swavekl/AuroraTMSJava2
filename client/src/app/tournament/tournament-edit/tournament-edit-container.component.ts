import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Tournament} from '../tournament.model';
import {TournamentService} from '../tournament.service';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-tournament-edit-container',
  template: `
    <mat-progress-bar *ngIf="loading$ | async; else elseblock" mode="indeterminate"
                      color="primary"></mat-progress-bar>
    <ng-template #elseblock>
      <mat-progress-bar mode="determinate" color="primary" value="0"></mat-progress-bar>
    </ng-template>
    <app-tournament-edit [tournament]="tournament$ | async"></app-tournament-edit>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentEditContainerComponent implements OnInit {

  tournament$: Observable<Tournament>;
  loading$: Observable<boolean>;
  private editedId: number;

  constructor(public tournamentService: TournamentService,
              private activatedRoute: ActivatedRoute) {
  }

  ngOnInit() {
    this.editedId = this.activatedRoute.snapshot.params['id'] || 0;
    this.tournament$ = this.tournamentService.getByKey(this.editedId);
    this.tournament$.subscribe(data => {
      console.log('got tournament data ' + JSON.stringify(data));
      return data;
    });
  }
}
