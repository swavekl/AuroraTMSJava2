import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {FormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatStepperModule} from '@angular/material/stepper';
import {MatButtonModule} from '@angular/material/button';
import {MatRadioModule} from '@angular/material/radio';
import {MatListModule} from '@angular/material/list';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatIconModule} from '@angular/material/icon';
import {MatTableModule} from '@angular/material/table';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSelectModule} from '@angular/material/select';
import {MatCardModule} from '@angular/material/card';
import {FlexLayoutModule} from '@angular/flex-layout';

import {SharedModule} from '../../shared/shared.module';
import {ProfileModule} from '../../profile/profile.module';
import {TournamentEntryRoutingModule} from './tournament-entry-routing.module';

import {EntryWizardContainerComponent} from './entry-wizard/entry-wizard-container.component';
import {EntryWizardComponent} from './entry-wizard/entry-wizard.component';
import {EventEntryStatusPipe} from './entry-wizard/pipes/event-entry-status.pipe';
import {AvailabilityStatusPipe} from './entry-wizard/pipes/availability-status.pipe';
import {DoublesPairDialogComponent} from './doubles-pair-dialog/doubles-pair-dialog.component';
import {MaxRatingDirective} from './doubles-pair-dialog/max-rating.directive';
import {DoublesTeamsContainerComponent} from './doubles-teams/doubles-teams-container.component';
import {DoublesTeamsComponent} from './doubles-teams/doubles-teams.component';
import {AccountModule} from '../../account/account.module';
import {EntryViewComponent} from './entry-view/entry-view.component';
import {EntryViewContainerComponent} from './entry-view/entry-view-container.component';
import { EntrySummaryTableComponent } from './pricecalculator/entry-summary-table/entry-summary-table.component';
import { AddEntryComponent } from './add-entry/add-entry.component';
import { ChangeRatingDialogComponent } from './change-rating-dialog/change-rating-dialog.component';

@NgModule({
  declarations: [
    EntryWizardContainerComponent,
    EntryWizardComponent,
    EventEntryStatusPipe,
    AvailabilityStatusPipe,
    DoublesPairDialogComponent,
    DoublesTeamsComponent,
    DoublesTeamsContainerComponent,
    MaxRatingDirective,
    EntryViewComponent,
    EntryViewContainerComponent,
    EntrySummaryTableComponent,
    AddEntryComponent,
    ChangeRatingDialogComponent
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
        MatDialogModule,
        MatSelectModule,
        MatCardModule,
        ProfileModule,
        AccountModule
    ],
  exports: [
    DoublesTeamsContainerComponent
  ]
})
export class TournamentEntryModule {
}
