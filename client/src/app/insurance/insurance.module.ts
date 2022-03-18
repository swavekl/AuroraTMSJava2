import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {MatCardModule} from '@angular/material/card';
import {FlexModule} from '@angular/flex-layout';
import {FormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatListModule} from '@angular/material/list';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatRadioModule} from '@angular/material/radio';
import {MatSelectModule} from '@angular/material/select';
import {MatInputModule} from '@angular/material/input';

import {MatToolbarModule} from '@angular/material/toolbar';
import {InsuranceRoutingModule} from './insurance-routing.module';
import {InsuranceListComponent} from './insurance-list/insurance-list.component';
import {InsuranceComponent} from './insurance/insurance.component';
import {InsuranceContainerComponent} from './insurance/insurance-container.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import {SharedModule} from '../shared/shared.module';


@NgModule({
  declarations: [
    InsuranceListComponent,
    InsuranceComponent,
    InsuranceContainerComponent
  ],
    imports: [
        CommonModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatCardModule,
        MatIconModule,
        FlexModule,
        MatButtonModule,
        MatListModule,
        MatDatepickerModule,
        // MatFormFieldModule,
        MatRadioModule,
        MatSelectModule,
        FormsModule,
        MatInputModule,
        MatToolbarModule,
        InsuranceRoutingModule,
        MatTooltipModule,
        SharedModule
    ]
})
export class InsuranceModule {
}
