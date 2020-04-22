import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {TournamentConfigService} from '../tournament-config.service';
import {Observable} from 'rxjs';
import {Tournament} from '../tournament.model';

@Component({
  selector: 'app-tournament-config-edit-container',
  template: `
    <mat-progress-bar *ngIf="loading$ | async; else elseblock" mode="indeterminate"
                      color="primary"></mat-progress-bar>
    <ng-template #elseblock>
      <mat-progress-bar mode="determinate" color="primary" value="0"></mat-progress-bar>
    </ng-template>
    <app-tournament-config-edit [tournament]="tournament$ | async"></app-tournament-config-edit>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentConfigEditContainerComponent implements OnInit {

  tournament$: Observable<Tournament>;
  loading$: Observable<boolean>;
  private editedId: number;

  constructor(public tournamentConfigService: TournamentConfigService,
              private activatedRoute: ActivatedRoute) { }

  ngOnInit(): void {
    this.editedId = this.activatedRoute.snapshot.params['id'] || 0;
    this.tournament$ = this.tournamentConfigService.getByKey(this.editedId);
    this.tournament$.subscribe(data => {
      console.log('got tournament data ' + JSON.stringify(data));
      return data;
    });
  }

}
