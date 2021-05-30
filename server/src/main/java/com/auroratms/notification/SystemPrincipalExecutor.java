package com.auroratms.notification;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Executor which sets a 'system' user with Admin privileges for executing in another thread from the
 * one which handles REST api which has authenticated user.
 */
public abstract class SystemPrincipalExecutor {

    private SystemPrincipalAuthenticationManager authManager = new SystemPrincipalAuthenticationManager();

    protected void taskBody() {
        // to be overridden
    }

    public void execute() {
//        long start = System.currentTimeMillis();
        SecurityContext sc = SecurityContextHolder.getContext();
        Authentication previousAuthentication = sc.getAuthentication();
        try {
            UsernamePasswordAuthenticationToken authReq
                    = new UsernamePasswordAuthenticationToken("system", "system");
            Authentication auth = authManager.authenticate(authReq);
            sc.setAuthentication(auth);
            this.taskBody();
        } finally {
//            long end = System.currentTimeMillis();
//            long duration = end - start;
//            System.out.println("Asynchronous task duration " + duration + " ms");
            sc.setAuthentication(previousAuthentication);
        }
    }

    /**
     * Authentication manager which simulates authentication by System principal
     */
    private class SystemPrincipalAuthenticationManager implements AuthenticationManager {

        final List<GrantedAuthority> AUTHORITIES = new ArrayList<GrantedAuthority>();

        SystemPrincipalAuthenticationManager() {
            AUTHORITIES.add(new SimpleGrantedAuthority("Admins"));
        }

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            return new UsernamePasswordAuthenticationToken(authentication.getName(),
                    authentication.getCredentials(), AUTHORITIES);
        }
    }
}
