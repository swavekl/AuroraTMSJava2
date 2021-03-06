import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Routes, RouterModule} from '@angular/router';
// import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from '@angular/material/list';
import {MatToolbarModule} from '@angular/material/toolbar';
import {ProfileEditComponent} from './profile-edit/profile-edit.component';
import {AuthGuard} from '../guards/auth.guard';
import {HttpClientModule} from '@angular/common/http';
import {FormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {ProfileEditContainerComponent} from './profile-edit/profile-edit-container.component';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatSelectModule} from '@angular/material/select';
import {FlexLayoutModule} from '@angular/flex-layout';
import {ProfileFindPopupComponent} from './profile-find-popup/profile-find-popup.component';
import {MatDialogModule} from '@angular/material/dialog';
import {SharedModule} from '../shared/shared.module';
import {MatRadioModule} from '@angular/material/radio';
import {MatNativeDateModule} from '@angular/material/core';
import { ProfileEditStartComponent } from './profile-edit-start/profile-edit-start.component';
import { UsattRecordSearchComponent } from './usatt-record-search/usatt-record-search.component';
import { OnBoardCompleteComponent } from './on-board-complete/on-board-complete.component';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import { UsattRecordSearchPopupComponent } from './usatt-record-search-popup/usatt-record-search-popup.component';

const profileRoutes: Routes = [
  {
    path: 'userprofile/:profileId',
    component: ProfileEditContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'userprofilestart',
    component: ProfileEditStartComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'onboardcomplete',
    component: OnBoardCompleteComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  declarations: [
    ProfileEditComponent,
    ProfileEditContainerComponent,
    ProfileFindPopupComponent,
    ProfileEditStartComponent,
    UsattRecordSearchComponent,
    OnBoardCompleteComponent,
    UsattRecordSearchPopupComponent
  ],
  exports: [
    ProfileFindPopupComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(profileRoutes),
    // BrowserAnimationsModule,
    MatButtonModule,
    MatCardModule,
    MatInputModule,
    MatListModule,
    MatToolbarModule,
    HttpClientModule,
    FormsModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    FlexLayoutModule,
    MatDialogModule,
    SharedModule,
    MatRadioModule,
    MatProgressBarModule
  ]
})
export class ProfileModule {
}
