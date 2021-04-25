import {Injectable} from '@angular/core';
import {EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {Draw} from '../model/draw.model';
import {DrawType} from '../model/draw-type.enum';
import {Observable} from 'rxjs';
import {DrawDataService} from './draw-data.service';

/**
 * Service for getting draws information
 */
@Injectable({
  providedIn: 'root'
})
  export class DrawService extends EntityCollectionServiceBase<Draw> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory,
              private drawDataService: DrawDataService) {
    super('Draw', serviceElementsFactory);
  }

  generate(eventId: number, drawType: DrawType): Observable<Draw[]> {
    super.clearCache();
    return this.drawDataService.generate(eventId, drawType);
  }

  deleteForEvent(eventId: number): Observable<number> {
    super.clearCache();
    return this.drawDataService.deleteForEvent(eventId);
  }
}
