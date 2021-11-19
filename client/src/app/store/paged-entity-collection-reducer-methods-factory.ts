import {Injectable} from '@angular/core';
import {EntityCollectionReducerMethodMap, EntityCollectionReducerMethods, EntityDefinitionService} from '@ngrx/data';
import {PagedEntityCollectionReducerMethods} from './paged-entity-collection-reducer-methods';

/**
 * Factory for creating either paged or non-paged (default) reducer methods
 */
@Injectable()
export class PagedEntityCollectionReducerMethodsFactory {
  constructor(private entityDefinitionService: EntityDefinitionService) {}
  /** Create the  {EntityCollectionReducerMethods} for the named entity type */
  create<T>(entityName: string): EntityCollectionReducerMethodMap<T> {
    const definition = this.entityDefinitionService.getDefinition<T>(entityName);
    const isPaged = (definition.metadata.additionalCollectionState !== undefined);
    const methodsClass = (isPaged)
      ? new PagedEntityCollectionReducerMethods(entityName, definition)
      : new EntityCollectionReducerMethods(entityName, definition);
      return methodsClass.methods;
  }
}
