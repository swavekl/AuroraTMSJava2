import {Inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {download, Download} from './download';
import {SAVER, Saver} from './saver.provider';

/**
 * Service for downloading files which reports progress
 */
@Injectable({providedIn: 'root'})
export class DownloadService {

  constructor(private httpClient: HttpClient,
    @Inject(SAVER) private save: Saver) {
  }

  download(url: string, filename?: string): Observable<Download> {
    return this.httpClient.get(url, {
      reportProgress: true,
      observe: 'events',
      responseType: 'blob'
    }).pipe(download(blob => this.save(blob, filename)));
  }

  blob(url: string, filename?: string): Observable<Blob> {
    return this.httpClient.get(url, {
      responseType: 'blob'
    });
  }
}
