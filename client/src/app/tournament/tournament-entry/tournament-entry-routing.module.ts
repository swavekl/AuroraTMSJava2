import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {EntryWizardContainerComponent} from './entry-wizard/entry-wizard-container.component';


const routes: Routes = [
  {
    path: 'entrywizard/:tournamentId/edit/:entryId',
    component: EntryWizardContainerComponent
  },
  {
    path: 'entrywizard/:tournamentId/create',
    component: EntryWizardContainerComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TournamentEntryRoutingModule {
}
