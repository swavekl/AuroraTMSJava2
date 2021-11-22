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
        return UserRolesHelper.hasRole(UserRoles.Umpires);
    }

    public static boolean isTournamentUmpire() {
        return UserRolesHelper.hasRole(UserRoles.Umpires);
    }

    public static boolean isUSATTOfficial() {
        return UserRolesHelper.hasRole(UserRoles.USATTOfficials);
    }

    /**
     * Tests if current user is an specified authority (role)
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
