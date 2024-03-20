import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from '@angular/material/list';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatNativeDateModule} from '@angular/material/core';
import {MatSelectModule} from '@angular/material/select';
import {MatDialogModule} from '@angular/material/dialog';
import {MatRadioModule} from '@angular/material/radio';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {FlexLayoutModule} from 'ng-flex-layout';

import {SharedModule} from '../shared/shared.module';
import {ProfileEditComponent} from './profile-edit/profile-edit.component';
import {ProfileEditContainerComponent} from './profile-edit/profile-edit-container.component';
import {ProfileFindPopupComponent} from './profile-find-popup/profile-find-popup.component';
import {ProfileEditStartComponent} from './profile-edit-start/profile-edit-start.component';
import {UsattRecordSearchComponent} from './usatt-record-search/usatt-record-search.component';
import {OnBoardCompleteComponent} from './on-board-complete/on-board-complete.component';
import {UsattRecordSearchPopupComponent} from './usatt-record-search-popup/usatt-record-search-popup.component';
import {ClubModule} from '../club/club/club.module';
import {ProfileAddByTDComponent} from './profile-add-by-td/profile-add-by-td.component';
import {ProfileAddByTdContainerComponent} from './profile-add-by-td/profile-add-by-td-container.component';
import {ProfileRoutingModule} from './profile-routing.module';

@NgModule({
  declarations: [
    ProfileEditComponent,
    ProfileEditContainerComponent,
    ProfileFindPopupComponent,
    ProfileEditStartComponent,
    UsattRecordSearchComponent,
    OnBoardCompleteComponent,
    UsattRecordSearchPopupComponent,
    ProfileAddByTDComponent,
    ProfileAddByTdContainerComponent
  ],
  exports: [
    ProfileFindPopupComponent
  ],
  imports: [
    CommonModule,
    ProfileRoutingModule,
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
    MatProgressBarModule,
    MatAutocompleteModule,
    ClubModule
  ]
})
export class ProfileModule {
}
