import {EntityAction, EntityCollection, EntityCollectionReducerMethods, EntityDefinition} from '@ngrx/data';

/**
 * Reducer method for adding 'total' to the state of the collection
 */
export class PagedEntityCollectionReducerMethods<T> extends EntityCollectionReducerMethods<T> {
  constructor(public entityName: string, public definition: EntityDefinition<T>) {
    super(entityName, definition);
  }
  protected queryManySuccess(
    collection: EntityCollection<T>,
    action: EntityAction<T[]>
  ): EntityCollection<T> {
    const ec = super.queryManySuccess(collection, action);
    if ((action.payload as any).total) {
      // save the foo property from action.payload to entityCollection instance
      (ec as any).total = (action.payload as any).total;
    }
    return ec;
  }
}

