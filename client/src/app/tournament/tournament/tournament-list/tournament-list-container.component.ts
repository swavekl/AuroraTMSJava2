import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {TournamentInfoService} from '../../service/tournament-info.service';
import {Observable, Subscription} from 'rxjs';
import {TournamentInfo} from '../../model/tournament-info.model';
import {LocalStorageService} from '../../../shared/local-storage.service';
import {Regions} from '../../../shared/regions';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {TodayService} from '../../../shared/today.service';
import moment from 'moment/moment';

@Component({
    selector: 'app-tournament-list-container',
    template: `
    <app-tournament-list [tournaments]="tournaments$ | async"
                         [selectedRegion]="selectedRegion"
                         (filterChange)="onFilterChange($event)"></app-tournament-list>
  `,
    styles: [],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class TournamentListContainerComponent implements OnInit, OnDestroy {

  tournaments$: Observable<TournamentInfo[]>;
  LAST_FILTER = 'tournaments-filter';
  selectedRegion: string;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentInfoService: TournamentInfoService,
              private localStorageService: LocalStorageService,
              private linearProgressBarService: LinearProgressBarService,
              private todayService: TodayService) {
    this.tournaments$ = this.tournamentInfoService.filteredEntities$;
    const subscription = this.tournamentInfoService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(subscription);
  }

  ngOnInit(): void {
    // restore region filter
    const selectRegion = history?.state?.selectRegion;
    if (selectRegion) {
      this.selectedRegion = selectRegion;
    } else {
      // otherwise get last saved region form local storage
      this.selectedRegion = this.localStorageService.getSavedState(this.LAST_FILTER);
    }
    this.setFilter(this.selectedRegion);

    const listingDate = this.getListingDate();
    this.tournamentInfoService.getAllRecentAndFuture(listingDate);
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
    const listingDate = this.getListingDate();
    this.tournamentInfoService.setFilter({
      states: states,
      startDate: listingDate,
      endDate: null
    });
  }

  // gets a listing date which is one month back from today
  // this way some tournaments that have alredy completed can be shown
  // to view results
  getListingDate () {
    return moment(this.todayService.todaysDate).subtract(1, 'months').toDate();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
