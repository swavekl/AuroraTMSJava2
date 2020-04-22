import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Routes, RouterModule} from '@angular/router';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatToolbarModule } from '@angular/material/toolbar';
import {HomeComponent} from './home/home.component';
import { UserWelcomeComponent } from './user-welcome/user-welcome.component';

const homeRoutes: Routes = [
    {
      path: 'home',
      component: HomeComponent
    },
  {
    path: 'userwelcome',
    component: UserWelcomeComponent
  }
  ];

@NgModule({
  declarations: [
    HomeComponent,
    UserWelcomeComponent
  ],
  imports: [
    RouterModule.forChild(homeRoutes),
    CommonModule,
    BrowserAnimationsModule,
    MatButtonModule,
    MatCardModule,
    MatInputModule,
    MatListModule,
    MatToolbarModule
  ],
  exports: [
    HomeComponent
  ]
})
export class HomeModule {
}
