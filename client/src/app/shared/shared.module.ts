import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LinearProgressBarComponent} from './linear-progress-bar/linear-progress-bar.component';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {DateBeforeDirective} from './directives/date-before.directive';
import {ConfirmationPopupComponent} from './confirmation-popup/confirmation-popup.component';
import {MatCardModule} from '@angular/material/card';
import {MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {LocalStorageService} from './local-storage.service';
import {StartTimePipe} from './pipes/start-time.pipe';
import {EventDayPipePipe} from './pipes/event-day-pipe.pipe';
import {CenteredPanelComponent} from './centered-panel/centered-panel.component';
import {FlexLayoutModule} from '@angular/flex-layout';
import { DebounceClicksDirective } from './directives/debounce-clicks.directive';


@NgModule({
  declarations: [
    LinearProgressBarComponent,
    DateBeforeDirective,
    ConfirmationPopupComponent,
    StartTimePipe,
    EventDayPipePipe,
    CenteredPanelComponent,
    DebounceClicksDirective
  ],
  exports: [
    LinearProgressBarComponent,
    DateBeforeDirective,
    ConfirmationPopupComponent,
    StartTimePipe,
    EventDayPipePipe,
    CenteredPanelComponent,
    DebounceClicksDirective
  ],
    imports: [
        CommonModule,
        MatProgressBarModule,
        MatCardModule,
        MatDialogModule,
        MatButtonModule,
        FlexLayoutModule
    ],
  providers: [
    LocalStorageService
  ]
})
export class SharedModule {
}
