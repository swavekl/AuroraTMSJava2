import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TournamentProcessingRequest} from '../model/tournament-processing-request';
import {TournamentProcessingRequestDetail} from '../model/tournament-processing-request-detail';

@Component({
  selector: 'app-tournament-processing-detail',
  templateUrl: './tournament-processing-detail.component.html',
  styleUrls: ['./tournament-processing-detail.component.scss']
})
export class TournamentProcessingDetailComponent implements OnInit {

  @Input()
  public tournamentProcessingRequest: TournamentProcessingRequest;

  @Output('generateReports')
  public generateReportsEventEmitter: EventEmitter<any> = new EventEmitter<any>();

  constructor() { }

  ngOnInit(): void {
  }

  generateAllReports() {
    const request = JSON.parse(JSON.stringify(this.tournamentProcessingRequest));
    const details = request.details || [];
    details.push(new TournamentProcessingRequestDetail());
    request.details = details;
    this.generateReportsEventEmitter.emit(request);
  }

  onSubmitRequest() {

  }
}
