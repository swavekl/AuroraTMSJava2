import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatListModule} from '@angular/material/list';
import {MatInputModule} from '@angular/material/input';
import {FlexModule} from '@angular/flex-layout';
import {FormsModule} from '@angular/forms';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSelectModule} from '@angular/material/select';
import {MatCardModule} from '@angular/material/card';
import {MatStepperModule} from '@angular/material/stepper';
import {MatRadioModule} from '@angular/material/radio';

import {ClubRoutingModule} from './club-routing.module';
import {ClubEditComponent} from './club-edit/club-edit.component';
import {ClubNameValidatorDirective} from './club-name-validator.directive';
import {ClubListComponent} from './club-list/club-list.component';
import {ClubAffiliationApplicationListComponent} from './club-affiliation-application-list/club-affiliation-application-list.component';
import {ClubAffiliationApplicationComponent} from './club-affiliation-application/club-affiliation-application.component';
// tslint:disable-next-line:max-line-length
import {ClubAffiliationApplicationContainerComponent} from './club-affiliation-application/club-affiliation-application-container.component';
import {MatTooltipModule} from '@angular/material/tooltip';


@NgModule({
  declarations: [
    ClubEditComponent,
    ClubNameValidatorDirective,
    ClubListComponent,
    ClubAffiliationApplicationListComponent,
    ClubAffiliationApplicationComponent,
    ClubAffiliationApplicationContainerComponent
  ],
  exports: [
    ClubNameValidatorDirective
  ],
    imports: [
        CommonModule,
        ClubRoutingModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatToolbarModule,
        MatIconModule,
        MatButtonModule,
        MatListModule,
        MatInputModule,
        FlexModule,
        FormsModule,
        MatDialogModule,
        MatSelectModule,
        MatCardModule,
        MatStepperModule,
        MatRadioModule,
        MatTooltipModule
    ]
})
export class ClubModule {
}
