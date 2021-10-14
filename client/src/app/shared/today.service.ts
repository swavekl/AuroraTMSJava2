import { Injectable } from '@angular/core';

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
}
