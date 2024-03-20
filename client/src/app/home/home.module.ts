import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from '@angular/material/list';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';
import {FlexLayoutModule} from 'ng-flex-layout';
import {SharedModule} from '../shared/shared.module';
import {HomeComponent} from './home/home.component';
import {UserWelcomeComponent} from './user-welcome/user-welcome.component';
import {HomeRoutingModule} from './home-routing.module';


@NgModule({
  declarations: [
    HomeComponent,
    UserWelcomeComponent
  ],
    imports: [
        HomeRoutingModule,
        CommonModule,
        MatButtonModule,
        MatCardModule,
        MatInputModule,
        MatListModule,
        MatToolbarModule,
        FlexLayoutModule,
        SharedModule,
        MatIconModule
    ],
  exports: [
    HomeComponent
  ]
})
export class HomeModule {
}
