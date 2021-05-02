import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RegistrationConfirmedComponent } from './registration-confirmed.component';

describe('RegistrationConfirmedComponent', () => {
  let component: RegistrationConfirmedComponent;
  let fixture: ComponentFixture<RegistrationConfirmedComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationConfirmedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationConfirmedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
