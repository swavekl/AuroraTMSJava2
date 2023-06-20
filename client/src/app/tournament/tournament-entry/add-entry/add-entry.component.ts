import {Component, OnDestroy} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {TournamentEntry} from '../model/tournament-entry.model';
import {MembershipUtil} from '../../util/membership-util';
import {first} from 'rxjs/operators';
import {ProfileService} from '../../../profile/profile.service';
import {Profile} from '../../../profile/profile';
import {TournamentEntryService} from '../service/tournament-entry.service';
import {TournamentConfigService} from '../../tournament-config/tournament-config.service';
import {createSelector} from '@ngrx/store';
import {Tournament} from '../../tournament-config/tournament.model';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {ErrorMessagePopupService} from '../../../shared/error-message-dialog/error-message-popup.service';

/**
 * Creates an entry by the tournament director
 */
@Component({
  selector: 'app-add-entry',
  template: ``,
  styles: []
})
export class AddEntryComponent implements OnDestroy {

  public tournamentId: number;
  public profileId: string;

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();
  private tournament: Tournament;
  private profile: Profile;

  constructor(private router: Router,
              private activatedRoute: ActivatedRoute,
              private profileService: ProfileService,
              private tournamentConfigService: TournamentConfigService,
              private tournamentEntryService: TournamentEntryService,
              private linearProgressBarService: LinearProgressBarService,
              private errorMessagePopupService: ErrorMessagePopupService) {
    this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.profileId = this.activatedRoute.snapshot.params['profileId'] || 0;
    this.setupProgressIndicator();
    this.loadTournament();
    this.loadProfile();
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest([
        this.tournamentConfigService.store.select(this.tournamentConfigService.selectors.selectLoading),
        this.profileService.loading$,
        this.tournamentEntryService.store.select(this.tournamentEntryService.selectors.selectLoading)
      ],
      (tournamentLoading: boolean, profileLoading: boolean, entryLoading: boolean) => {
        return tournamentLoading || profileLoading || entryLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });

    this.subscriptions.add(loadingSubscription);
  }

  private loadProfile() {
    const subscription = this.profileService.getProfile(this.profileId)
      .pipe(first())
      .subscribe(
        (profile: Profile) => {
          // console.log('loaded profile', profile);
          this.profile = profile;
          this.createEntry();
        },
        (error) => {
          this.errorMessagePopupService.showError(error.message);
        }
      );
    this.subscriptions.add(subscription);
  }

  private loadTournament() {
    const selectedTournamentSelector = createSelector(
      this.tournamentConfigService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[this.tournamentId];
      });
    const localTournament$ = this.tournamentConfigService.store.select(selectedTournamentSelector)
    const subscription = localTournament$
      .subscribe((tournament: Tournament) => {
        if (tournament == null) {
          // console.log('tournament not in cache, loading from server');
          this.tournamentConfigService.getByKey(this.tournamentId).subscribe();
        } else {
          // console.log('tournament in cache ', tournament);
          this.tournament = tournament;
        }
      }, (error) => {
        this.errorMessagePopupService.showError(`Unable to load tournament with id ${this.tournamentId}. Error: ${error.message}`);
      });
    this.subscriptions.add(subscription);
  }

  private createEntry() {
    if (this.profile != null && this.tournament != null) {
      const entryToEdit = new TournamentEntry();
      entryToEdit.tournamentFk = this.tournamentId;
      entryToEdit.dateEntered = new Date();
      entryToEdit.profileId = this.profileId;
      const membershipExpirationDate: Date = this.profile.membershipExpirationDate;
      const dateOfBirth = this.profile.dateOfBirth;
      const tournamentStartDate = this.tournament.startDate;
      entryToEdit.membershipOption = new MembershipUtil().getInitialMembershipOption(
        dateOfBirth, membershipExpirationDate, tournamentStartDate, this.tournament.starLevel);
      this.tournamentEntryService.add(entryToEdit, null)
        .pipe(first())
        .subscribe(
          (tournamentEntry: TournamentEntry) => {
            // console.log('created new tournament entry', tournamentEntry);
            const url = `ui/entries/entrywizard/${this.tournamentId}/edit/${tournamentEntry.id}?add=true`;
            this.router.navigateByUrl(url);
          },
          (error: any) => {
            this.errorMessagePopupService.showError(`Unable to create tournament entry for user ${this.profile.firstName} ${this.profile.lastName}. Error: ${error.message}`);
          }
        );
    }
  }
}

