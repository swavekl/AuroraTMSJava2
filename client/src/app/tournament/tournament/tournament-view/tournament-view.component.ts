import {Component, Input, OnInit} from '@angular/core';
import {TournamentInfo} from '../tournament-info.model';
import {Router} from '@angular/router';
import {TournamentEntry} from '../../tournament-entry/model/tournament-entry.model';
import {first} from 'rxjs/operators';
import {AuthenticationService} from '../../../user/authentication.service';
import {TournamentEntryService} from '../../tournament-entry/service/tournament-entry.service';

@Component({
  selector: 'app-tournament-view',
  templateUrl: './tournament-view.component.html',
  styleUrls: ['./tournament-view.component.css']
})
export class TournamentViewComponent implements OnInit {
  @Input()
  tournament: TournamentInfo;

  @Input()
  entryId: number;

  constructor(private router: Router,
              private authService: AuthenticationService,
              private tournamentEntryService: TournamentEntryService) {
  }

  ngOnInit(): void {
  }

  onEnter() {
    // create entry
    const entryToEdit = new TournamentEntry();
    entryToEdit.tournamentFk = this.tournament.id;
    entryToEdit.type = 0; // EntryType.INDIVIDUAL
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
    const url = `entries/entrywizard/${this.tournament.id}/edit/${this.entryId}`;
    this.router.navigateByUrl(url);
  }
}
