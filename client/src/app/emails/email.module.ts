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


@NgModule({
  declarations: [
    EmailComponent,
    EmailContainerComponent
  ],
  imports: [
    CommonModule,
    EmailRoutingModule,
    MatToolbarModule,
    MatButtonModule,
    FlexLayoutModule,
    FormsModule,
    MatTooltipModule
  ]
})
export class EmailModule { }
