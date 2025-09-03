import { Injectable } from '@angular/core';
import moment from 'moment';

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
    // const utcMoment = moment([2022, 11, 20, 0, 0, 0]).utc();
    // Aurora Cup Real - RR & Single Elimination
    // const utcMoment = moment([2023, 0, 16, 0, 0, 0]);  // results
    // const utcMoment = moment([2023, 0, 15, 0, 0, 0]);
    // const utcMoment = moment([2023, 0, 14, 0, 0, 0]);
    // const utcMoment = moment([2023, 0, 13, 0, 0, 0]);
    // const utcMoment = moment([2023, 0, 12, 0, 0, 0]);  // day before
    // 2025 Aurora Cup Real - RR & Single Elimination May 23-25, 2025
    // const utcMoment = moment([2025, 4, 26, 0, 0, 0]);  // results
    // const utcMoment = moment([2025, 4, 25, 0, 0, 0]);
    // const utcMoment = moment([2025, 4, 24, 0, 0, 0]);
    // const utcMoment = moment([2025, 4, 23, 0, 0, 0]);
    // const utcMoment = moment([2025, 4, 22, 0, 0, 0]);  // day before
    // Prisco Mini - Single Elimination
    // const utcMoment = moment([2023, 8, 30, 5, 0, 0]);
    // Aurora Fall Open - Giant RR
    // const utcMoment = moment([2023, 10, 25, 0, 0, 0]);
    // 2023 Aurora Spring Open - Giant RR
    // const utcMoment = moment([2023, 10, 25, 0, 0, 0]);
    // 2025 Illinois State Championships
    // const utcMoment = moment([2024, 11, 1, 0, 0, 0]);
    // 2024 Regional Championships
    // const utcMoment = moment([2024, 9, 1, 0, 0, 0]);
    // 2025 Aurora Fall OPen RR - 9/7/2025
    // const utcMoment = moment([2025, 8, 7, 0, 0, 0]);
    // return utcMoment.toDate();
    return new Date();
  }
}
