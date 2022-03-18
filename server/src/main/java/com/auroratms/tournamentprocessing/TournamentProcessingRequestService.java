package com.auroratms.tournamentprocessing;

import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.reports.*;
import com.auroratms.users.UserRoles;
import com.auroratms.users.UserRolesHelper;
import com.auroratms.utils.SecurityService;
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

import java.util.List;

@Service
@CacheConfig(cacheNames = {"tournamentprocessingrequest"})
@Transactional
@PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('USATTTournamentManagers')")
public class TournamentProcessingRequestService {

    @Autowired
    private TournamentProcessingRequestRepository repository;

    @Autowired
    private SecurityService securityService;

//    @Autowired
//    TournamentProcessingDataEventPublisher eventPublisher;

    @Autowired
    private TournamentProcessingReportService tournamentProcessingReportService;

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
        TournamentProcessingRequestStatus oldStatus = null;
        if (!isCreating) {
            TournamentProcessingRequest oldEntity = this.findById(tournamentProcessingRequest.getId());
            oldStatus = oldEntity.getStatus();
        } else {
            oldStatus = TournamentProcessingRequestStatus.New;
        }

        // generate reports if needed
        tournamentProcessingRequest = generateReports (tournamentProcessingRequest);

        TournamentProcessingRequest savedTournamentProcessingRequest = this.repository.save(tournamentProcessingRequest);
        if (isCreating) {
            provideAccessToAdmin(savedTournamentProcessingRequest.getId());
            provideAccessToUSATTOfficials(savedTournamentProcessingRequest.getId());
        }

        // send email about payment completed, approval/rejection etc. if status changed
        // create/update club information in club table.
//        eventPublisher.publishEvent(savedTournamentProcessingData, oldStatus);

        return savedTournamentProcessingRequest;
    }

    private TournamentProcessingRequest generateReports(TournamentProcessingRequest tournamentProcessingRequest) throws ReportGenerationException {
        if (needsToGenerateReports(tournamentProcessingRequest)) {
            this.tournamentProcessingReportService.generateReports(tournamentProcessingRequest);
        }
        return tournamentProcessingRequest;
    }

    /**
     *
     * @param tournamentProcessingRequest
     * @return
     */
    private boolean needsToGenerateReports(TournamentProcessingRequest tournamentProcessingRequest) {
        boolean createReports = false;
        if (tournamentProcessingRequest.getId() == null) {
            createReports = true;
        } else {
            List<TournamentProcessingRequestDetail> details = tournamentProcessingRequest.getDetails();
            for (TournamentProcessingRequestDetail detail : details) {
                if (detail.getId() == null) {
                    createReports = true;
                    break;
                }
            }
        }
        return createReports;
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
}
