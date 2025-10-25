import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {distinctUntilChanged, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {ImportProgressInfo} from '../model/import-progress-info.model';
import {ImportEntriesRequest} from '../model/import-entries-request.model';
import {ImportTournamentRequest} from '../model/import-tournament-request.model';

/**
 * Service for importing tournament entries from Omnipong
 */
@Injectable({
  providedIn: 'root'
})
export class TournamentImportService {

  private loadingSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean> = this.loadingSubject$.asObservable().pipe(distinctUntilChanged());

  constructor(private httpClient: HttpClient) {
  }

  private setLoading(loading: boolean) {
    this.loadingSubject$.next(loading);
  }

  /**
   * lists tournaments on Activities page
   */
  listTournaments(): Observable<ImportTournamentRequest []> {
    this.setLoading(true);
    const url = `/api/importtournament/list`;
    return this.httpClient.get<ImportTournamentRequest []>(url)
      .pipe(
        tap(
          () => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }
        )
      );
  }

  /**
   * Imports tournament entries from Omnipong into selected tournament
   * @param importEntriesRequest import request
   */
  importTournamentEntries(importEntriesRequest: ImportEntriesRequest): Observable<ImportProgressInfo> {
    this.setLoading(true);
    const url = `/api/importtournament/entries`;
    return this.httpClient.post<ImportProgressInfo>(url, importEntriesRequest)
      .pipe(
        tap(
          () => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }
        )
      );
  }

  /**
   * Imports tournament entries from Omnipong into selected tournament
   */
  importTournamentConfiguration(importTournamentRequest: ImportTournamentRequest): Observable<ImportProgressInfo> {
    this.setLoading(true);
    const url = `/api/importtournament/configuration`;
    return this.httpClient.post<ImportProgressInfo>(url, importTournamentRequest)
      .pipe(
        tap(
          () => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }
        )
      );
  }

  getImportStatus(jobId: string): Observable<ImportProgressInfo> {
    console.log('getting status for jobid', jobId);
    const url = `/api/importtournament/status/${jobId}`;
    return this.httpClient.get<ImportProgressInfo>(url);
  }
}
