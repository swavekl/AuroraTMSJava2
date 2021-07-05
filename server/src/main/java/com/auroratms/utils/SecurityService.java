package com.auroratms.utils;

import com.auroratms.users.UserRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

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
        GrantedAuthoritySid adminsRole = new GrantedAuthoritySid(UserRoles.Admins);
        addPermission(objectId, objectClass, adminsRole, BasePermission.READ);
        addPermission(objectId, objectClass, adminsRole, BasePermission.WRITE);
        addPermission(objectId, objectClass, adminsRole, BasePermission.DELETE);
        addPermission(objectId, objectClass, adminsRole, BasePermission.ADMINISTRATION); // change owner of class
    }

    /**
     * Grant all permissions to Tournament Director
     *
     * @param objectId
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
     * @param sid
     */
    public void revokeAllPermissions(long objectId, Class objectClass, Sid sid) {
        MutableAcl acl = null;
        try {
            ObjectIdentity oid = new ObjectIdentityImpl(objectClass, objectId);
            acl = (MutableAcl) aclService.readAclById(oid);
            List<AccessControlEntry> entries = acl.getEntries();
            // traverse entries in revers order so that removal from array works correctly
            for (int i = entries.size(); i > 0; i--) {
                int index = i - 1;
                AccessControlEntry entry = entries.get(index);
                if (entry.getSid().equals(sid)) {
                    acl.deleteAce(index);
                }
            }
        } catch (NotFoundException e) {
            throw new RuntimeException("Unable to revoke permission ", e);
        }
        aclService.updateAcl(acl);
    }

    /**
     * @param objectId
     * @param objectClass
     * @return
     */
    public Acl readAclForObject(long objectId, Class objectClass) {
        ObjectIdentity oid = new ObjectIdentityImpl(objectClass, objectId);
        return this.aclService.readAclById(oid);
    }

    /**
     * Deletes all ACLs for this object
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

}
