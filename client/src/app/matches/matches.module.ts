import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {MatListModule} from '@angular/material/list';
import {FlexModule} from '@angular/flex-layout';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatDialogModule} from '@angular/material/dialog';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatInputModule} from '@angular/material/input';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatToolbarModule} from '@angular/material/toolbar';

import {EntityDataService, EntityServices} from '@ngrx/data';

import {MatchesRoutingModule} from './matches-routing.module';
import {MatchesComponent} from './matches/matches.component';
import {MatchesContainerComponent} from './matches/matches-container.component';
import {MatchesLandingContainerComponent} from './matches-landing/matches-landing-container.component';
import {MatchesLandingComponent} from './matches-landing/matches-landing.component';
import {ScoreEntryDialogComponent} from './score-entry-dialog/score-entry-dialog.component';
import {MatchCardService} from './service/match-card.service';
import {MatchService} from './service/match.service';
import {SharedModule} from '../shared/shared.module';
import {ScoreEntryPhoneComponent} from './score-entry-phone/score-entry-phone.component';
import {ScoreEntryPhoneContainerComponent} from './score-entry-phone/score-entry-phone-container.component';
import {TieBreakingResultsDialogComponent} from './tie-breaking-results-dialog/tie-breaking-results-dialog.component';
import {ScoreAuditDialogComponent} from './score-audit-dialog/score-audit-dialog.component';
import {RankingResultsContainerComponent} from './ranking-results/ranking-results-container.component';
import {RankingResultsComponent} from './ranking-results/ranking-results.component';


@NgModule({
  declarations: [
    MatchesComponent,
    MatchesContainerComponent,
    MatchesLandingContainerComponent,
    MatchesLandingComponent,
    ScoreEntryDialogComponent,
    ScoreEntryPhoneComponent,
    ScoreEntryPhoneContainerComponent,
    TieBreakingResultsDialogComponent,
    ScoreAuditDialogComponent,
    RankingResultsComponent,
    RankingResultsContainerComponent
  ],
    imports: [
        CommonModule,
        FormsModule,
        MatchesRoutingModule,
        MatListModule,
        FlexModule,
        MatIconModule,
        MatButtonModule,
        MatDialogModule,
        MatCheckboxModule,
        MatInputModule,
        SharedModule,
        MatToolbarModule,
        MatSnackBarModule
    ]
})
export class MatchesModule {
  // Inject the service to ensure it registers with EntityServices
  constructor(
    entityServices: EntityServices,
    entityDataService: EntityDataService,
    matchCardService: MatchCardService,
    matchService: MatchService
  ) {

    // register service for contacting REST API because it doesn't follow the pattern of standard REST call
    // entityDataService.registerService('DrawItem', drawDataService);

    entityServices.registerEntityCollectionServices([
      matchCardService,
      matchService
    ]);
  }
}
