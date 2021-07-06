import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatchesRoutingModule } from './matches-routing.module';
import { MatchesComponent } from './matches/matches.component';
import { MatchesContainerComponent } from './matches/matches-container.component';
import { MatchesLandingContainerComponent } from './matches-landing/matches-landing-container.component';
import { MatchesLandingComponent } from './matches-landing/matches-landing.component';
import {MatListModule} from '@angular/material/list';
import {FlexModule} from '@angular/flex-layout';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {EntityDataService, EntityServices} from '@ngrx/data';
import {MatchCardService} from './service/match-card.service';


@NgModule({
  declarations: [
    MatchesComponent,
    MatchesContainerComponent,
    MatchesLandingContainerComponent,
    MatchesLandingComponent
  ],
  imports: [
    CommonModule,
    MatchesRoutingModule,
    MatListModule,
    FlexModule,
    MatIconModule,
    MatButtonModule
  ]
})
export class MatchesModule {
  // Inject the service to ensure it registers with EntityServices
  constructor(
    entityServices: EntityServices,
    entityDataService: EntityDataService,
    matchCardService: MatchCardService
  ) {

    // register service for contacting REST API because it doesn't follow the pattern of standard REST call
    // entityDataService.registerService('DrawItem', drawDataService);

    entityServices.registerEntityCollectionServices([
      matchCardService
    ]);
  }
}
