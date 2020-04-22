import {NgModule} from '@angular/core';
import {EffectsModule} from '@ngrx/effects';
import {StoreModule} from '@ngrx/store';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';
import {environment} from '../../environments/environment';
import {DefaultDataServiceConfig, NgrxDataModule} from 'ngrx-data';
import {entityConfig} from './entity-metadata';
import {clearState} from './reducers';
import {ResetStore} from './reset-store';

const defaultDataServiceConfig: DefaultDataServiceConfig = {
  root: 'api',
  timeout: 3000, // request timeout
};

const reducers: any = {};

@NgModule({
  imports: [
    StoreModule.forRoot(reducers, {metaReducers: [clearState]}),
    EffectsModule.forRoot([]),
    NgrxDataModule.forRoot(entityConfig),
    environment.production ? [] : StoreDevtoolsModule.instrument()
  ],
  providers: [
    {provide: DefaultDataServiceConfig, useValue: defaultDataServiceConfig}
  ]
})
export class AppStoreModule {
}
