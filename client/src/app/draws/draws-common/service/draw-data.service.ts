import {Injectable} from '@angular/core';
import {DefaultDataService, DefaultDataServiceConfig, HttpUrlGenerator} from '@ngrx/data';
import {HttpClient} from '@angular/common/http';
import {DrawItem} from '../model/draw-item.model';
import {Observable} from 'rxjs';
import {DrawType} from '../model/draw-type.enum';

@Injectable({
  providedIn: 'root'
})
export class DrawDataService extends DefaultDataService<DrawItem> {

  // parameters for making calls to server
  private _eventId: number;
  private _drawType: DrawType;
  private _loadStatus: boolean;

  constructor(http: HttpClient,
              httpUrlGenerator: HttpUrlGenerator,
              config?: DefaultDataServiceConfig) {
    super('DrawItem', http, httpUrlGenerator, config);
    this._loadStatus = false;
  }

  public set eventId(value: number) {
    this._eventId = value;
  }

  public set drawType(value: DrawType) {
    this._drawType = value;
  }

  public set loadStatus(value: boolean) {
    this._loadStatus = value;
  }

  /**
   * Gets all draws but only for one event (different from base class action)
   */
  getAll(): Observable<DrawItem[]> {
    const queryParams = `eventId=${this._eventId}&loadStatus=${this._loadStatus}&drawType=${this._drawType}`;
    const url = `/api/draws?${queryParams}`;
    return this.execute('GET', url);
  }

  /**
   * Generate all draws
   * @param eventId event id
   * @param drawType type of draw RR or SE
   */
  generate(eventId: number, drawType: DrawType): Observable<DrawItem[]> {
    const queryParams = `eventId=${eventId}&drawType=${drawType.valueOf()}`;
    const url = `/api/draws?${queryParams}`;
    return this.execute('POST', url);
  }

  /**
   * Deletes draws for this event id (different meaning than base class)
   * @param drawItems
   */
  updateItems(drawItems: DrawItem[]): Observable<DrawItem> {
    const url = `/api/draws`;
    return this.execute('PUT', url, drawItems);
  }

  /**
   * Deletes draws for this event id (different meaning than base class)
   * @param eventId
   */
  delete(eventId: number): Observable<number> {
    const url = `/api/draws?eventId=${eventId}`;
    return this.execute('DELETE', url);
  }

  replacePlayerInDraw(drawItem: DrawItem, playerToAddEntryId: number): Observable<DrawItem> {
    const queryParams = `playerToAddEntryId=${playerToAddEntryId}`;
    const url = `/api/draws/replace?${queryParams}`;
    return this.execute('PUT', url, drawItem);
  }

}
