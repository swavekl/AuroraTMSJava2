import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {TournamentInfoService} from '../tournament-info.service';
import {Observable, Subscription} from 'rxjs';
import {TournamentInfo} from '../tournament-info.model';
import {LocalStorageService} from '../../../shared/local-storage.service';
import {Regions} from '../../../shared/regions';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';

@Component({
  selector: 'app-tournament-list-container',
  template: `
    <app-tournament-list [tournaments]="tournaments$ | async"
                         [selectedRegion]="selectedRegion"
                         (filterChange)="onFilterChange($event)"></app-tournament-list>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush

})
export class TournamentListContainerComponent implements OnInit, OnDestroy {

  tournaments$: Observable<TournamentInfo[]>;
  LAST_FILTER = 'tournaments-filter';
  selectedRegion: string;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentInfoService: TournamentInfoService,
              private localStorageService: LocalStorageService,
              private linearProgressBarService: LinearProgressBarService) {
    this.tournaments$ = this.tournamentInfoService.filteredEntities$;
    const subscription = this.tournamentInfoService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(subscription);
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

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
