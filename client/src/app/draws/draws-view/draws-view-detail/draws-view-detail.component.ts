import {Component, Input} from '@angular/core';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {DrawItem} from '../../draws-common/model/draw-item.model';

@Component({
  selector: 'app-draws-view-detail',
  templateUrl: './draws-view-detail.component.html',
  styleUrls: ['./draws-view-detail.component.scss']
})
export class DrawsViewDetailComponent {

  @Input()
  selectedEvent: TournamentEvent;

  @Input()
  draws: DrawItem [] = [];

  constructor() {
  }

  onRRDrawsAction($event: any) {
    // do nothing
  }
}
