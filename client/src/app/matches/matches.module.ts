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
export class MatchesModule { }
