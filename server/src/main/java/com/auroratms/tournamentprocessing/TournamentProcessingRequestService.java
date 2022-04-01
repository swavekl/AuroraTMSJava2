package com.auroratms.tournamentprocessing;

import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.reports.ReportGenerationException;
import com.auroratms.users.UserRoles;
import com.auroratms.users.UserRolesHelper;
import com.auroratms.utils.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@CacheConfig(cacheNames = {"tournamentprocessingrequest"})
@Transactional
@PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('USATTTournamentManagers')")
@Slf4j
public class TournamentProcessingRequestService {

    @Autowired
    private TournamentProcessingRequestRepository repository;

    @Autowired
    private SecurityService securityService;

    private static final Class ACL_MANAGED_OBJECT_CLASS = TournamentProcessingRequest.class;

    /**
     * Finds applicaitions by name
     * @param nameLike
     * @param pageable
     * @return
     */
    Page<TournamentProcessingRequest> findByName(String nameLike, Pageable pageable) {
        Page<TournamentProcessingRequest> tournamentProcessingDataPage = null;
        try {
            String currentUserName = UserRolesHelper.getCurrentUsername();
            String authority = UserRolesHelper.isAdmin() ? UserRoles.Admins :
                    UserRolesHelper.isUSATTTournamentManager() ? UserRoles.USATTTournamentManagers :
                            UserRolesHelper.isTournamentDirector() ? UserRoles.TournamentDirectors :
                                    UserRoles.Everyone;
            String containsName = (StringUtils.isNotEmpty(nameLike)) ? "%" + nameLike + "%" : "%";
            tournamentProcessingDataPage = this.repository.findAllCustom(
                    containsName, currentUserName, authority, BasePermission.WRITE.getMask(), pageable);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return tournamentProcessingDataPage;
    }

    @Cacheable(key = "#id")
    public TournamentProcessingRequest findById(Long id) {
        return this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance request with id " + id + " not found"));
    }

    @CachePut(key = "#result.id")
    public TournamentProcessingRequest save(TournamentProcessingRequest tournamentProcessingRequest) throws ReportGenerationException {
        boolean isCreating = (tournamentProcessingRequest.getId() == null);

        // update request status on subsequent saves
        if (!isCreating) {
            TournamentProcessingRequestStatus latestStatus = TournamentProcessingRequestStatus.New;
            Date latestCreatedOn = null;
            // find the detail with the latest generation date - this is the one that needs to promote status to request
            List<TournamentProcessingRequestDetail> details = tournamentProcessingRequest.getDetails();
            for (TournamentProcessingRequestDetail detail : details) {
                if (latestCreatedOn == null) {
                    latestCreatedOn = detail.getCreatedOn();
                    latestStatus = detail.getStatus();
                } else if (detail.getCreatedOn() == null || latestCreatedOn.before(detail.getCreatedOn())) {
                    latestCreatedOn = (detail.getCreatedOn() == null) ? new Date() : detail.getCreatedOn();
                    latestStatus = detail.getStatus();
                }
            }
            tournamentProcessingRequest.setRequestStatus(latestStatus);
        }

        TournamentProcessingRequest savedTPR = this.repository.save(tournamentProcessingRequest);
        if (isCreating) {
            provideAccessToAdmin(savedTPR.getId());
            provideAccessToUSATTOfficials(savedTPR.getId());
        }
        return savedTPR;
    }

    /**
     * Grand all permissions to Admin role
     *
     * @param objectId
     */
    private void provideAccessToAdmin(long objectId) {
        this.securityService.provideAccessToAdmin(objectId, ACL_MANAGED_OBJECT_CLASS);
    }

    /**
     * Grands permissions to USATT official for this tournament
     *
     * @param objectId
     */
    private void provideAccessToUSATTOfficials(long objectId) {
        GrantedAuthoritySid grantedAuthoritySid = new GrantedAuthoritySid(UserRoles.USATTTournamentManagers);
        this.securityService.addPermission(objectId, ACL_MANAGED_OBJECT_CLASS, grantedAuthoritySid, BasePermission.READ);
        this.securityService.addPermission(objectId, ACL_MANAGED_OBJECT_CLASS, grantedAuthoritySid, BasePermission.WRITE);
    }

    public TournamentProcessingRequest findByTournamentId(long tournamentId) {
        if (this.repository.existsByTournamentId(tournamentId)) {
            return this.repository.findByTournamentId(tournamentId);
        }
        return null;
    }

    public TournamentProcessingRequest findByDetailId(long detailId) {
        return repository.findByDetailId(detailId);
    }
}
