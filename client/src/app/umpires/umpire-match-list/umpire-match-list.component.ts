import {Component, Input} from '@angular/core';
import {UmpiredMatchInfo} from '../model/umpired-match-info.model';

@Component({
  selector: 'app-umpire-match-list',
  templateUrl: './umpire-match-list.component.html',
  styleUrl: './umpire-match-list.component.scss'
})
export class UmpireMatchListComponent {

  @Input()
  singleTournament: boolean = true;

  @Input()
  umpireMatchInfos: UmpiredMatchInfo[] = [];

  @Input()
  umpireName: string;
}
