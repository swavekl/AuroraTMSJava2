jest.mock('@angular/core', () => ({
  Component: () => component => component,
  ViewChild: () => () => undefined
}));
jest.mock('@angular/common', () => ({TitleCasePipe: class { transform(value: string) { return value; } }}));
jest.mock('../cross-field-error-matcher/cross-field-error-matcher', () => ({CrossFieldErrorMatcher: class {}}));

import {RegisterComponent} from './register.component';
import type {AuthenticationService} from '../authentication.service';
import type {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import type {Router} from '@angular/router';
import {of} from 'rxjs';

describe('RegisterComponent', () => {
  it('offers Start Over after two resend submissions and clears the credentials', () => {
    const authenticationService = {
      resendVerificationEmail: jest.fn()
        .mockReturnValue(of({message: 'Another email was sent. Please check again.'}))
    } as unknown as AuthenticationService;
    const progressBarService = {setLoading: jest.fn()} as unknown as LinearProgressBarService;
    const router = {navigateByUrl: jest.fn()} as unknown as Router;
    const component = new RegisterComponent(authenticationService, progressBarService, router);
    component['resendAttempts'] = 0;
    component.resendLimitReached = false;
    component.resendInProgress = false;

    component.email = 'old@example.com';
    component.password = 'old-password';
    component.password2 = 'old-password';
    component.registrationCompleted = true;
    component.verificationMessage = 'We sent a verification email.';

    component.resendVerificationEmail();
    expect(component.resendLimitReached).toBe(false);
    component.resendVerificationEmail();

    expect(component.resendLimitReached).toBe(true);
    expect(authenticationService.resendVerificationEmail).toHaveBeenCalledTimes(2);
    expect(progressBarService.setLoading).toHaveBeenNthCalledWith(1, true);
    expect(progressBarService.setLoading).toHaveBeenNthCalledWith(2, false);
    expect(progressBarService.setLoading).toHaveBeenNthCalledWith(3, true);
    expect(progressBarService.setLoading).toHaveBeenNthCalledWith(4, false);

    component.startOver();

    expect(component.email).toBe('');
    expect(component.password).toBe('');
    expect(component.password2).toBe('');
    expect(component.registrationCompleted).toBe(false);
    expect(component.resendLimitReached).toBe(false);
    expect(router.navigateByUrl).toHaveBeenCalledWith('/ui/login/register');
  });
});
