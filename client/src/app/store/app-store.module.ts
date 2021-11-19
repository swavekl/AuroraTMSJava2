import {NgModule} from '@angular/core';
import {EffectsModule} from '@ngrx/effects';
import {StoreModule} from '@ngrx/store';
import {DefaultDataServiceConfig, EntityCollectionReducerMethodsFactory, EntityDataModule, PersistenceResultHandler} from '@ngrx/data';
import {entityConfig} from './entity-metadata';
import {clearState} from './reducers';
import {PagedEntityCollectionReducerMethodsFactory} from './paged-entity-collection-reducer-methods-factory';
import {PagedPersistenceResultHandler} from './paged-persistence-result-handler';

const defaultDataServiceConfig: DefaultDataServiceConfig = {
  root: 'api',
  timeout: 3000, // request timeout
};

const reducers: any = {};

@NgModule({
  imports: [
    StoreModule.forRoot(reducers, {metaReducers: [clearState]}),
    EffectsModule.forRoot([]),
    EntityDataModule.forRoot(entityConfig)
  ],
  providers: [
    {provide: DefaultDataServiceConfig, useValue: defaultDataServiceConfig},
    {provide: PersistenceResultHandler, useClass: PagedPersistenceResultHandler},
    {provide: EntityCollectionReducerMethodsFactory, useClass: PagedEntityCollectionReducerMethodsFactory}
  ]
})
export class AppStoreModule {
}
