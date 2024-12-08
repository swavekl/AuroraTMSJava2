import {Component, Input} from '@angular/core';
import {Tournament} from '../../tournament/tournament-config/tournament.model';

@Component({
  selector: 'app-referee-tournament-list',
  templateUrl: './referee-tournament-list.component.html',
  styleUrl: './referee-tournament-list.component.scss'
})
export class RefereeTournamentListComponent {

  @Input()
  tournaments!: Tournament[] | null;
}
