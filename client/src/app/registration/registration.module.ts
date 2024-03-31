import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {RegistrationRoutingModule} from './registration-routing.module';
import {RegistrationListComponent} from './registration-list/registration-list.component';
import {RegistrationListContainerComponent} from './registration-list/registration-list-container.component';
import {MatListModule} from '@angular/material/list';
import {MatIconModule} from '@angular/material/icon';
import {SharedModule} from '../shared/shared.module';
import {FlexLayoutModule} from 'ng-flex-layout';
import {MatButton} from '@angular/material/button';
import {MatToolbar} from '@angular/material/toolbar';


@NgModule({
  declarations: [
    RegistrationListComponent,
    RegistrationListContainerComponent,
  ],
    imports: [
        CommonModule,
        RegistrationRoutingModule,
        MatListModule,
        MatIconModule,
        SharedModule,
        FlexLayoutModule,
        MatButton,
        MatToolbar
    ]
})
export class RegistrationModule {
}
