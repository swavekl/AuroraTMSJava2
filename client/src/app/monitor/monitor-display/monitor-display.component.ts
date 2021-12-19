import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {MonitorMessage} from '../model/monitor-message.model';

@Component({
  selector: 'app-monitor-display',
  templateUrl: './monitor-display.component.html',
  styleUrls: ['./monitor-display.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MonitorDisplayComponent implements OnInit, OnChanges {

  @Input()
  matchData: MonitorMessage;

  // match: Match;

  // array so we can use iteration in the template
  games: number[];

  constructor(protected cdr: ChangeDetectorRef) {
    this.games = Array(7);
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const matchDataChange: SimpleChange = changes.matchData;
    if (matchDataChange != null) {
      const mm: MonitorMessage = matchDataChange.currentValue;
      console.log('monitor display got changes', mm);
      if (mm != null) {
        this.games = Array(mm.numberOfGames);
      }
      this.cdr.markForCheck();
      this.cdr.detectChanges();
    }
  }
}
