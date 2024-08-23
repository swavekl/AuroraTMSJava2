import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatFormFieldModule} from '@angular/material/form-field';
import {FormsModule} from '@angular/forms';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {MatInputModule} from '@angular/material/input';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';

import {FlexLayoutModule} from 'ng-flex-layout';
import {SharedModule} from '../shared/shared.module';
import {ScoreBoardRoutingModule} from './score-board-routing.module';
import {ScoreBoardMatchSelectionComponent} from './score-board-match-select/score-board-match-selection.component';
import {ScoreBoardConfigureComponent} from './score-board-configure/score-board-configure.component';
import {ScoreBoardConfigureContainerComponent} from './score-board-configure/score-board-configure-container.component';
import {ScoreBoardMatchSelectContainerComponent} from './score-board-match-select/score-board-match-select-container.component';
import {ScoreBoardScoreEntryComponent} from './score-board-score-entry/score-board-score-entry.component';
import {ScoreBoardScoreEntryContainerComponent} from './score-board-score-entry/score-board-score-entry-container.component';
import {MatIcon} from '@angular/material/icon';
import {TimerFormatterPipe} from '../shared/pipes/timer-formatter.pipe';
import {ScoreBoardMatchStartComponent} from './score-board-match-start/score-board-match-start.component';
import {
  ScoreBoardMatchStartContainerComponent
} from './score-board-match-start/score-board-match-start-container.component';
import {MatRadioButton, MatRadioGroup} from '@angular/material/radio';
import {ScoreTableComponent} from '../matches/score-table/score-table.component';
import {MatchesModule} from '../matches/matches.module';
import { CardsPopupComponent } from './cards-popup/cards-popup.component';
import {MatDialogActions, MatDialogContent, MatDialogTitle} from '@angular/material/dialog';


@NgModule({
  declarations: [
    ScoreBoardConfigureComponent,
    ScoreBoardConfigureContainerComponent,
    ScoreBoardMatchSelectionComponent,
    ScoreBoardMatchSelectContainerComponent,
    ScoreBoardScoreEntryComponent,
    ScoreBoardScoreEntryContainerComponent,
    ScoreBoardMatchStartComponent,
    ScoreBoardMatchStartContainerComponent,
    CardsPopupComponent
  ],
  imports: [
    CommonModule,
    ScoreBoardRoutingModule,
    SharedModule,
    MatFormFieldModule,
    MatSelectModule,
    FormsModule,
    FlexLayoutModule,
    MatInputModule,
    MatGridListModule,
    MatButtonModule,
    MatIcon,
    MatToolbar,
    MatToolbarRow,
    TimerFormatterPipe,
    MatRadioGroup,
    MatRadioButton,
    MatchesModule,
    MatDialogContent,
    MatDialogActions,
    MatDialogTitle
  ]
})
export class ScoreBoardModule {
}
