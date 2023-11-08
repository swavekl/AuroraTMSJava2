import {Component, OnDestroy, OnInit} from '@angular/core';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {TournamentEntryInfoService} from '../../service/tournament-entry-info.service';
import {ActivatedRoute, Router} from '@angular/router';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {first} from 'rxjs/operators';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';
import {createSelector} from '@ngrx/store';
import {TournamentInfo} from '../../model/tournament-info.model';
import {TournamentInfoService} from '../../service/tournament-info.service';
import {UsattRecordSearchCallbackData, UsattRecordSearchPopupService} from '../../../profile/service/usatt-record-search-popup.service';
import {RecordSearchData} from '../../../profile/usatt-record-search-popup/usatt-record-search-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';

@Component({
  selector: 'app-tournament-players-list-big-container',
  template: `
    <app-tournament-players-list-big [entryInfos]="entryInfos$ | async"
    [tournamentName]="tournamentName$ | async"
    [tournamentId]="tournamentId"
    [tournamentReady]="tournamentReady$ | async"
    (viewEntry)="onViewEntry($event)"
    (addEntry)="onAddEntry($event)"
    (findPlayer)="onFindPlayer($event)">
    </app-tournament-players-list-big>
  `,
  styles: [
  ]
})
export class TournamentPlayersListBigContainerComponent implements OnInit, OnDestroy {

  private subscriptions: Subscription = new Subscription();

  entryInfos$: Observable<TournamentEntryInfo[]>;

  loading$: Observable<boolean>;
  tournamentName$: Observable<string>;
  tournamentReady$: Observable<boolean>;
  tournamentId: number;

  constructor(private tournamentEntryInfoService: TournamentEntryInfoService,
              private tournamentInfoService: TournamentInfoService,
              private router: Router,
              private activatedRoute: ActivatedRoute,
              private playerFindPopupService: UsattRecordSearchPopupService,
              private dialog: MatDialog,
              private linearProgressBarService: LinearProgressBarService) {
    this.setupProgressIndicator();
  }

  ngOnInit(): void {
    const strTournamentId = this.activatedRoute.snapshot.params['id'];
    this.tournamentId = (strTournamentId != null) ? Number(strTournamentId) : 0;
    this.loadTournamentName(this.tournamentId);
    this.loadTournamentEntries(this.tournamentId);
  }

  ngOnDestroy(): void {
        this.subscriptions.unsubscribe();
    }

  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.tournamentEntryInfoService.loading$,
      this.tournamentInfoService.loading$,
      (entryInfosLoading: boolean, tournamentInfoLoading: boolean) => {
        return entryInfosLoading || tournamentInfoLoading;
      }
    );

    const loadingSubscription = this.loading$
      .subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });

    this.subscriptions.add(loadingSubscription);
  }

  private loadTournamentEntries(tournamentId: number) {
    const subscription = this.tournamentEntryInfoService.getAll(tournamentId)
      .pipe(
        first())
      .subscribe(
        (infos: TournamentEntryInfo[]) => {
          // console.log('returning infos.length ' + infos.length);
          this.entryInfos$ = of(infos);
        },
        (error: any) => {
          console.log('error loading entry infos' + JSON.stringify(error));
        }
      );
    this.subscriptions.add(subscription);
  }

  private loadTournamentName(tournamentId: number) {
    // tournament view may have passed us the tournament start date
    // but if user navigated to this screen by url then go to the server and get it.
    const tournamentName = history?.state?.tournamentName;
    const tournamentReady = history?.state?.tournamentReady;
    if (tournamentName != null) {
      // console.log('Tournament name PASSED from previous screen', tournamentName);
      this.tournamentName$ = of(tournamentName);
      this.tournamentReady$ = of(tournamentReady);
    } else {
      // create a selector for fast lookup in cache
      // console.log('Tournament name NOT PASSED from previous screen');
      const tournamentInfoSelector = this.tournamentInfoService.selectors.selectEntityMap;
      const selectedTournamentSelector = createSelector(
        tournamentInfoSelector,
        (entityMap) => {
          return entityMap[tournamentId];
        });

      const subscription = this.tournamentInfoService.store.select(selectedTournamentSelector)
        .subscribe(
          (tournamentInfo: TournamentInfo) => {
            if (tournamentInfo) {
              // console.log('got tournamentInfo from cache for tournament name');
              this.tournamentName$ = of(tournamentInfo.name);
              this.tournamentReady$ = of(tournamentInfo.ready);
            } else {
              // console.log('tournamentInfo not in cache. getting from SERVER');
              // not in cache so get it. Since it is an entity collection it will be
              // piped to the above selector and processed by if branch
              this.tournamentInfoService.getByKey(tournamentId);
            }
          });
      this.subscriptions.add(subscription);
    }
  }

  onViewEntry(tournamentEntryInfo: TournamentEntryInfo) {
    const url = `ui/entries/entryview/${this.tournamentId}/edit/${tournamentEntryInfo.entryId}`;
    const extras = {
      state: {
        returnUrl: window.location.pathname,
        canChangeRating: true
      }
    };
    this.router.navigateByUrl(url, extras);
  }

  onAddEntry($event: any) {
    const extras = {
      state: {
        returnUrl: `/ui/tournaments/playerlistbig/${this.tournamentId}`,
        forwardUrl: `/ui/entries/entryadd/${this.tournamentId}`
      }
    };
    this.router.navigate(['/ui/userprofile/addbytd', this.tournamentId], extras);
  }

  onFindPlayer($event: any) {
    const data: RecordSearchData = {
      firstName: null,
      lastName: null,
      searchingByMembershipId: null
    };
    const callbackParams: UsattRecordSearchCallbackData = {
      successCallbackFn: this.findPlayerSuccessCallback,
      cancelCallbackFn: null,
      callbackScope: this
    };
    this.playerFindPopupService.showPopup(data, callbackParams);
  }

  findPlayerSuccessCallback (scope: any, result: any) {
    const me = scope;
    let playerInfo = JSON.stringify(result, null, 2);
    const config = {
      width: '500px', height: '350px', data: {
        message: playerInfo, showCancel: false, okText: 'Close', contentAreaHeight: '200px', title: 'Player USATT Record'
      }
    };
    const dialogRef = me.dialog.open(ConfirmationPopupComponent, config);
    const subscription = dialogRef.afterClosed().subscribe(result => {
    });
    me.subscriptions.add(subscription);
  }
}

