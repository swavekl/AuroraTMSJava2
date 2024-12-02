import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {PlayerScheduleItem} from '../model/player-schedule-item.model';
import {PlayerDaySchedule} from '../model/player-day-schedule.model';
import {TournamentInfo} from '../../tournament/model/tournament-info.model';
import {Router} from '@angular/router';
import {DateUtils} from '../../shared/date-utils';
import {ScheduleItemStatus} from '../model/schedule-item-status.model';

@Component({
  selector: 'app-player-schedule',
  templateUrl: './player-schedule.component.html',
  styleUrls: ['./player-schedule.component.scss']
})
export class PlayerScheduleComponent implements OnInit, OnChanges {

  // individual player schedule items
  @Input()
  playerScheduleItems: PlayerScheduleItem[] = [];

  @Input()
  tournamentInfo: TournamentInfo;

  @Input()
  tournamentEntryId: number;

  @Input()
  tournamentDay: number;

  playerDaySchedules: PlayerDaySchedule[] = [];

  tournamentStartDate: Date;

  constructor(private router: Router) { }

  ngOnInit(): void {
  }

  /**
   *
   * @param changes
   */
  ngOnChanges(changes: SimpleChanges): void {
    const playerScheduleItemsChanges: SimpleChange = changes.playerScheduleItems;
    if (playerScheduleItemsChanges != null) {
      const playerScheduleItems = playerScheduleItemsChanges.currentValue;
      if (playerScheduleItems != null) {
        this.playerDaySchedules = this.convertToDaySchedules(playerScheduleItems);
      }
    }
    const tournamentInfoChanges: SimpleChange = changes.tournamentInfo;
    if (tournamentInfoChanges != null) {
      const tournamentInfo = tournamentInfoChanges.currentValue;
      if (tournamentInfo != null) {
        this.tournamentStartDate = new DateUtils().convertFromString(tournamentInfo.startDate);
      }
    }
  }

  /**
   *
   * @param playerScheduleItems
   * @private
   */
  private convertToDaySchedules(playerScheduleItems): PlayerDaySchedule[] {
    const playerDaySchedules: PlayerDaySchedule[] = [];
    for (const playerScheduleItem of playerScheduleItems) {
      const day = playerScheduleItem.day;
      let dayPlayerSchedule = null;
      for (const tempPlayerSchedule of playerDaySchedules) {
        if (tempPlayerSchedule.day === day) {
          dayPlayerSchedule = tempPlayerSchedule;
        }
      }
      if (dayPlayerSchedule === null) {
        dayPlayerSchedule = new PlayerDaySchedule();
        dayPlayerSchedule.day = day;
        playerDaySchedules.push(dayPlayerSchedule);
      }
      if (dayPlayerSchedule != null) {
        dayPlayerSchedule.playerScheduleItems.push(playerScheduleItem);
      }
    }
    return playerDaySchedules;
  }

  showScheduleDetail(matchCardId: number) {
    const returnUrl = window.location.pathname;
    const url = `/ui/today/playerscheduledetail/${this.tournamentInfo.id}/${this.tournamentDay}/${this.tournamentEntryId}/${matchCardId}`;
    const extras = {
      state: {
        returnUrl:  returnUrl
      }
    };
    this.router.navigateByUrl(url, extras);
  }

  back() {
    this.router.navigateByUrl('/ui/home');
  }

  getStatusColor(status: ScheduleItemStatus) {
    switch (status) {
      case ScheduleItemStatus.NotReady: return 'black';
      case ScheduleItemStatus.NotStarted: return 'red';
      case ScheduleItemStatus.Started: return 'green';
      case ScheduleItemStatus.InProgress: return 'blue';
      case ScheduleItemStatus.Completed: return 'purple';
      default: return 'black';

    }
  }

  getStatusText(status: ScheduleItemStatus) {
    switch (status) {
      case ScheduleItemStatus.NotReady: return 'Not Ready';
      case ScheduleItemStatus.NotStarted: return 'No Tables'; // 'Not Started';
      case ScheduleItemStatus.Started: return 'Started';
      case ScheduleItemStatus.InProgress: return 'In Progress';
      case ScheduleItemStatus.Completed: return 'Completed';
      default: return 'black';

    }
  }
}
