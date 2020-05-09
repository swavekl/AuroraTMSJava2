import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LinearProgressBarComponent} from './linear-progress-bar/linear-progress-bar.component';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {DateBeforeDirective} from './directives/date-before.directive';
import {ConfirmationPopupComponent} from './confirmation-popup/confirmation-popup.component';
import {MatCardModule} from '@angular/material/card';
import {MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';


@NgModule({
  declarations: [
    LinearProgressBarComponent,
    DateBeforeDirective,
    ConfirmationPopupComponent
  ],
  exports: [
    LinearProgressBarComponent,
    DateBeforeDirective,
    ConfirmationPopupComponent
  ],
  imports: [
    CommonModule,
    MatProgressBarModule,
    MatCardModule,
    MatDialogModule,
    MatButtonModule
  ]
})
export class SharedModule {
}
