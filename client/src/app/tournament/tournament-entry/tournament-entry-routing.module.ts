import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {EntryWizardContainerComponent} from './entry-wizard/entry-wizard-container.component';
import {AuthGuard} from '../../guards/auth.guard';
import {DoublesTeamsContainerComponent} from './doubles-teams/doubles-teams-container.component';
import {EntryWizardCanDeactivateGuard} from './entry-wizard/entry-wizard-can-deactivate.guard';
import {EntryViewContainerComponent} from './entry-view/entry-view-container.component';


const routes: Routes = [
  {
    path: 'entrywizard/:tournamentId/edit/:entryId',
    component: EntryWizardContainerComponent,
    canActivate: [AuthGuard],
    canDeactivate: [EntryWizardCanDeactivateGuard]
  },
  {
    path: 'doublesteams/:tournamentId',
    component: DoublesTeamsContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'entryview/:tournamentId/edit/:entryId',
    component: EntryViewContainerComponent,
    canActivate: [AuthGuard],
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TournamentEntryRoutingModule {
}
