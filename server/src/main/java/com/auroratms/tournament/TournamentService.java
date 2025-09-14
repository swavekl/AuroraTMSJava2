package com.auroratms.tournament;

import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.users.UserRoles;
import com.auroratms.users.UserRolesHelper;
import com.auroratms.utils.SecurityService;
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
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private SecurityService securityService;

    @Autowired
    private UserProfileService userProfileService;

    // class used for managing Access Control lists for tournaments
    private static final Class ACL_MANAGED_OBJECT_CLASS = TournamentEntity.class;

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

    public Collection<Tournament> listTournamentsAfterDate(Date date) {
        Collection<TournamentEntity> tournamentEntities = repository.findAllByStartDateAfterOrderByStartDateDesc(date).stream()
                .collect(Collectors.toList());
        return toTournamentCollection(tournamentEntities);
    }

    public Collection<Tournament> listTournamentsByIds(List<Long> tournamentIds) {
        List<TournamentEntity> tournamentEntities = repository.findAllById(tournamentIds).stream().collect(Collectors.toList());
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
     * List all tournaments which are played on given day
     * @param date
     * @return
     */
    public Collection<Tournament> listDaysTournaments(Date date) {
        Collection<TournamentEntity> tournamentEntities = repository.findDaysTournaments(date).stream()
                .collect(Collectors.toList());
        return toTournamentCollection(tournamentEntities);
    }


    /**
     * List tournaments owned by the current user (e.g. tournament director) or admin
     *
     * @return
     */
    public Collection<Tournament> listOwned(int page, int size) {
        String currentUser = getCurrentUsername();
        String authority = UserRolesHelper.isAdmin() ? UserRoles.Admins : UserRoles.Everyone;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.ASC, "name");
        Page<TournamentEntity> ownedTournaments = repository.findWriteable(currentUser, authority, BasePermission.WRITE.getMask(), pageRequest);
        List<TournamentEntity> tournamentEntities = ownedTournaments.get().collect(Collectors.toList());
        Comparator<TournamentEntity> comparator = Comparator
                .comparing(TournamentEntity::getStartDate).reversed();
        Collections.sort(tournamentEntities, comparator);
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
        // fetch the old tournament definition containing personnel list
        List<Personnel> oldPersonnelList = null;
        if (!isCreating) {
            TournamentEntity oldTournamentEntity = repository.getReferenceById(tournament.getId());
            Tournament oldTournament = new Tournament().convertFromEntity(oldTournamentEntity);
            oldPersonnelList = oldTournament.getConfiguration().getPersonnelList();
        }
        TournamentEntity savedTournamentEntity = repository.save(tournamentEntity);
        if (isCreating) {
            provideAccessToAdmin(savedTournamentEntity.getId());
            provideAccessToUSATTOfficials(savedTournamentEntity.getId());
        }

        // grant or revoke access to this tournament to particular personnel e.g. tournament referee, umpire and data entry clerks
        grantRevokePersonnelAccess(savedTournamentEntity.getId(), oldPersonnelList, tournament.getConfiguration().getPersonnelList());

        return new Tournament().convertFromEntity(savedTournamentEntity);
    }

    /**
     * Updates the tournament - e.g. after updating statistics
     *
     * @param tournament
     */
    @CachePut(key = "#result.id")
    @PreAuthorize("isAuthenticated()")
    public Tournament updateTournament(Tournament tournament) {
        TournamentEntity tournamentEntity = tournament.convertToEntity();
        TournamentEntity savedTournamentEntity = repository.save(tournamentEntity);
        return new Tournament().convertFromEntity(savedTournamentEntity);
    }

    /**
     * Updates user access for tournament personnel
     *
     * @param tournamentId
     * @param oldPersonnelList
     * @param currentPersonnelList
     */
    private void grantRevokePersonnelAccess(Long tournamentId, List<Personnel> oldPersonnelList, List<Personnel> currentPersonnelList) {
        // find newly added personnel
        List<Personnel> addedPersonnel = getListDifference(oldPersonnelList, currentPersonnelList);
        // find removed personnel
        List<Personnel> removedPersonnel = getListDifference(currentPersonnelList, oldPersonnelList);
        // skip this work if there were no changes
        if (addedPersonnel.size() > 0 || removedPersonnel.size() > 0) {

            // collect user profile ids so we can translate them into login ids needed for ACLs
            List<String> profileIds = new ArrayList<>();
            for (Personnel personnel : addedPersonnel) {
                profileIds.add(personnel.getProfileId());
            }
            for (Personnel personnel : removedPersonnel) {
                profileIds.add(personnel.getProfileId());
            }
            Collection<UserProfile> userProfiles = this.userProfileService.listByProfileIds(profileIds);

            // grant access to newly added personnel
            for (Personnel personnel : addedPersonnel) {
                for (UserProfile userProfile : userProfiles) {
                    if (userProfile.getUserId().equals(personnel.getProfileId())) {
                        this.grantUserAccess(tournamentId, userProfile.getLogin());
                    }
                }
            }

            // revoke access to users who are no longer serving at this tournament
            for (Personnel personnel : removedPersonnel) {
                for (UserProfile userProfile : userProfiles) {
                    if (userProfile.getUserId().equals(personnel.getProfileId())) {
                        this.revokeUserAccess(tournamentId,  userProfile.getLogin());
                    }
                }
            }
        }
    }

    /**
     * Creates a list with items that are in the second list but not the first list
     *
     * @param personnelList1
     * @param personnelList2
     * @return
     */
    private List<Personnel> getListDifference(List<Personnel> personnelList1, List<Personnel> personnelList2) {
        List<Personnel> missingPersonnel = new ArrayList<>();
        List<Personnel> personnelList1ToCheck = (personnelList1 == null) ? Collections.emptyList() : personnelList1;
        List<Personnel> personnelList2ToCheck = (personnelList2 == null) ? Collections.emptyList() : personnelList2;
        for (Personnel personnel2 : personnelList2ToCheck) {
            boolean found = false;
            for (Personnel personnel1 : personnelList1ToCheck) {
                if (personnel1.getProfileId().equals(personnel2.getProfileId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missingPersonnel.add(personnel2);
            }
        }
        return missingPersonnel;
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
        this.securityService.deleteAcl(tournamentId, ACL_MANAGED_OBJECT_CLASS);
    }

    /**
     * Grand all permissions to Admin role
     *  @param objectId
     *
     */
    private void provideAccessToAdmin(long objectId) {
        this.securityService.provideAccessToAdmin(objectId, ACL_MANAGED_OBJECT_CLASS);
    }

    /**
     * Grands permissions to USATT official for this tournament
     * @param objectId
     */
    private void provideAccessToUSATTOfficials(long objectId) {
        GrantedAuthoritySid grantedAuthoritySid = new GrantedAuthoritySid(UserRoles.USATTTournamentManagers);
        this.securityService.addPermission(objectId, ACL_MANAGED_OBJECT_CLASS, grantedAuthoritySid, BasePermission.READ);
        this.securityService.addPermission(objectId, ACL_MANAGED_OBJECT_CLASS, grantedAuthoritySid, BasePermission.WRITE);
    }

    /**
     * Grants access to user
     * @param objectId
     * @param userId
     */
    private void grantUserAccess(long objectId, String userId) {
        PrincipalSid principalSid = new PrincipalSid(userId);
        this.securityService.addPermission(objectId, ACL_MANAGED_OBJECT_CLASS, principalSid, BasePermission.READ);
        this.securityService.addPermission(objectId, ACL_MANAGED_OBJECT_CLASS, principalSid, BasePermission.WRITE);
    }

    /**
     * Revokes user access to object
     * @param objectId
     * @param userId
     */
    private void revokeUserAccess(long objectId, String userId) {
        PrincipalSid principalSid = new PrincipalSid(userId);
        this.securityService.revokeAllPermissions(objectId, ACL_MANAGED_OBJECT_CLASS, principalSid);
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
     * Gets identifier of tournament owner
     * @param tournamentId
     * @return
     */
    public String getTournamentOwner(long tournamentId) {
        Acl acl = this.securityService.readAclForObject(tournamentId, ACL_MANAGED_OBJECT_CLASS);
        Sid owner = acl.getOwner();
        return (owner instanceof PrincipalSid ps)
                ? ps.getPrincipal()
                : ((GrantedAuthoritySid)owner).getGrantedAuthority();
    }
}
