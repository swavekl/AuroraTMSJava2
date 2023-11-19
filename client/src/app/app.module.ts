import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {HTTP_INTERCEPTORS, HttpClientModule, HttpClientXsrfModule} from '@angular/common/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatButtonModule} from '@angular/material/button';
import {MatListModule} from '@angular/material/list';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';
import {MatSidenavModule} from '@angular/material/sidenav';
import {LayoutModule} from '@angular/cdk/layout';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS, MatFormFieldDefaultOptions} from '@angular/material/form-field';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';
import {environment} from '../environments/environment';
import {AppComponent} from './app.component';
import {AppRoutingModule} from './app-routing.module';
import {AuthInterceptor} from './auth.interceptor';
import {AppStoreModule} from './store/app-store.module';
import {UserModule} from './user/user.module';
import {SharedModule} from './shared/shared.module';
import {EntityDataService, EntityServices} from '@ngrx/data';
import {TournamentConfigService} from './tournament/tournament-config/tournament-config.service';
import {TournamentEventConfigService} from './tournament/tournament-config/tournament-event-config.service';
import {TournamentEventConfigDataService} from './tournament/tournament-config/tournament-event-config-data.service';
import {TournamentEventEntryDataService} from './tournament/tournament-config/tournament-event-entry-data.service';

const appearance: MatFormFieldDefaultOptions = {
  appearance: 'fill'
};

@NgModule({
  declarations: [
    AppComponent
  ],
    imports: [
        AppRoutingModule,
        BrowserModule,
        HttpClientModule,
        HttpClientXsrfModule.withOptions({
            cookieName: 'XSRF-TOKEN',
            headerName: 'X-XSRF-TOKEN',
        }),
        BrowserAnimationsModule,
        MatButtonModule,
        MatListModule,
        MatToolbarModule,
        MatSidenavModule,
        MatIconModule,
        UserModule,
        LayoutModule,
        AppStoreModule,
        environment.production ? [] : StoreDevtoolsModule.instrument(),
        SharedModule
    ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
    {provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: appearance}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  // Inject the service to ensure it registers with EntityServices
  constructor(
    entityServices: EntityServices,
    entityDataService: EntityDataService,
    // custom collection services
    tournamentConfigService: TournamentConfigService,

    tournamentEventConfigService: TournamentEventConfigService,
    tournamentEventConfigDataService: TournamentEventConfigDataService,

    tournamentEventEntryDataService: TournamentEventEntryDataService

  ) {
    // register service for contacting REST API because it doesn't follow the pattern of standard REST call
    entityDataService.registerService('TournamentEvent', tournamentEventConfigDataService);
    entityDataService.registerService('TournamentEventEntry', tournamentEventEntryDataService);

    entityServices.registerEntityCollectionServices([
      tournamentConfigService,
      tournamentEventConfigService
    ]);
  }
}
