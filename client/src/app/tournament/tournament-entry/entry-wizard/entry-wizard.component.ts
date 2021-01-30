import {ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TournamentEntryModule} from '../tournament-entry.module';
import {TournamentEntry} from '../model/tournament-entry.model';
import {PlayerFindPopupComponent} from '../../../profile/player-find-popup/player-find-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {BehaviorSubject, Subject} from 'rxjs';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';

@Component({
  selector: 'app-entry-wizard',
  templateUrl: './entry-wizard.component.html',
  styleUrls: ['./entry-wizard.component.css']
})
export class EntryWizardComponent implements OnInit {

  @Input()
  entry: TournamentEntry;

  @Input()
  teamsTournament: boolean;

  @Input()
  otherPlayers: any[];

  otherPlayersBS$: BehaviorSubject<any[]>;

  @Input()
  allEvents: TournamentEvent[];

  enteredEvents: TournamentEvent[] = [];
  availableEvents: TournamentEvent[] = [];
  unavailableEvents: TournamentEvent[] = [];

  @Output()
  emitter: EventEmitter<TournamentEntry>;

  columnsToDisplay: string[] = ['name', 'action'];

  constructor(private dialog: MatDialog,
              private _change: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    this.otherPlayersBS$ = new BehaviorSubject(this.otherPlayers);
    this.availableEvents = this.allEvents;
  }

  onSave(formValues: any) {
    console.log('formValues', formValues);
  }

  editEntry(profileId: number) {

  }

  addMember() {
    const config = {
      width: '250px', height: '550px', data: {}
    };
    const me = this;
    const dialogRef = this.dialog.open(PlayerFindPopupComponent, config);
    dialogRef.afterClosed().subscribe(next => {
      if (next !== null && next !== 'cancel') {
        console.log('got ok player', next);
          const currentValue = me.otherPlayersBS$.getValue();
          currentValue.push(next);
          this.otherPlayersBS$.next(currentValue);
      }
    });
  }

  typeChanged(event) {
    console.log ('event', event);
    console.log ('this.entry.type', this.entry.type);
    this.entry.type = event.value;
    this._change.markForCheck();
  }
}
