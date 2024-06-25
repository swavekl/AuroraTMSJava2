import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {Router} from '@angular/router';
import {TournamentEntry} from '../../tournament-entry/model/tournament-entry.model';
import {first} from 'rxjs/operators';
import {AuthenticationService} from '../../../user/authentication.service';
import {TournamentEntryService} from '../../tournament-entry/service/tournament-entry.service';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {DateUtils} from '../../../shared/date-utils';
import {Tournament} from '../../tournament-config/tournament.model';
import {NavigateUtil} from '../../../shared/navigate-util';
import {TodayService} from '../../../shared/today.service';
import {MembershipUtil} from '../../util/membership-util';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {EligibilityRestriction} from '../../tournament-config/model/tournament-type.enum';
import {Regions} from '../../../shared/regions';
import {StatesList} from '../../../shared/states/states-list';

@Component({
  selector: 'app-tournament-view',
  templateUrl: './tournament-view.component.html',
  styleUrls: ['./tournament-view.component.scss']
})
export class TournamentViewComponent implements OnInit, OnChanges {
  @Input()
  tournament: Tournament;

  @Input()
  entryId: number;

  @Input()
  tournamentEvents: TournamentEvent [];

  tournamentStartDate: Date;
  percentFull: number;

  // flag for disabling Enter or View Entry button when on a slow network
  // users hit it more than once since the new page doesn't show up quickly
  enteringOrViewing: boolean;

  constructor(private router: Router,
              private authService: AuthenticationService,
              private tournamentEntryService: TournamentEntryService,
              private todayService: TodayService,
              private dialog: MatDialog) {
    this.entryId = 0;
    this.enteringOrViewing = false;
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentChange: SimpleChange = changes.tournament;
    if (tournamentChange != null) {
      const tournament: Tournament = tournamentChange.currentValue;
      if (tournament != null) {
        this.tournamentStartDate = new DateUtils().convertFromString(tournament.startDate);
        const maxNumEventEntries = (tournament.maxNumEventEntries > 0) ? tournament.maxNumEventEntries : 1;
        this.percentFull = tournament.numEventEntries / maxNumEventEntries;
      }
    }
  }

  canEnterClosedTournament(): boolean {
    let canEnter = true;
    if (this.tournament.configuration?.eligibilityRestriction !== null) {
      const currentUserState = this.authService.getCurrentUserState();
      switch (this.tournament.configuration?.eligibilityRestriction) {
        case EligibilityRestriction.CLOSED_STATE:
          canEnter = (currentUserState === this.tournament.state);
          break;

        case EligibilityRestriction.CLOSED_REGIONAL:
          const currentUserRegion = new Regions().lookupRegion(currentUserState);
          const tournamentRegion = new Regions().lookupRegion(this.tournament.state);
          canEnter = (currentUserRegion === tournamentRegion);
          break;

        case EligibilityRestriction.CLOSED_NATIONAL:
          const currentUserCountry = this.authService.getCurrentUserCountry();
          canEnter = StatesList.isStateInCountry(this.tournament.state, currentUserCountry);
          break;

        default:
          break;
      }
    }
    return canEnter;
  }

  showClosedTournamentWarning(): void {
    let message = '';
    switch (this.tournament.configuration?.eligibilityRestriction) {
      case EligibilityRestriction.CLOSED_STATE:
        message = `This tournament is open only to players from the state of ${this.tournament?.state}`;
        break;

      case EligibilityRestriction.CLOSED_REGIONAL:
        const tournamentRegion = new Regions().lookupRegion(this.tournament.state);
        message = `This tournament is open only to players from the ${tournamentRegion} region.`;
        break;

      case EligibilityRestriction.CLOSED_NATIONAL:
        // todo - get country
        const tournamentCountry = 'United States';
        message = `This tournament is open only to players from the ${tournamentCountry}.`;
        break;

      default:
        message = `This tournament is restricted by ${this.tournament?.configuration?.eligibilityRestriction} restriction`;
        break;
    }

    const config = {
      width: '450px', height: '230px', data: {
        message: message,
        showOk: true, showCancel: false, okText: 'Close'
      }
    };
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
    dialogRef.afterClosed().pipe(first()).subscribe(result => {
    });
  }

  onEnter() {
    if (!this.canEnterClosedTournament()) {
      this.showClosedTournamentWarning();
      return;
    }
    // prevent double entering on slow network - disables Enter button
    this.enteringOrViewing = true;
    // create entry
    const entryToEdit = new TournamentEntry();
    entryToEdit.tournamentFk = this.tournament.id;
    entryToEdit.dateEntered = new Date();
    entryToEdit.profileId = this.authService.getCurrentUserProfileId();
    const membershipExpirationDate: Date = this.authService.getCurrentUserMembershipExpiration();
    const dateOfBirth = this.authService.getCurrentUserBirthDate();
    entryToEdit.membershipOption = new MembershipUtil().getInitialMembershipOption(
      dateOfBirth, membershipExpirationDate, this.tournamentStartDate, this.tournament.starLevel);
    this.tournamentEntryService.add(entryToEdit, null)
      .pipe(first())
      .subscribe(
        (tournamentEntry: TournamentEntry) => {
          // console.log('created new tournament entry', tournamentEntry);
          this.entryId = tournamentEntry.id;
          const url = `ui/entries/entrywizard/${this.tournament.id}/edit/${this.entryId}?enter=true`;
          this.router.navigateByUrl(url);
        },
        (error: any) => {
          console.log('error during entry creation', error);
        }
      );
  }

  onView() {
    // prevent double viewing on slow network - disables View Entry button
    this.enteringOrViewing = true;
    const url = `ui/entries/entryview/${this.tournament.id}/edit/${this.entryId}`;
    const extras = {
      state: {
        returnUrl: window.location.pathname,
        canChangeRating: false
      }
    };
    this.router.navigateByUrl(url, extras);
  }

  showPlayers() {
    const url = `ui/tournaments/playerlist/${this.tournament.id}`;
    // get the single piece of information that it needs to properly render event dates
    // to prevent going to the server for it
    const extras = {
      state: {
        tournamentStartDate: this.tournament.startDate
      }
    };
    this.router.navigateByUrl(url, extras);
  }

  getDirectionsURL(): string {
    if (this.tournament) {
      return NavigateUtil.getNavigationURL(this.tournament.streetAddress, this.tournament.city,
        this.tournament.state, this.tournament.venueName);
    } else {
      return '';
    }
  }

  resultsAvailable() {
    const startDate = this.tournament.startDate;
    const endDate = this.tournament.endDate || startDate;
    const today = this.todayService.todaysDate;
    return !(new DateUtils().isDateBefore(today, endDate));
  }

  private isBeforeEntryCutoffDate() {
    const entryCutoffDate = this.tournament.configuration.entryCutoffDate;
    const today = this.todayService.todaysDate;
    return (new DateUtils().isDateBefore(today, entryCutoffDate));
  }

  canEnter() {
    const isBeforeEntryCutoffDate = this.isBeforeEntryCutoffDate();
    const noEntry = this.entryId === 0;
    return noEntry && isBeforeEntryCutoffDate && !this.enteringOrViewing;
  }

  canView () {
    const isBeforeEntryCutoffDate = this.isBeforeEntryCutoffDate();
    const hasEntry = this.entryId > 0;
    return hasEntry && isBeforeEntryCutoffDate && !this.enteringOrViewing;
  }

  getEventAvailabilityClass(tournamentEvent: TournamentEvent): string {
    const remainingSpots = tournamentEvent.maxEntries - tournamentEvent.numEntries;
    if (remainingSpots > 4) {
      return 'many-available';
    } else if (remainingSpots > 0 && remainingSpots <= 4) {
      return 'some-available';
    } else {
      return 'not-available';
    }
  }

  protected readonly Math = Math;
}
