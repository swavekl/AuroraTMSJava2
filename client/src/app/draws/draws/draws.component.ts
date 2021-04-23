import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';

@Component({
  selector: 'app-draws',
  templateUrl: './draws.component.html',
  styleUrls: ['./draws.component.scss']
})
export class DrawsComponent implements OnInit, OnChanges {

  @Input()
  tournamentEvents: TournamentEvent [] = [];
  selectedEvent: TournamentEvent;

  groups: number [];

  constructor() {
    this.groups = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18 , 19, 20, 21, 22, 23, 24, 25];
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentEventsChanges: SimpleChange = changes.tournamentEvents;
    if (tournamentEventsChanges) {
      const te = tournamentEventsChanges.currentValue;
      console.log('DrawsComponent got tournament events of length ' + te.length);
    }
  }

  onSelectEvent(tournamentEvent: TournamentEvent) {
    this.selectedEvent = tournamentEvent;
    console.log ('selected event id '  + this.selectedEvent.id  + ' name ' + this.selectedEvent.name);
  }

  clearDraw() {
    console.log ('clearing draw for event ' + this.selectedEvent.id);
  }

  generateDraw() {
    console.log('generating draw for event ' + this.selectedEvent.id);
  }
}
