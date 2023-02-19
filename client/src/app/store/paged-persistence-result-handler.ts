import {DefaultPersistenceResultHandler, EntityAction} from '@ngrx/data';
import {Action} from '@ngrx/store';
import {Injectable} from '@angular/core';

/**
 * Handler called when data arrives from the data service
 * Extracts data for paged or non-paged entity collection
 * Paged entity collection has entities in 'content' property
 * and the total count in 'total' property.  Content needs to be
 * passed for action handler as 2nd param.
 */
@Injectable({
  providedIn: 'root'
})
export class PagedPersistenceResultHandler extends DefaultPersistenceResultHandler {
  handleSuccess(originalAction: EntityAction): (data: any) => Action {
    const actionHandler = super.handleSuccess(originalAction);
    // return a factory to get a data handler to
    // parse data from DataService and save to action.payload
    return function(data: any) {
      // console.log ('in Result Handler', data);
      let collectionData = data;
      if (data && data.content) {
        collectionData = data.content;
      }
      const action = actionHandler.call(this, collectionData);
      if (action && data && data.totalElements) {
        // console.log ('totalElements', data.totalElements);
        // save the data.foo to action.payload.foo
        (action as any).payload.total = data.totalElements;
      }
      return action;
    };
  }
}


