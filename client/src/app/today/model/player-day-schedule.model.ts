import {PlayerScheduleItem} from './player-schedule-item.model';

export class PlayerDaySchedule {
  // day 1 of tournament, day 2 etc.
  day: number;

  // list of events on that day player is signed up for
  playerScheduleItems: PlayerScheduleItem[] = [];
}
