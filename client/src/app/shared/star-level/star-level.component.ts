import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';

/**
 * Component for showing number of stars
 */
@Component({
  selector: 'app-star-level',
  template: `
      <ng-container *ngFor="let star of starsArray">
        <mat-icon matListIcon color="accent" [ngClass]="compact ? 'compact' : 'normal'">star</mat-icon>
      </ng-container>
  `,
  styles: [
  'mat-icon.compact {font-size: 16px; height: 16px; width: 16px;} ' +
  'mat-icon.normal  {font-size: 24px;}'
  ]
})
export class StarLevelComponent implements OnChanges {
  starsArray: any [];

  @Input()
  numStars: number;

  @Input()
  compact: boolean;

  constructor() {
    this.starsArray = [];
  }

  ngOnChanges(changes: SimpleChanges): void {
    const numStarsChanges = changes.numStars;
    if (numStarsChanges) {
      this.starsArray = Array(this.numStars);
    }
  }
}
