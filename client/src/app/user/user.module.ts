import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from './login/login.component';
import {SignInComponent} from './sign-in/sign-in.component';
import {RegisterComponent} from './register/register.component';
import {FlexLayoutModule} from '@angular/flex-layout';
import { RegistrationConfirmedComponent } from './registration-confirmed/registration-confirmed.component';
import {AuthenticationService} from './authentication.service';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { ResetPasswordResultComponent } from './reset-password-result/reset-password-result.component';
import { ResetPasswordStartComponent } from './reset-password-start/reset-password-start.component';

const userRoutes: Routes = [
  {
    path: 'login', component: LoginComponent,
    children: [
      {path: '', redirectTo: 'signin', pathMatch: 'full'},
      {path: 'signin', component: SignInComponent},
      {path: 'register', component: RegisterComponent}
    ],
  },
  {
    path: 'registrationconfirmed', component: RegistrationConfirmedComponent
  },
  {
    path: 'resetpasswordstart', component: ResetPasswordStartComponent
  },
  {
    path: 'resetpassword/:resetPasswordToken', component: ResetPasswordComponent
  },
  {
    path: 'resetpasswordresult/:succeeded', component: ResetPasswordResultComponent
  }
];

@NgModule({
  declarations: [
    LoginComponent,
    SignInComponent,
    RegisterComponent,
    RegistrationConfirmedComponent,
    ResetPasswordStartComponent,
    ResetPasswordComponent,
    ResetPasswordResultComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(userRoutes),
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatInputModule,
    MatIconModule,
    FlexLayoutModule
  ]
})
export class UserModule {
}
