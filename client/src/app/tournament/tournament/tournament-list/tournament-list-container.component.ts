import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {TournamentInfoService} from '../tournament-info.service';
import {Observable} from 'rxjs';
import {TournamentInfo} from '../tournament-info.model';
import {LocalStorageService} from '../../../shared/local-storage.service';
import {Regions} from '../../../shared/regions';

@Component({
  selector: 'app-tournament-list-container',
  template: `
    <app-linear-progress-bar [loading]="loading$ | async"></app-linear-progress-bar>
    <app-tournament-list [tournaments]="tournaments$ | async"
                         [selectedRegion]="selectedRegion"
                         (filterChange)="onFilterChange($event)"></app-tournament-list>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush

})
export class TournamentListContainerComponent implements OnInit {

  tournaments$: Observable<TournamentInfo[]>;
  loading$: Observable<boolean>;
  LAST_FILTER = 'tournaments-filter';
  selectedRegion: string;

  constructor(private tournamentInfoService: TournamentInfoService,
              private localStorageService: LocalStorageService) {
    this.tournaments$ = this.tournamentInfoService.filteredEntities$;
    this.loading$ = this.tournamentInfoService.loading$;
  }

  ngOnInit(): void {
    // restore region filter
    this.selectedRegion = this.localStorageService.getSavedState(this.LAST_FILTER);
    this.setFilter(this.selectedRegion);

    this.tournamentInfoService.getAll();
  }

  onFilterChange(selectedRegion: string) {
    this.setFilter(selectedRegion);
    // persist state
    this.localStorageService.setSavedState(selectedRegion, this.LAST_FILTER);
  }

  setFilter(selectedRegion: string) {
    const regions: any [] = new Regions().getList();
    let states = null;
    for (const region of regions) {
      if (region.name === selectedRegion) {
        states = region.states;
        break;
      }
    }
    this.tournamentInfoService.setFilter({
      states: states
    });
  }
}
