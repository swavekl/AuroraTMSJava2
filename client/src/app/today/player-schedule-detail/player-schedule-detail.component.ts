import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {PlayerScheduleItem} from '../model/player-schedule-item.model';
import {EventStatusCode} from '../model/event-status-code.enum';
import {Router} from '@angular/router';
import {CheckInType} from '../../tournament/model/check-in-type.enum';
import {MatSnackBar} from '@angular/material/snack-bar';
import {PlayerDetail} from '../model/player-detail.model';

@Component({
  selector: 'app-player-schedule-detail',
  templateUrl: './player-schedule-detail.component.html',
  styleUrls: ['./player-schedule-detail.component.scss']
})
export class PlayerScheduleDetailComponent implements OnInit, OnChanges {

  @Input()
  public playerScheduleItem: PlayerScheduleItem;

  @Input()
  public returnUrl: string;

  @Input()
  public tournamentId: number;

  @Input()
  public tournamentEntryId: number;

  @Input()
  public tournamentDay: number;

  @Input()
  public checkInType: CheckInType;

  constructor(private router: Router,
              private snackBar: MatSnackBar) { }

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

  getPlayerRowStyle(doubles: boolean, index: number, last: boolean): string {
    if (!last) {
      return (doubles && index % 2 !== 0) ? 'player-row-not-separated' : 'player-row-separated';
    } else {
      return (doubles) ? 'player-row-last' : 'player-row-separated  player-row-last';
    }
  }

  goBack() {
    this.router.navigateByUrl(this.returnUrl);
  }

  isPerEventCheckIn() {
    return (this.checkInType === CheckInType.PEREVENT);
  }

  checkInForEvent() {
    const url = `/ui/today/checkincommunicate/${this.tournamentId}/${this.playerScheduleItem.day}/${this.playerScheduleItem.eventId}`;
    const extras = {
      state: {
        eventName: this.playerScheduleItem.eventName
      }
    };
    this.router.navigateByUrl(url, extras);
  }

  showStatusDetail(playerDetail: PlayerDetail) {
    let statusText = "";
    switch (playerDetail.statusCode) {
      case EventStatusCode.WILL_PLAY:
        statusText = "Will play";
        break;
      case EventStatusCode.WILL_NOT_PLAY:
        statusText = "Will not play: " + playerDetail.reason;
        break;
      case EventStatusCode.WILL_PLAY_BUT_IS_LATE:
        statusText = "Will play but will arrive at " + playerDetail.estimatedArrivalTime;
        break;
    }

    if (statusText !== "") {
      this.snackBar.open(statusText, "Close", {
        duration: 3000
      });
    }
  }

  public goToMatches(playerScheduleItem) {
    const url = `/ui/matches/playermatches/${this.tournamentId}/${this.tournamentDay}/${this.tournamentEntryId}/${playerScheduleItem.matchCardId}`;
    const extras = {
      state: {
        doubles: playerScheduleItem.doubles
      }
    };
    this.router.navigateByUrl(url, extras);
  }
}
