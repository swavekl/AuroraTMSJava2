import {AfterViewInit, ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {TournamentConfigService} from '../tournament-config.service';
import {Observable, of, Subscription} from 'rxjs';
import {Tournament} from '../tournament.model';
import {TournamentConfigEditComponent} from './tournament-config-edit.component';
import {createSelector} from '@ngrx/store';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';

@Component({
  selector: 'app-tournament-config-edit-container',
  template: `
    <app-tournament-config-edit [tournament]="tournament$ | async"
                                (saved)="onSave($event)"
                                (canceled)="onCancel($event)"></app-tournament-config-edit>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentConfigEditContainerComponent implements OnInit, OnDestroy, AfterViewInit {

  tournament$: Observable<Tournament>;
  private subscriptions: Subscription = new Subscription();

  // child component reference for getting back to the events list tab
  @ViewChild(TournamentConfigEditComponent)
  tournamentConfigEditComponent: TournamentConfigEditComponent;

  constructor(public tournamentConfigService: TournamentConfigService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService) {
  }

  ngOnInit(): void {
    const loadingSubscription = this.tournamentConfigService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);

    const routePath = this.activatedRoute.snapshot.routeConfig.path;
    const fromId = this.activatedRoute.snapshot.queryParams['from'];
    const id = this.activatedRoute.snapshot.params['id'] || 0;
    const creating: boolean = (routePath.endsWith('create'));

    const selectedTournamentId = (!creating) ? id : (fromId) ? fromId : 0;

    // create selector which will select tournament from the cached tournaments (entityMap) id : tournament
    const entityMapSelector = this.tournamentConfigService.selectors.selectEntityMap;
    const selectedTournamentSelector = createSelector(
      entityMapSelector,
      (entityMap) => {
        return entityMap[selectedTournamentId];
      });

    // use selector on the store and once it is activated make a copy of this tournament
    const selectedTournament$ = this.tournamentConfigService.store.select(selectedTournamentSelector);
    const subscription = selectedTournament$.subscribe((next: Tournament) => {
      let tournamentToEdit = next;
      if (creating) {
        if (fromId) {
          tournamentToEdit = Tournament.cloneTournament(tournamentToEdit);
        } else {
          tournamentToEdit = Tournament.makeDefault();
        }
      } else {
        // if tournament was not found in cache - e.g. direct navigation by url emailed to you
        if (!tournamentToEdit) {
          this.tournament$ = this.tournamentConfigService.getByKey(selectedTournamentId);
          return;
        } else {
          tournamentToEdit = Tournament.convert(tournamentToEdit);  // convert string dates to date objects
        }
      }
      // make this tournament into observable
      this.tournament$ = of(tournamentToEdit);
    });
    this.subscriptions.add(subscription);
  }

  ngOnDestroy(): void {
    if (this.subscriptions) {
      this.subscriptions.unsubscribe();
    }
  }


  ngAfterViewInit(): void {
    // if we are coming back from adding a new event then we want to make Events tab the active tab
    const activateTab = this.activatedRoute.snapshot.queryParams['activateTab'];
    if (this.tournamentConfigEditComponent && activateTab) {
      this.tournamentConfigEditComponent.setActiveTab(activateTab);
    }
  }

  onSave(tournament: Tournament) {
    this.tournamentConfigService.upsert(tournament, null).subscribe(
      () => this.navigateBack(),
      (err: any) => console.log('error saving ' + err)
    );
  }

  onCancel($event: any) {
    this.navigateBack();
  }

  // back to the list of tournaments
  navigateBack() {
    this.router.navigateByUrl('/tournamentsconfig');
  }

}
