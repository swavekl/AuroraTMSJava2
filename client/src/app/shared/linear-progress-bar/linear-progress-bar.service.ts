import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';

/**
 * Service for indicating progress on the global progress bar
 */
@Injectable({
  providedIn: 'root'
})
export class LinearProgressBarService {

  private loading$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor() { }

  /**
   * observable for progress indicator
   */
  public getLoadingObservable(): Observable<boolean> {
    return this.loading$.asObservable();
  }

  public setLoading (loading: boolean) {
    this.loading$.next(loading);
  }

  /**
   * clients who want to indicate progress call start
   */
  public startProgress() {
    this.loading$.next(true);
  }

  /**
   * clients who want to stop indicating progress call end
   */
  public endProgress() {
    this.loading$.next(false);
  }
}
