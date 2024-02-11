import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Registration} from '../model/registration.model';
import {TodayService} from '../../shared/today.service';
import {DateUtils} from '../../shared/date-utils';

@Component({
  selector: 'app-registration-list',
  templateUrl: './registration-list.component.html',
  styleUrls: ['./registration-list.component.scss']
})
export class RegistrationListComponent implements OnChanges {

  @Input()
  registrations: Registration [];

  @Output()
  eventEmitter: EventEmitter<Registration> = new EventEmitter<Registration>();

  pastIndex: number;

  upcomingRegistrations: Registration [];
  pastRegistrations: Registration [];

  constructor(private todayService: TodayService) {
    this.pastIndex = -1;
  }

  ngOnChanges(changes: SimpleChanges): void {
    const registrationChanges = changes.registrations;
    if (registrationChanges) {
      const registrations: Registration[] = registrationChanges.currentValue;
      if (registrations != null) {
        const dateUtils = new DateUtils();
        const today = this.todayService.todaysDate;
        this.pastRegistrations = registrations.filter((registration: Registration) => dateUtils.isDateBefore(registration.startDate, today));
        this.upcomingRegistrations = registrations.filter((registration: Registration) => !dateUtils.isDateBefore(registration.startDate, today));
      }
    }
  }

  onView(registration: Registration) {
    this.eventEmitter.emit(registration);
  }
}
