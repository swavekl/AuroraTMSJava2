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
import {FlexLayoutModule} from 'ng-flex-layout';
import {DebounceClicksDirective} from './directives/debounce-clicks.directive';
import {SelectionsDifferentDirective} from './directives/selections-different.directive';
import {ValuesMatchDirective} from './directives/values-match.directive';
import {MessageAreaComponent} from './message-area/message-area.component';
import {TypographyComponent} from './typograhpy/typography.component';
import {DateRangePipe} from './pipes/date-range.pipe';
import {ErrorMessagePopupComponent} from './error-message-dialog/error-message-popup.component';
import {StarLevelComponent} from './star-level/star-level.component';
import {MatIconModule} from '@angular/material/icon';
import {MatListModule} from '@angular/material/list';
import {RoundNamePipe} from './pipes/round-name.pipe';
import {UploadButtonComponent} from './upload-button/upload-button.component';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {DownloadButtonComponent} from './download-file/download-button.component';
import {getSaver, SAVER} from './download-service/saver.provider';
import {DateAfterDirective} from './directives/date-after.directive';
import { PhonePipe } from './pipes/phone.pipe';
import {TimerDisplayComponent} from './timer-display/timer-display.component';
import {TimerFormatterPipe} from './pipes/timer-formatter.pipe';
import {CardsDisplayComponent} from './cards-display/cards-display.component';
import {PasswordStrengthComponent} from './password-strength/password-strength.component';
import {HtmlContentPopupComponent} from './html-content-popup/html-content-popup.component';


@NgModule({
  declarations: [
    LinearProgressBarComponent,
    DateBeforeDirective,
    DateAfterDirective,
    ConfirmationPopupComponent,
    StartTimePipe,
    EventDayPipePipe,
    CenteredPanelComponent,
    DebounceClicksDirective,
    MessageAreaComponent,
    TypographyComponent,
    DateRangePipe,
    ErrorMessagePopupComponent,
    StarLevelComponent,
    RoundNamePipe,
    UploadButtonComponent,
    DownloadButtonComponent,
    SelectionsDifferentDirective,
    ValuesMatchDirective,
    PhonePipe,
    TimerDisplayComponent,
    CardsDisplayComponent,
    PasswordStrengthComponent,
    HtmlContentPopupComponent
  ],
  exports: [
    LinearProgressBarComponent,
    DateBeforeDirective,
    DateAfterDirective,
    ConfirmationPopupComponent,
    StartTimePipe,
    EventDayPipePipe,
    DateRangePipe,
    CenteredPanelComponent,
    DebounceClicksDirective,
    MessageAreaComponent,
    TypographyComponent,
    StarLevelComponent,
    RoundNamePipe,
    UploadButtonComponent,
    DownloadButtonComponent,
    SelectionsDifferentDirective,
    ValuesMatchDirective,
    PhonePipe,
    TimerDisplayComponent,
    CardsDisplayComponent,
    PasswordStrengthComponent,
    HtmlContentPopupComponent
  ],
  imports: [
    CommonModule,
    MatProgressBarModule,
    MatCardModule,
    MatDialogModule,
    MatButtonModule,
    FlexLayoutModule,
    MatIconModule,
    MatListModule,
    MatProgressSpinnerModule,
    TimerFormatterPipe
  ],
  providers: [
    LocalStorageService,
    {provide: SAVER, useFactory: getSaver}
  ]
})
export class SharedModule {
}
