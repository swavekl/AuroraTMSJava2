import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {MatchCard} from '../model/match-card.model';

@Component({
  selector: 'app-matches',
  templateUrl: './matches.component.html',
  styleUrls: ['./matches.component.scss']
})
export class MatchesComponent implements OnInit, OnChanges {

  @Input()
  tournamentEvents: TournamentEvent[] = [];

  @Input()
  matchCards: MatchCard[] = [];

  @Input()
  selectedMatchCard: MatchCard;

  @Output()
  private tournamentEventEmitter: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  private matchCardEmitter: EventEmitter<any> = new EventEmitter<any>();

  // currently selected event for viewing draws
  selectedEvent: TournamentEvent;

  selectedMatchCardId: number;

  games: number [];

  constructor() {
    this.games = [];
  }

  ngOnInit(): void {
  }

  onSelectEvent(tournamentEvent: TournamentEvent) {
    // load match cards for this event
    this.selectedMatchCardId = -1;
    this.selectedMatchCard = null;
    this.selectedEvent = tournamentEvent;
    this.tournamentEventEmitter.emit(tournamentEvent.id);
  }

  isSelected(tournamentEvent: TournamentEvent) {
    return tournamentEvent.id === this.selectedEvent?.id;  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentEventsChanges: SimpleChange = changes.tournamentEvents;
    if (tournamentEventsChanges) {
      const te = tournamentEventsChanges.currentValue;
      // console.log('DrawsComponent got tournament events of length ' + te.length);
    }
  }

  onSelectMatchCard(matchCard: MatchCard) {
    this.selectedMatchCardId = matchCard.id;
    this.matchCardEmitter.emit(this.selectedMatchCardId);
    const numGames = matchCard.numberOfGames === 0 ? 5 : matchCard.numberOfGames;
    this.games = Array(numGames);
  }

  isSelectedMatchCard(matchCard: MatchCard): boolean {
    return (this.selectedMatchCardId === matchCard.id);
  }
}
