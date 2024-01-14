import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { EmailRoutingModule } from './email-routing.module';
import { EmailComponent } from './email/email.component';
import { EmailContainerComponent } from './email/email-container.component';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {FlexLayoutModule} from '@angular/flex-layout';
import {FormsModule} from '@angular/forms';
import {MatTooltipModule} from '@angular/material/tooltip';
import { EmailServerConfigDialogComponent } from './email-server-config-dialog/email-server-config-dialog.component';
import {MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {SharedModule} from '../shared/shared.module';
import {MatProgressBarModule} from '@angular/material/progress-bar';


@NgModule({
  declarations: [
    EmailComponent,
    EmailContainerComponent,
    EmailServerConfigDialogComponent
  ],
  imports: [
    CommonModule,
    EmailRoutingModule,
    MatToolbarModule,
    MatButtonModule,
    FlexLayoutModule,
    FormsModule,
    MatTooltipModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    SharedModule,
    MatProgressBarModule
  ]
})
export class EmailModule { }
