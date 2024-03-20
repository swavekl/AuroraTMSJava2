import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatListModule} from '@angular/material/list';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {MatCardModule} from '@angular/material/card';
import {MatStepperModule} from '@angular/material/stepper';
import {MatRadioModule} from '@angular/material/radio';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatNativeDateModule} from '@angular/material/core';

import {FlexLayoutModule} from 'ng-flex-layout';

import {AccountModule} from '../../account/account.module';
import {ClubAffiliationRoutingModule} from './club-affiliation-routing.module';
import {ClubAffiliationApplicationListComponent} from './club-affiliation-application-list/club-affiliation-application-list.component';
import {
  ClubAffiliationApplicationContainerComponent
} from './club-affiliation-application/club-affiliation-application-container.component';
import {ClubAffiliationApplicationComponent} from './club-affiliation-application/club-affiliation-application.component';


@NgModule({
  declarations: [
    ClubAffiliationApplicationListComponent,
    ClubAffiliationApplicationContainerComponent,
    ClubAffiliationApplicationComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    MatToolbarModule,
    MatIconModule,
    MatFormFieldModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatCardModule,
    MatButtonModule,
    MatInputModule,
    MatStepperModule,
    MatSelectModule,
    MatRadioModule,
    MatTooltipModule,
    MatListModule,
    MatDatepickerModule,
    MatNativeDateModule,
    FlexLayoutModule,
    ClubAffiliationRoutingModule,
    AccountModule
  ]
})
export class ClubAffiliationModule {
}
