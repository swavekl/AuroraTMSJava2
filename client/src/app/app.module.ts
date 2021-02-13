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

import {AppComponent} from './app.component';
import {AppRoutingModule} from './app-routing.module';
import {AuthInterceptor} from './auth.interceptor';
import {LayoutModule} from '@angular/cdk/layout';
import {AppStoreModule} from './store/app-store.module';
import {HomeModule} from './home/home.module';
import {TournamentModule} from './tournament/tournament/tournament.module';
import {TournamentConfigModule} from './tournament/tournament-config/tournament-config.module';
import {ProfileModule} from './profile/profile.module';
import {UserModule} from './user/user.module';
import {environment} from '../environments/environment';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {SharedModule} from './shared/shared.module';

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
    SharedModule
  ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
