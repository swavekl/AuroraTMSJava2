import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TournamentEventConfigComponent } from './tournament-event-config.component';

describe('TournamentEventConfigComponent', () => {
  let component: TournamentEventConfigComponent;
  let fixture: ComponentFixture<TournamentEventConfigComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TournamentEventConfigComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TournamentEventConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
