import {Component, OnInit, ViewChild} from '@angular/core';
import {Observable} from 'rxjs';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {map} from 'rxjs/operators';
import {AuthenticationService} from './user/authentication.service';
import {Route, Router} from '@angular/router';
import {MatSidenav} from '@angular/material/sidenav';
import {TodayService} from './shared/today.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  public isAuthenticated$: Observable<boolean>;

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
              private authenticationService: AuthenticationService,
              private todayService: TodayService,
              private router: Router) {
  }

  ngOnInit() {
    this.isAuthenticated$ = this.authenticationService.getIsAuthenticated();
  }

  getUserFirstName() {
    return this.authenticationService.getCurrentUserFirstName();
  }

  logout() {
    this.closeDrawerOnMobile();
    this.router.navigate(['/logout']);
  }

  editProfile() {
    const profileId: string = this.authenticationService.getCurrentUserProfileId();
    this.closeAndNavigateToRoute(`/userprofile/edit/${profileId}`);
  }

  hasTournamentToday(): boolean {
    return this.todayService.hasTournamentToday;
  }

  goToToday() {
    const url = this.todayService.todayUrl;
    if (url) {
      this.closeAndNavigateToRoute(url);
    }
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

  public isLinkVisible(routerLink): boolean {
    let isVisible = true;
    routerLink = routerLink.startsWith('/') ? routerLink.substr(1, routerLink.length) : routerLink;
    // console.log ('routerLink', routerLink);
    const routesConfigurations: Route[] = this.router.config;
    for (let i = 0; i < routesConfigurations.length; i++) {
      const route: Route = routesConfigurations[i];
      // console.log('path', route.path);
      if (route.path === routerLink) {
        // console.log ('route found');
        const routeData = route.data;
        if (routeData != null && routeData.roles != null) {
          const roles: string [] = routeData.roles;
          isVisible = this.authenticationService.hasCurrentUserRole(roles);
          break;
        }
      }
    }
    // console.log (routerLink + ' isVisible ' + isVisible);
    return isVisible;
  }
}
