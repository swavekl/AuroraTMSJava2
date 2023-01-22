import {Component, Input, OnInit} from '@angular/core';
import {LinearProgressBarService} from './linear-progress-bar.service';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-linear-progress-bar',
  template: `
    <mat-progress-bar *ngIf="loading$ | async; else elseblock" mode="indeterminate" color="accent" style="height: 4px;"></mat-progress-bar>
    <ng-template #elseblock>
      <div style="height: 4px; width: 100%; background-color: white; color: white"></div>
    </ng-template>
  `,
  styleUrls: []
})
export class LinearProgressBarComponent implements OnInit {
  loading$: Observable<boolean>;

  constructor(private linearProgressBarService: LinearProgressBarService) {
    this.loading$ = this.linearProgressBarService.getLoadingObservable();
  }

  ngOnInit(): void {
  }
}
