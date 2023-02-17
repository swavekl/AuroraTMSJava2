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

const appearance: MatFormFieldDefaultOptions = {
  appearance: 'fill'
};

@NgModule({
  declarations: [
    AppComponent,
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
}
