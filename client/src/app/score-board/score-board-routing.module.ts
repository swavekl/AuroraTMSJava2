import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {UserRoles} from '../user/user-roles.enum';
import {ScoreBoardConfigureContainerComponent} from './score-board-configure/score-board-configure-container.component';
import {ScoreBoardContainerComponent} from './score-board/score-board-container.component';

const routes: Routes = [
  {
    path: '', component: ScoreBoardConfigureContainerComponent,
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_DIGITAL_SCORE_BOARDS]
    }
  },
  {
    path: 'scoreentry/:tournamentId/:tournamentDay/:tableNumber', component: ScoreBoardContainerComponent,
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
