import {AfterViewInit, Component, OnInit} from '@angular/core';
import {AuthenticationService} from '../authentication.service';
import {Store} from '@ngrx/store';
import {ResetStore} from '../../store/reset-store';

@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.css']
})
export class LogoutComponent implements OnInit, AfterViewInit {

  constructor(private authenticationService: AuthenticationService,
              private store: Store) {
  }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    // execute logout with delay so we avoid
    // 'Expression has changed after it was checked' error caused by isAuthenticated$ in app-component.html
    // which is used to show/hide Login/Logout menu items.
    const me = this;
    setTimeout(function() {
      me.doLogoutStuff();
    }, 500);
  }

  private doLogoutStuff(): void {
    this.store.dispatch(new ResetStore());
    this.authenticationService.logout();
  }

}
