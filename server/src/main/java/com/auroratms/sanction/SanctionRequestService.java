package com.auroratms.sanction;

import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.sanction.notification.SanctionRequestEventPublisher;
import com.auroratms.users.UserRoles;
import com.auroratms.users.UserRolesHelper;
import com.auroratms.utils.SecurityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CacheConfig(cacheNames = {"sanctionrequest"})
@Transactional
@PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('USATTSanctionCoordinators')")
public class SanctionRequestService {
    
    @Autowired
    private SanctionRequestRepository repository;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private SanctionRequestEventPublisher eventPublisher;

    private static final Class ACL_MANAGED_OBJECT_CLASS = SanctionRequest.class;

    /**
     * Finds applicaitions by name
     * @param nameLike
     * @param pageable
     * @return
     */
    Page<SanctionRequestEntity> findByName(String nameLike, Pageable pageable) {
        Page<SanctionRequestEntity> sanctionRequestPage = null;
        try {
            String currentUserName = UserRolesHelper.getCurrentUsername();
            String authority = UserRolesHelper.isAdmin() ? UserRoles.Admins :
                    UserRolesHelper.isUSATTSanctionCoordinator() ? UserRoles.USATTSanctionCoordinators :
                            UserRolesHelper.isTournamentDirector() ? UserRoles.TournamentDirectors :
                                    UserRoles.Everyone;
            String containsName = (StringUtils.isNotEmpty(nameLike)) ? "%" + nameLike + "%" : "%";
            sanctionRequestPage = this.repository.findAllCustom(
                    containsName, currentUserName, authority, BasePermission.WRITE.getMask(), pageable);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return sanctionRequestPage;
    }

    @Cacheable(key = "#id")
    public SanctionRequestEntity findById(Long id) {
        return this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sanction request with id " + id + " not found"));
    }

    @CachePut(key = "#result.id")
    public SanctionRequestEntity save(SanctionRequestEntity sanctionRequest) {
        boolean isCreating = (sanctionRequest.getId() == null);
        SanctionRequestStatus oldStatus = null;
        if (!isCreating) {
            SanctionRequestEntity oldEntity = this.findById(sanctionRequest.getId());
            oldStatus = oldEntity.getStatus();
        } else {
            oldStatus = SanctionRequestStatus.New;
        }
        SanctionRequestEntity savedSanctionRequest = this.repository.save(sanctionRequest);
        if (isCreating) {
            provideAccessToAdmin(savedSanctionRequest.getId());
            provideAccessToUSATTOfficials(savedSanctionRequest.getId());
        }

        // send email about payment completed, approval/rejection etc. if status changed
        if (oldStatus != sanctionRequest.getStatus()) {
            SanctionRequest convertedSanctionRequest = new SanctionRequest().convertFromEntity(savedSanctionRequest);
            eventPublisher.publishEvent(convertedSanctionRequest, oldStatus);
        }

        return savedSanctionRequest;
    }

    @CacheEvict(key = "#id")
    public void delete(long id) {
        try {
            SanctionRequestEntity sanctionRequest = this.repository.findById(id).get();
        } catch (Exception e) {
            // ignore
        }

        this.repository.deleteById(id);
        this.securityService.deleteAcl(id, ACL_MANAGED_OBJECT_CLASS);
    }

    public boolean exists(long applicationId) {
        return this.repository.existsById(applicationId);
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
        GrantedAuthoritySid grantedAuthoritySid = new GrantedAuthoritySid(UserRoles.USATTSanctionCoordinators);
        this.securityService.addPermission(objectId, ACL_MANAGED_OBJECT_CLASS, grantedAuthoritySid, BasePermission.READ);
        this.securityService.addPermission(objectId, ACL_MANAGED_OBJECT_CLASS, grantedAuthoritySid, BasePermission.WRITE);
    }

    /**
     * Updates status field
     * @param id
     * @param status
     */
    public void updateStatus(Long id, SanctionRequestStatus status) {
        SanctionRequestEntity sanctionRequest = this.findById(id);
        sanctionRequest.setStatus(status);
        this.save(sanctionRequest);
    }

}
