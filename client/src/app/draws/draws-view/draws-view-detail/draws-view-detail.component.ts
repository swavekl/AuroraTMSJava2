import {Component, Input} from '@angular/core';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {DrawItem} from '../../draws-common/model/draw-item.model';
import {Router} from '@angular/router';
import {MatchCardInfo} from '../../../matches/model/match-card-info.model';

@Component({
    selector: 'app-draws-view-detail',
    templateUrl: './draws-view-detail.component.html',
    styleUrls: ['./draws-view-detail.component.scss'],
    standalone: false
})
export class DrawsViewDetailComponent {

  @Input()
  selectedEvent: TournamentEvent;

  @Input()
  draws: DrawItem [] = [];

  @Input()
  matchCardInfos: MatchCardInfo[];

  constructor(private router: Router) {
  }

  onRRDrawsAction($event: any) {
    // do nothing
  }

  back() {
    this.router.navigateByUrl(`/ui/drawsview/${this.selectedEvent.tournamentFk}`);
  }
}
