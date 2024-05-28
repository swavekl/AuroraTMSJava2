import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {UserRoles} from '../user/user-roles.enum';
import {ScoreBoardConfigureContainerComponent} from './score-board-configure/score-board-configure-container.component';
import {ScoreBoardMatchSelectContainerComponent} from './score-board-match-select/score-board-match-select-container.component';
import {ScoreBoardScoreEntryContainerComponent} from './score-board-score-entry/score-board-score-entry-container.component';

const routes: Routes = [
  {
    path: '', component: ScoreBoardConfigureContainerComponent,
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_DIGITAL_SCORE_BOARDS]
    }
  },
  {
    path: 'selectmatch/:tournamentId/:tournamentDay/:tableNumber', component: ScoreBoardMatchSelectContainerComponent,
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_DIGITAL_SCORE_BOARDS]
    }
  },
  {
    path: 'scoreentry/:tournamentId/:tournamentDay/:tableNumber/:matchCardId/:matchIndex', component: ScoreBoardScoreEntryContainerComponent,
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_DIGITAL_SCORE_BOARDS]
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ScoreBoardRoutingModule { }
