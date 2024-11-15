import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'oktaUserStatus'
})
export class OktaUserStatusPipe implements PipeTransform {

  transform(value: string, ...args: unknown[]): unknown {
    switch (value) {
      case "STAGED":
        return "Staged"; // New users created through the API and not activated yet.
      case "ACTIVE":
          return "Active";
      case "SUSPENDED":
        return "Suspended"; // The user cannot access applications, including the dashboard/admin.
      case "LOCKED_OUT":
        return "Locked out"; // The user exceeds the number of login attempts defined in the login policy.
      case "DEPROVISIONED":``
        return "Deactivated";  // Deactivated in Okta.
      case "PASSWORD_EXPIRED":
        return "Password Expired";  // The user password is expired.
      case "PROVISIONED":
        return "Provisioned"; // Admin manually activates the user account. The user is in an active state but has not completed the activation process.
      case "PENDING_USER_ACTION":
        return "Pending user action"; // Admin manually activates the user account. The user is in an active state but has not completed the activation process.
      case "RECOVERY":
        return "Recovery"; // Existing user, activated previously, currently in password reset mode. User action is required.
      case "PASSWORD_RESET":
        return "Password reset"; // Existing user, activated previously, currently in password reset mode. User action is required.
      default:
        return value;
    }
  }

  static getAllStatus(): string [] {
    return [
      'STAGED',
      'ACTIVE',
      'SUSPENDED',
      'LOCKED_OUT',
      'DEPROVISIONED',
      // 'PASSWORD_EXPIRED',
      'PROVISIONED',
      // 'PENDING_USER_ACTION',
      'RECOVERY',
      // 'PASSWORD_RESET'
    ];
  }
}
