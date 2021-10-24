import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {PlayerScheduleItem} from '../model/player-schedule-item.model';
import {EventStatusCode} from '../model/event-status-code.enum';

@Component({
  selector: 'app-player-schedule-detail',
  templateUrl: './player-schedule-detail.component.html',
  styleUrls: ['./player-schedule-detail.component.css']
})
export class PlayerScheduleDetailComponent implements OnInit, OnChanges {

  @Input()
  public playerScheduleItem: PlayerScheduleItem;

  constructor() { }

  ngOnInit(): void {
  }

  /**
   *
   * @param status
   */
  getStatusIcon(status: EventStatusCode) {
    switch (status) {
      case EventStatusCode.WILL_PLAY:
        return 'check_circle';
      case EventStatusCode.WILL_NOT_PLAY:
        return 'error';
      case EventStatusCode.WILL_PLAY_BUT_IS_LATE:
        return 'warning';
      default:
        return 'help';
    }
  }

  /**
   *
   * @param status
   */
  getStatusIconClass(status: EventStatusCode) {
    switch (status) {
      case EventStatusCode.WILL_PLAY:
        return 'will-play';
      case EventStatusCode.WILL_NOT_PLAY:
        return 'will-not-play';
      case EventStatusCode.WILL_PLAY_BUT_IS_LATE:
        return 'will-play-late';
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    // const playerScheduleItemChange: SimpleChange = changes.playerScheduleItem;
    // console.log('playerScheduleItemChange', playerScheduleItemChange);
  }
}
