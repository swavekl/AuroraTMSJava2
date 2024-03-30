import { Component, Input } from '@angular/core';
import {PlayerStatus} from '../model/player-status.model';
import {EventStatusCode} from '../model/event-status-code.enum';

/**
 * Indicates status of player
 */
@Component({
  selector: 'app-player-status-indicator',
  template: `
    <mat-icon [className]="getStatusIconClass()">
      {{getStatusIcon()}}
    </mat-icon>
  `,
  styles: [
    `
      mat-icon {
        font-size: 16px;
      }

      mat-icon.will-play {
        color: green;
      }

      mat-icon.will-not-play {
        color: red;
      }

      mat-icon.will-play-late {
        color: orange;
      }
    `
  ]
})
export class PlayerStatusIndicatorComponent {

  @Input()
  playerStatus: EventStatusCode;

  getStatusIcon() {
    switch (this.playerStatus) {
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
   */
  getStatusIconClass() {
    switch (this.playerStatus) {
      case EventStatusCode.WILL_PLAY:
        return 'will-play';
      case EventStatusCode.WILL_NOT_PLAY:
        return 'will-not-play';
      case EventStatusCode.WILL_PLAY_BUT_IS_LATE:
        return 'will-play-late';
    }
  }
}
