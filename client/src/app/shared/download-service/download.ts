import {HttpEvent, HttpEventType, HttpProgressEvent, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {distinctUntilChanged, scan} from 'rxjs/operators';

/**
 * Helper function for determining if the response is HTTP response
 * @param event event to check
 * */
function isHttpResponse<T>(event: HttpEvent<T>): event is HttpResponse<T> {
  return event.type === HttpEventType.Response;
}

/**
 * Helper function for determining if this event is an event reporting download progress
 * @param event event to check
 */
function isHttpProgressEvent(event: HttpEvent<unknown>): event is HttpProgressEvent {
  return (
    event.type === HttpEventType.DownloadProgress ||
    event.type === HttpEventType.UploadProgress
  );
}

/**
 * Interface for reporting progress and returning downloaded file
 */
export interface Download {
  // downloaded file
  content: Blob | null;
  // percentage of progress
  progress: number;
  // state of download
  state: 'PENDING' | 'IN_PROGRESS' | 'DONE';
}

/**
 * Function to call to download
 * @param saver saver function for saving downloaded file
 */
export function download(saver?: (blob: Blob) => void): (source: Observable<HttpEvent<Blob>>) => Observable<Download> {
  return (source: Observable<HttpEvent<Blob>>) =>
    source.pipe(
      scan(
        (theDownload: Download, event): Download => {
          // this is progress event - convert and report
          if (isHttpProgressEvent(event)) {
            return {
              progress: event.total
                ? Math.round((100 * event.loaded) / event.total)
                : theDownload.progress,
              state: 'IN_PROGRESS',
              content: null
            };
          }
          // this is http event - we are done
          if (isHttpResponse(event)) {
            // save the content
            if (saver) {
              saver(event.body);
            }
            return {
              progress: 100,
              state: 'DONE',
              content: event.body
            };
          }
          return theDownload;
        },
        { state: 'PENDING', progress: 0, content: null }
      ),
      distinctUntilChanged((a, b) => a.state === b.state
        && a.progress === b.progress
        && a.content === b.content
      )
    );
}
