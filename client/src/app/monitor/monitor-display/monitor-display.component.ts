import {ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {MonitorMessage} from '../model/monitor-message.model';

@Component({
  selector: 'app-monitor-display',
  templateUrl: './monitor-display.component.html',
  styleUrls: ['./monitor-display.component.scss']
})
export class MonitorDisplayComponent implements OnInit, OnChanges {

  @Input()
  matchData: MonitorMessage;

  @Input()
  isConnected: boolean;

  // table number this screen is connected to
  @Input()
  tableNumber: number;

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
      if (mm != null) {
        // console.log('monitor display got changes', mm.numberOfGames);
        this.games = Array(mm.numberOfGames);
      }
    }
  }
}
