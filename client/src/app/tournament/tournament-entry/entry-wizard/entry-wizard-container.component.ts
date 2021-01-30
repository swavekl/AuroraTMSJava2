import {Component, OnDestroy, OnInit} from '@angular/core';
import {TournamentEntry} from '../model/tournament-entry.model';
import {Observable, of, Subscription} from 'rxjs';
import {TournamentEntryService} from '../service/tournament-entry.service';
import {ActivatedRoute, Router} from '@angular/router';
import {createSelector} from '@ngrx/store';
import {TournamentInfoService} from '../../tournament/tournament-info.service';
import {TournamentInfo} from '../../tournament/tournament-info.model';
import {TournamentEventConfigService} from '../../tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';

@Component({
  selector: 'app-entry-wizard-container',
  template: `
    <app-linear-progress-bar [loading]="loading$ | async"></app-linear-progress-bar>
    <app-entry-wizard [entry]="entry$ | async"
                      [teamsTournament]="teamsTournament$ | async"
                      [allEvents]="tournamentEvents$ | async"
                      [otherPlayers]="otherPlayers$ | async">
    </app-entry-wizard>
  `,
  styles: []
})
export class EntryWizardContainerComponent implements OnInit, OnDestroy {

  entry$: Observable<TournamentEntry>;
  loading$: Observable<boolean>;

  teamsTournament$: Observable<boolean>;
  otherPlayers$: Observable<any>;
  tournamentEvents$: Observable<any>;

  private subscription: Subscription;
  private subscription2: Subscription;
  private subscription3: Subscription;

  constructor(private tournamentEntryService: TournamentEntryService,
              private tournamentInfoService: TournamentInfoService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private activatedRoute: ActivatedRoute,
              private router: Router) {
//    this.entry$ = this.tournamentEntryService.entity$;
    this.loading$ = this.tournamentEntryService.loading$;

    this.otherPlayers$ = of([
      {firstName: 'Mario', lastName: 'Lorenc', profileId: 2, entryId: 11},
      {firstName: 'Justine', lastName: 'Lorenc', profileId: 3, entryId: null},
      {firstName: 'Danielle', lastName: 'Lorenc', profileId: 4, entryId: null},
    ]);
  }

  ngOnInit(): void {
    const routePath = this.activatedRoute.snapshot.routeConfig.path;
    const creating: boolean = (routePath.endsWith('create'));

    const entryId = this.activatedRoute.snapshot.params['entryId'] || 0;
    const tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;

    this.selectEntry(entryId, creating, tournamentId);
    this.selectTournamentType(tournamentId);
    this.selectTournamentConfig(tournamentId);
  }

  /**
   *
   * @param entryId
   * @param creating
   * @param tournamentId
   */
  private selectEntry(entryId, creating: boolean, tournamentId) {
    const entityMapSelector = this.tournamentEntryService.selectors.selectEntityMap;
    const selectedEntrySelector = createSelector(
      entityMapSelector,
      (entityMap) => {
        return entityMap[entryId];
      });
    const selectedEntry$: Observable<TournamentEntry> = this.tournamentEntryService.store.select(selectedEntrySelector);
    this.subscription = selectedEntry$.subscribe((next: TournamentEntry) => {
      let entryToEdit = next;
      if (creating) {
        entryToEdit = new TournamentEntry();
        entryToEdit.tournamentId = tournamentId;
        entryToEdit.type = 0;
      } else {
        // editing - check if we had it in cache if not - then fetch it
        if (!entryToEdit) {
          this.entry$ = this.tournamentEntryService.getByKey(entryId);
          return;
        }
      }
      this.entry$ = of(entryToEdit);
    });
  }

  /**
   *
   * @param tournamentId
   */
  selectTournamentType(tournamentId: number): void {
    const tournamentInfoSelector = this.tournamentInfoService.selectors.selectEntityMap;
    const selectedTournamentSelector = createSelector(
      tournamentInfoSelector,
      (entityMap) => {
        return entityMap[tournamentId];
      });
    const selectedTournamentInfo$: Observable<TournamentInfo> =
      this.tournamentInfoService.store.select(selectedTournamentSelector);
    this.subscription2 = selectedTournamentInfo$.subscribe((next: TournamentInfo) => {
      const isTeamsTournament: boolean = (next) ? (next?.tournamentType === 'Teams') : false;
      console.log('isTeamsTournament', isTeamsTournament);
      this.teamsTournament$ = of(isTeamsTournament);
    });
  }

  selectTournamentConfig(tournamentId: number) {
    const selectedTournamentConfig$: Observable<TournamentEvent[]> = this.tournamentEventConfigService.getAllForTournament(tournamentId);
    this.subscription3 = selectedTournamentConfig$.subscribe((next: TournamentEvent[]) => {
      this.tournamentEvents$ = of(next || []);
    });
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    if (this.subscription2) {
      this.subscription2.unsubscribe();
    }
    if (this.subscription3) {
      this.subscription3.unsubscribe();
    }
  }
}
