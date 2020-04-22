import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-linear-progress-bar',
  template: `
    <mat-progress-bar *ngIf="loading; else elseblock" mode="indeterminate" color="warn"></mat-progress-bar>
    <ng-template #elseblock>
      <mat-progress-bar mode="determinate" color="warn" value="0"></mat-progress-bar>
    </ng-template>
  `,
  styleUrls: []
})
export class LinearProgressBarComponent implements OnInit {
  @Input()
  loading: boolean;

  constructor() {
    this.loading = true;
  }

  ngOnInit(): void {
  }
}
