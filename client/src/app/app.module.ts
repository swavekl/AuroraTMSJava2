import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {HTTP_INTERCEPTORS, HttpClientModule, HttpClientXsrfModule} from '@angular/common/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from '@angular/material/list';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';
import {MatSidenavModule} from '@angular/material/sidenav';
import {FormsModule} from '@angular/forms';
import {FlexLayoutModule} from '@angular/flex-layout';
import {LayoutModule} from '@angular/cdk/layout';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatTableModule} from '@angular/material/table';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS, MatFormFieldDefaultOptions} from '@angular/material/form-field';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';
import {MatSortModule} from '@angular/material/sort';

import {environment} from '../environments/environment';
import {AppComponent} from './app.component';
import {AppRoutingModule} from './app-routing.module';
import {AuthInterceptor} from './auth.interceptor';
import {AppStoreModule} from './store/app-store.module';
import {HomeModule} from './home/home.module';
import {TournamentModule} from './tournament/tournament/tournament.module';
import {TournamentConfigModule} from './tournament/tournament-config/tournament-config.module';
import {ProfileModule} from './profile/profile.module';
import {UserModule} from './user/user.module';
import {SharedModule} from './shared/shared.module';
import {AccountModule} from './account/account.module';
import {DrawsModule} from './draws/draws.module';
import {DoublesTeamsContainerComponent} from './tournament/tournament-entry/doubles-teams/doubles-teams-container.component';
import {DoublesTeamsComponent} from './tournament/tournament-entry/doubles-teams/doubles-teams.component';
import {MonitorModule} from './monitor/monitor.module';
import { PrizesModule } from './prizes/prizes.module';

const appearance: MatFormFieldDefaultOptions = {
  appearance: 'fill'
  // appearance: 'outline'
};

@NgModule({
  declarations: [
    AppComponent,
    DoublesTeamsContainerComponent,
    DoublesTeamsComponent
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
        MatCardModule,
        MatInputModule,
        MatListModule,
        MatToolbarModule,
        MatSidenavModule,
        MatIconModule,
        HomeModule,
        TournamentModule,
        TournamentConfigModule,
        ProfileModule,
        UserModule,
        LayoutModule,
        AppStoreModule,
        environment.production ? [] : StoreDevtoolsModule.instrument(),
        MatProgressBarModule,
        SharedModule,
        AccountModule,
        DrawsModule,
        FormsModule,
        FlexLayoutModule,
        MatTableModule,
        MatTooltipModule,
        MatSortModule,
        MonitorModule,
        PrizesModule
    ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
    {provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: appearance}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
