import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';

/**
 * Component for showing number of stars
 */
@Component({
  selector: 'app-star-level',
  template: `
      <ng-container *ngFor="let star of starsArray">
        <mat-icon matListItemIcon color="accent" [ngClass]="compact ? 'compact' : 'normal'">star</mat-icon>
      </ng-container>
  `,
  styles: [
  'mat-icon.compact {font-size: 16px !important; height: 16px !important; width: 16px !important; padding: 0 !important;} ' +
  'mat-icon.normal  {font-size: 24px; padding: 4px 0 0 0}'
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
    this.compact = true;
  }

  ngOnChanges(changes: SimpleChanges): void {
    const numStarsChanges = changes.numStars;
    if (numStarsChanges) {
      this.starsArray = Array(this.numStars);
    }
  }
}
