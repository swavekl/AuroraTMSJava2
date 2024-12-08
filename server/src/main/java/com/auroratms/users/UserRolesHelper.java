package com.auroratms.users;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

public class UserRolesHelper {
    /**
     * Tests if current user is an Admin authority
     *
     * @return
     */
    public static boolean isAdmin() {
        return UserRolesHelper.hasRole(UserRoles.Admins);
    }

    public static boolean isDataEntryClerk() {
        return UserRolesHelper.hasRole(UserRoles.DataEntryClerks);
    }

    public static boolean isTournamentDirector() {
        return UserRolesHelper.hasRole(UserRoles.TournamentDirectors);
    }

    public static boolean isTournamentReferee() {
        return UserRolesHelper.hasRole(UserRoles.Referees);
    }

    public static boolean isTournamentUmpire() {
        return UserRolesHelper.hasRole(UserRoles.Umpires);
    }

    public static boolean isUSATTTournamentManager() {
        return UserRolesHelper.hasRole(UserRoles.USATTTournamentManagers);
    }

    public static boolean isUSATTClubManager() {
        return UserRolesHelper.hasRole(UserRoles.USATTClubManagers);
    }

    public static boolean isUSATTInsuranceManager() {
        return UserRolesHelper.hasRole(UserRoles.USATTInsuranceManagers);
    }

    public static boolean isUSATTSanctionCoordinator() {
        return UserRolesHelper.hasRole(UserRoles.USATTSanctionCoordinators);
    }

    public static boolean isUSATTPlayerManager() {
        return UserRolesHelper.hasRole(UserRoles.USATTPlayerManagers);
    }

    public static boolean isUSATTMatchOfficialsManager() {
        return UserRolesHelper.hasRole(UserRoles.USATTMatchOfficialsManagers);
    }

    /**
     * Tests if current user is a specified authority (role)
     *
     * @return
     */
    public static boolean hasRole(String role) {
        boolean isAdmin = false;
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(role)) {
                isAdmin = true;
            }
        }
        return isAdmin;

    }

    /**
     * Gets current user name
     *
     * @return
     */
    public static String getCurrentUsername() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        return authentication.getName();
    }
}
