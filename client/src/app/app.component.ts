import {Component, OnInit, ViewChild} from '@angular/core';
// import {OktaAuthService} from '@okta/okta-angular';
import {Observable} from 'rxjs';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {map} from 'rxjs/operators';
import {AuthenticationService} from './user/authentication.service';
import {Router} from '@angular/router';
import {MatSidenav} from '@angular/material/sidenav';
import {ResetStore} from './store/reset-store';
import {Store} from '@ngrx/store';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  isAuthenticated$: Observable<boolean>;

  isMobile = false;

  isHandset$: Observable<boolean> = this.breakpointObserver.observe(Breakpoints.Handset)
    .pipe(
      map(result => {
        this.isMobile = result.matches;
        return result.matches;
      })
    );

  // drawer component for closing after click
  @ViewChild(MatSidenav, {static: true})
  drawer: MatSidenav;

  constructor(private breakpointObserver: BreakpointObserver,
              // private oktaAuth: OktaAuthService,
              private authenticationService: AuthenticationService,
              private router: Router,
              private store: Store) {
  }

  async ngOnInit() {
    this.isAuthenticated$ = this.authenticationService.getIsAuthenticated();
    // this.isAuthenticated = await this.oktaAuth.isAuthenticated();
    // // Subscribe to authentication state changes
    // this.oktaAuth.$authenticationState.subscribe(
    //   (isAuthenticated: boolean)  => this.isAuthenticated = isAuthenticated
    // );
  }

  logout() {
    // this.oktaAuth.logout();
    this.closeDrawerOnMobile();
    this.authenticationService.logout();
    this.store.dispatch(new ResetStore());
  }

  /**
   * on mobile devices the menu is opened and when you click on menu item it should close before navigating to this item
   */
  closeAndNavigateToRoute(routerLink) {
    this.closeDrawerOnMobile();
    this.router.navigate([routerLink]);
  }

  private closeDrawerOnMobile() {
    if (this.drawer && this.drawer.opened && this.isMobile) {
      this.drawer.close();
    }
  }
}
