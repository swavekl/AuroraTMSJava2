import {Injectable} from '@angular/core';
import {EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {DrawItem} from '../model/draw-item.model';
import {DrawType} from '../model/draw-type.enum';
import {Observable} from 'rxjs';
import {DrawDataService} from './draw-data.service';
import {tap} from 'rxjs/operators';

/**
 * Service for getting draws information
 */
@Injectable({
  providedIn: 'root'
})
  export class DrawService extends EntityCollectionServiceBase<DrawItem> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory,
              private drawDataService: DrawDataService) {
    super('DrawItem', serviceElementsFactory);
  }

  /**
   * Loads this event draws
   * @param eventId event id
   * @param drawType draw type
   */
  loadForEvent(eventId: number, drawType: DrawType): Observable<DrawItem[]> {
    // pass these params to data service so it can customize load
    this.drawDataService.eventId = eventId;
    this.drawDataService.drawType = drawType;
    // replace all draws with draws for this event
    return super.load();
  }

  /**
   * Custom action to generate draws
   * @param eventId event id
   * @param drawType draw type
   */
  generate(eventId: number, drawType: DrawType): Observable<DrawItem[]> {
    this.drawDataService.eventId = eventId;
    this.drawDataService.drawType = drawType;
    super.clearCache();
    this.setLoading(true);
    return this.drawDataService.generate(eventId, drawType)
      .pipe(tap(
        (draws: DrawItem[]) => {
          // console.log('adding generated draws to cache of length ' + draws.length);
          super.addAllToCache(draws);
          this.setLoading(false);
        },
        () => {
          this.setLoading(false);
          console.log ('error when generating draws');
        }
      ));
  }

  /**
   * Deletes the draw
   * @param eventId event id
   */
  deleteForEvent(eventId: number): Observable<number> {
    super.clearCache();
    return this.drawDataService.delete(eventId);
  }

  /**
   * Updates a single draw item
   * @param drawItems
   */
  updateDrawItems(drawItems: DrawItem[]): Observable<DrawItem> {
    return this.drawDataService.updateItems(drawItems);
  }
}
