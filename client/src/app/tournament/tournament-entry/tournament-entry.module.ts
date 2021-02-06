import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TournamentEntryRoutingModule } from './tournament-entry-routing.module';
import { EntryWizardContainerComponent } from './entry-wizard/entry-wizard-container.component';
import { EntryWizardComponent } from './entry-wizard/entry-wizard.component';
import {SharedModule} from '../../shared/shared.module';
import {FormsModule} from '@angular/forms';
import {MatStepperModule} from '@angular/material/stepper';
import {MatButtonModule} from '@angular/material/button';
import {MatRadioModule} from '@angular/material/radio';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatListModule} from '@angular/material/list';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatIconModule} from '@angular/material/icon';
import {MatTableModule} from '@angular/material/table';
import {MatToolbarModule} from '@angular/material/toolbar';
import { EventEntryStatusPipe } from './entry-wizard/pipes/event-entry-status.pipe';


@NgModule({
  declarations: [
    EntryWizardContainerComponent,
    EntryWizardComponent,
    EventEntryStatusPipe
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
    MatToolbarModule
  ],
  exports: [
  ]
})
export class TournamentEntryModule { }
