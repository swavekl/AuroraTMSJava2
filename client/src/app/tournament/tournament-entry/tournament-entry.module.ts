import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {TournamentEntryRoutingModule} from './tournament-entry-routing.module';
import {EntryWizardContainerComponent} from './entry-wizard/entry-wizard-container.component';
import {EntryWizardComponent} from './entry-wizard/entry-wizard.component';
import {SharedModule} from '../../shared/shared.module';
import {FormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatStepperModule} from '@angular/material/stepper';
import {MatButtonModule} from '@angular/material/button';
import {MatRadioModule} from '@angular/material/radio';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatListModule} from '@angular/material/list';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatIconModule} from '@angular/material/icon';
import {MatTableModule} from '@angular/material/table';
import {MatToolbarModule} from '@angular/material/toolbar';
import {EventEntryStatusPipe} from './entry-wizard/pipes/event-entry-status.pipe';
import {AvailabilityStatusPipe} from './entry-wizard/pipes/availability-status.pipe';
import {ProfileModule} from '../../profile/profile.module';
import { DoublesPairDialogComponent } from './doubles-pair-dialog/doubles-pair-dialog.component';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSelectModule} from '@angular/material/select';
import { SelectionsDifferentDirective } from './doubles-pair-dialog/selections-different.directive';

@NgModule({
  declarations: [
    EntryWizardContainerComponent,
    EntryWizardComponent,
    EventEntryStatusPipe,
    AvailabilityStatusPipe,
    DoublesPairDialogComponent,
    SelectionsDifferentDirective
  ],
  imports: [
    CommonModule,
    TournamentEntryRoutingModule,
    SharedModule,
    FormsModule,
    MatStepperModule,
    MatButtonModule,
    MatRadioModule,
    FlexLayoutModule,
    MatListModule,
    MatGridListModule,
    MatIconModule,
    MatTableModule,
    MatToolbarModule,
    MatFormFieldModule,
    MatInputModule,
    ProfileModule,
    MatDialogModule,
    MatSelectModule
  ],
  exports: []
})
export class TournamentEntryModule {
}
