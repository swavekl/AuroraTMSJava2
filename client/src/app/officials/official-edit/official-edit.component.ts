import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Official} from '../model/official.model';
import {UmpireRank} from '../model/umpire-rank.enum';
import {Ranks} from '../model/ranks';

@Component({
  selector: 'app-official-edit',
  templateUrl: './official-edit.component.html',
  styleUrls: ['./official-edit.component.scss']
})
export class OfficialEditComponent {

  @Input()
  official: Official;

  @Output()
  save: EventEmitter<Official> = new EventEmitter<Official>();

  @Output()
  cancel: EventEmitter<any> = new EventEmitter<any>();

  umpireRanks: any[] = [];
  refereeRanks: any[] = [];


  constructor() {
    this.umpireRanks = Ranks.getUmpireRanks();
    this.refereeRanks = Ranks.getRefereeRanks();
  }

  onSave(value: any) {
    this.save.emit(this.official);
  }

  onCancel() {
    this.cancel.emit(null);
  }
}
