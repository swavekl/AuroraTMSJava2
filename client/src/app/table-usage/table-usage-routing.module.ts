import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TableUsageContainerComponent} from './table-usage/table-usage-container.component';

const routes: Routes = [
  {
    path: '', component: TableUsageContainerComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TableUsageRoutingModule { }
