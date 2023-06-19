import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {FlexModule} from '@angular/flex-layout';
import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {MatCardModule} from '@angular/material/card';
import {MatStepperModule} from '@angular/material/stepper';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatNativeDateModule} from '@angular/material/core';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatRadioModule} from '@angular/material/radio';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatListModule} from '@angular/material/list';
import {MatToolbarModule} from '@angular/material/toolbar';

import {SanctionRoutingModule} from './sanction-routing.module';
import {SanctionRequestListComponent} from './sanction-request-list/sanction-request-list.component';
import {SanctionRequestEditComponent} from './sanction-edit/sanction-request-edit.component';
import {SanctionRequestEditContainerComponent} from './sanction-edit/sanction-request-edit-container.component';
import {AccountModule} from '../account/account.module';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {SharedModule} from '../shared/shared.module';
import { SanctionRequestCreateComponent } from './sanction-request-create/sanction-request-create.component';


@NgModule({
  declarations: [
    SanctionRequestListComponent,
    SanctionRequestEditComponent,
    SanctionRequestEditContainerComponent,
    SanctionRequestCreateComponent
  ],
    imports: [
        CommonModule,
        SanctionRoutingModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatCardModule,
        FormsModule,
        MatStepperModule,
        FlexModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatInputModule,
        MatSelectModule,
        MatExpansionModule,
        MatRadioModule,
        MatCheckboxModule,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        MatListModule,
        MatToolbarModule,
        AccountModule,
        MatAutocompleteModule,
        SharedModule
    ]
})
export class SanctionModule { }
