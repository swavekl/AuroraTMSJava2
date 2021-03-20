package com.auroratms.tournament;

import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.event.TournamentEventEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tournament service which can perform caching
 */
@Service
@CacheConfig(cacheNames = {"tournaments"})
@Transactional
public class TournamentService {

    @Autowired
    private TournamentRepository repository;

    @Autowired
    private MutableAclService aclService;

    /**
     * Lists all tournaments
     *
     * @return
     */
    public Collection<Tournament> list() {
        Collection<TournamentEntity> tournamentEntities = repository.findAll().stream()
                .collect(Collectors.toList());
        return toTournamentCollection(tournamentEntities);
    }

    private Collection<Tournament> toTournamentCollection(Collection<TournamentEntity> tournamentEntities) {
        Collection<Tournament> tournaments = new ArrayList<>();
        for (TournamentEntity tournamentEntity : tournamentEntities) {
            tournaments.add(new Tournament().convertFromEntity(tournamentEntity));
        }
        return tournaments;
    }

    /**
     * List tournaments owned by the current user (e.g. tournament director) or admin
     *
     * @return
     */
    public Collection<Tournament> listOwned(int page, int size) {
        String currentUser = getCurrentUsername();
        String authority = isAdmin() ? "Admins" : "Everyone";
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.ASC, "name");
        Page<TournamentEntity> ownedTournaments = repository.findWriteable(currentUser, authority, BasePermission.WRITE.getMask(), pageRequest);
        List<TournamentEntity> tournamentEntities = ownedTournaments.get().collect(Collectors.toList());
        return toTournamentCollection(tournamentEntities);
    }

    /**
     * Gets one tournament
     *
     * @param id
     * @return
     */
    @Cacheable(key = "#id")
    public Tournament getByKey(Long id) {
        TournamentEntity tournamentEntity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TournamentEntity with id " + id + " not found"));
        return new Tournament().convertFromEntity(tournamentEntity);
    }

    /**
     * Save the tournament
     *
     * @param tournament
     */
    @CachePut(key = "#result.id")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public Tournament saveTournament(Tournament tournament) {
        boolean isCreating = (tournament.getId() == null);
        TournamentEntity tournamentEntity = tournament.convertToEntity();
        TournamentEntity savedTournamentEntity = repository.save(tournamentEntity);
        if (isCreating) {
            provideAccessToAdmin(savedTournamentEntity.getId(), TournamentEntity.class);
        }
        return new Tournament().convertFromEntity(savedTournamentEntity);
    }

    /**
     * Deletes tournament
     *
     * @param tournamentId
     */
    @CacheEvict(key = "#tournamentId")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public void deleteTournament(long tournamentId) {
        repository.deleteById(tournamentId);
        deleteAcl(tournamentId, Tournament.class);
    }

    /**
     * Grand all permissions to Admin role
     *
     * @param objectId
     * @param objectClass
     */
    private void provideAccessToAdmin(long objectId, Class objectClass) {
        GrantedAuthoritySid adminsRole = new GrantedAuthoritySid("Admins");
        addPermission(objectId, objectClass, adminsRole, BasePermission.READ);
        addPermission(objectId, objectClass, adminsRole, BasePermission.WRITE);
        addPermission(objectId, objectClass, adminsRole, BasePermission.DELETE);
        addPermission(objectId, objectClass, adminsRole, BasePermission.ADMINISTRATION); // change owner of class
    }

    /**
     * Adds permission for recipient
     *
     * @param objectId
     * @param objectClass
     * @param recipient
     * @param permission
     */
    private void addPermission(long objectId, Class objectClass, Sid recipient, Permission permission) {
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
    private void deleteAcl(long objectId, Class objectClass) {
        ObjectIdentity oid = new ObjectIdentityImpl(objectClass, objectId);
        aclService.deleteAcl(oid, false);
    }

    /**
     * Gets current user name
     *
     * @return
     */
    private String getCurrentUsername() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        return authentication.getName();
    }

    /**
     * Tests if current user is an Admin authority
     *
     * @return
     */
    private boolean isAdmin() {
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

    public String getTournamentOwner(long tournamentId) {
        ObjectIdentity objectIdentity = new ObjectIdentityImpl(TournamentEntity.class, tournamentId);
        Acl acl = aclService.readAclById(objectIdentity);
        Sid owner = acl.getOwner();
        return (owner instanceof PrincipalSid)
                ? ((PrincipalSid)owner).getPrincipal()
                : ((GrantedAuthoritySid)owner).getGrantedAuthority();
    }
}
