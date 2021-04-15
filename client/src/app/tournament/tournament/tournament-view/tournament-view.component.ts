import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentInfo} from '../../model/tournament-info.model';
import {Router} from '@angular/router';
import {TournamentEntry} from '../../tournament-entry/model/tournament-entry.model';
import {first} from 'rxjs/operators';
import {AuthenticationService} from '../../../user/authentication.service';
import {TournamentEntryService} from '../../tournament-entry/service/tournament-entry.service';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {DateUtils} from '../../../shared/date-utils';

@Component({
  selector: 'app-tournament-view',
  templateUrl: './tournament-view.component.html',
  styleUrls: ['./tournament-view.component.scss']
})
export class TournamentViewComponent implements OnInit, OnChanges {
  @Input()
  tournamentInfo: TournamentInfo;

  @Input()
  entryId: number;

  @Input()
  tournamentEvents: TournamentEvent [];

  tournamentStartDate: Date;
  percentFull: number;
  starsArray: number [];

  constructor(private router: Router,
              private authService: AuthenticationService,
              private tournamentEntryService: TournamentEntryService) {
    this.starsArray = [];
    this.entryId = 0;
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentInfoChange: SimpleChange = changes.tournamentInfo;
    if (tournamentInfoChange != null) {
      const tournamentInfo: TournamentInfo = tournamentInfoChange.currentValue;
      if (tournamentInfo != null) {
        this.tournamentStartDate = new DateUtils().convertFromString(tournamentInfo.startDate);
        const maxNumEventEntries = (tournamentInfo.maxNumEventEntries > 0) ? tournamentInfo.maxNumEventEntries : 1;
        this.percentFull = tournamentInfo.numEventEntries / maxNumEventEntries;
        this.starsArray = Array(tournamentInfo.starLevel);
      }
    }
  }

  onEnter() {
    // create entry
    const entryToEdit = new TournamentEntry();
    entryToEdit.tournamentFk = this.tournamentInfo.id;
    entryToEdit.dateEntered = new Date();
    entryToEdit.profileId = this.authService.getCurrentUserProfileId();
    this.tournamentEntryService.add(entryToEdit)
      .pipe(first())
      .subscribe(
        (tournamentEntry: TournamentEntry) => {
          // console.log('created new tournament entry', tournamentEntry);
          this.entryId = tournamentEntry.id;
          this.onView();
        },
        (error: any) => {
          console.log('error during entry creation', error);
        }
      );
  }

  onView() {
    const url = `entries/entrywizard/${this.tournamentInfo.id}/edit/${this.entryId}`;
    this.router.navigateByUrl(url);
  }

  onWithdraw() {
    console.log('warning about withdrawal');
  }

  showPlayers() {
    const url = `playerlist/${this.tournamentInfo.id}`;
    // get the single piece of information that it needs to properly render event dates
    // to prevent going to the server for it
    const extras = {
      state: {
        tournamentStartDate: this.tournamentInfo.startDate
      }
    };
    this.router.navigateByUrl(url, extras);
  }

  getDirectionsURL(): string {
    let destination = null;
    if (this.tournamentInfo) {
      if (this.tournamentInfo.venueName !== '') {
        destination = this.tournamentInfo.venueName;
        destination += ' ' + this.tournamentInfo.streetAddress;
      } else {
        destination = this.tournamentInfo.streetAddress;
      }
      destination += ' ' + this.tournamentInfo.city;
      destination += ' ' + this.tournamentInfo.state;
      destination = encodeURIComponent(destination);
      return `https://www.google.com/maps/dir/?api=1&travelmode=driving&dir_action=navigate&destination=${destination}`;
    } else {
      return '';
    }
  }

  showDirections() {
    const url = this.getDirectionsURL();
    window.open(url);
  }
}
