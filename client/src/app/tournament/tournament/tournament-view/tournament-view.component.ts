import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentInfo} from '../tournament-info.model';
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
  styleUrls: ['./tournament-view.component.css']
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
    console.log ('warning about withdrawal');
  }
}
