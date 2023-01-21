import { Injectable } from '@angular/core';
import * as moment from 'moment';

/**
 * Service for sharing visibility and url of Today page between home page and application's main menu
 */
@Injectable({
  providedIn: 'root'
})
export class TodayService {

  // true if current user plays in a tournament today
  private _hasTournamentToday: boolean;

  // the url of the Today landing page
  private _todayUrl: string;

  // day of the tournament e.g. 1, 2 etc.
  private _tournamentDay: number;

  constructor() {
    this._hasTournamentToday = false;
  }

  get hasTournamentToday(): boolean {
    return this._hasTournamentToday;
  }

  set hasTournamentToday(value: boolean) {
    this._hasTournamentToday = value;
  }


  get todayUrl(): string {
    return this._todayUrl;
  }

  set todayUrl(value: string) {
    this._todayUrl = value;
  }

  get tournamentDay(): number {
    return this._tournamentDay;
  }

  set tournamentDay(value: number) {
    this._tournamentDay = value;
  }

  get todaysDate(): Date {
    // todo - remove for production
    // Phoenix Winter Open
    // const utcMoment = moment([2023, 1, 25, 0, 0, 0]).utc();
    // Aurora Cup
    // const utcMoment = moment([2023, 0, 15, 0, 0, 0]).utc();
    // Aurora Fall Open
    // const utcMoment = moment([2025, 4, 10, 0, 0, 0]).utc();
    // return utcMoment.toDate();
    return new Date();
  }
}
