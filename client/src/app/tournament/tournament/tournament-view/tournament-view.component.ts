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
import {UserRoles} from '../../../user/user-roles.enum';
import {MembershipUtil} from '../../util/membership-util';

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

  constructor(private router: Router,
              private authService: AuthenticationService,
              private tournamentEntryService: TournamentEntryService,
              private todayService: TodayService) {
    this.entryId = 0;
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

  onEnter() {
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
    const url = `ui/entries/entryview/${this.tournament.id}/edit/${this.entryId}`;
    const extras = {
      state: {
        returnUrl: window.location.pathname
      }
    };
    this.router.navigateByUrl(url, extras);
  }

  onWithdraw() {
    console.log('warning about withdrawal');
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

  showDirections() {
    const url = this.getDirectionsURL();
    window.open(url);
  }

  resultsAvailable() {
    const startDate = this.tournament.startDate;
    const endDate = this.tournament.endDate || startDate;
    const today = this.todayService.todaysDate;
    return !(new DateUtils().isDateBefore(today, endDate));
  }

  canWithdraw() {
    const isBeforeEntryCutoffDate = this.isBeforeEntryCutoffDate();
    const hasEntry = this.entryId !== 0;
    return hasEntry && isBeforeEntryCutoffDate;
  }

  private isBeforeEntryCutoffDate() {
    const entryCutoffDate = this.tournament.configuration.entryCutoffDate;
    const today = this.todayService.todaysDate;
    return (new DateUtils().isDateBefore(today, entryCutoffDate));
  }

  canEnter() {
    const isBeforeEntryCutoffDate = this.isBeforeEntryCutoffDate();
    const noEntry = this.entryId === 0;
    return noEntry && isBeforeEntryCutoffDate;
  }

  canEnterPlayer() {
    // only if you are a tournament director for this tournament can you enter players
    const profileId = this.authService.getCurrentUserProfileId();
    const isAllowed = this.authService.hasCurrentUserRole(
      [UserRoles.ROLE_ADMINS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]);
    if (isAllowed) {
      // get list of tournaments owned by this tournament director

      // is this tournament owned by it.
    }
    return true;
  }

  canView () {
    const isBeforeEntryCutoffDate = this.isBeforeEntryCutoffDate();
    const hasEntry = this.entryId !== 0;
    return hasEntry && isBeforeEntryCutoffDate;
  }

}
