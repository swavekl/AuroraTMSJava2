package com.auroratms.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class SecurityService {

    @Autowired
    private MutableAclService aclService;

    /**
     * Grant all permissions to Admin role
     *
     * @param objectId
     * @param objectClass
     */
    public void provideAccessToAdmin(long objectId, Class objectClass) {
        GrantedAuthoritySid adminsRole = new GrantedAuthoritySid("Admins");
        addPermission(objectId, objectClass, adminsRole, BasePermission.READ);
        addPermission(objectId, objectClass, adminsRole, BasePermission.WRITE);
        addPermission(objectId, objectClass, adminsRole, BasePermission.DELETE);
        addPermission(objectId, objectClass, adminsRole, BasePermission.ADMINISTRATION); // change owner of class
    }

    /**
     * Grant all permissions to Admin role
     *  @param objectId
     * @param ownerId
     * @param objectClass
     */
    public void provideAccessToTournamentDirector(long objectId, String ownerId, Class objectClass) {
        PrincipalSid role = new PrincipalSid(ownerId);
        addPermission(objectId, objectClass, role, BasePermission.READ);
        addPermission(objectId, objectClass, role, BasePermission.WRITE);
        addPermission(objectId, objectClass, role, BasePermission.DELETE);
        addPermission(objectId, objectClass, role, BasePermission.ADMINISTRATION); // change owner of class
    }

    /**
     * Adds permission for recipient
     *
     * @param objectId
     * @param objectClass
     * @param recipient
     * @param permission
     */
    public void addPermission(long objectId, Class objectClass, Sid recipient, Permission permission) {
        MutableAcl acl = null;
        ObjectIdentity oid = new ObjectIdentityImpl(objectClass, objectId);
        try {
            acl = (MutableAcl) aclService.readAclById(oid);
        } catch (NotFoundException e) {
            acl = aclService.createAcl(oid);
        }
        acl.insertAce(acl.getEntries().size(), permission, recipient, true);
        aclService.updateAcl(acl);
    }

    /**
     * deletes all permissions for recipient
     *
     * @param objectId
     * @param objectClass
     */
    public void deleteAcl(long objectId, Class objectClass) {
        ObjectIdentity oid = new ObjectIdentityImpl(objectClass, objectId);
        aclService.deleteAcl(oid, false);
    }

    /**
     * Gets current user name
     *
     * @return
     */
    public String getCurrentUsername() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        return authentication.getName();
    }

    /**
     * Tests if current user is an Admin authority
     *
     * @return
     */
    public boolean isAdmin() {
        boolean isAdmin = false;
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("Admins")) {
                isAdmin = true;
            }
        }
        return isAdmin;

    }

}
