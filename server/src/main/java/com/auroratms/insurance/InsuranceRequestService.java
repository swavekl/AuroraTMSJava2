package com.auroratms.insurance;

import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.insurance.notification.InsuranceRequestEventPublisher;
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
@CacheConfig(cacheNames = {"insurancerequest"})
@Transactional
@PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('USATTOfficials')")
public class InsuranceRequestService {

    @Autowired
    private InsuranceRequestRepository repository;

    @Autowired
    private SecurityService securityService;

    @Autowired
    InsuranceRequestEventPublisher eventPublisher;

    private static final Class ACL_MANAGED_OBJECT_CLASS = InsuranceRequest.class;

    /**
     * Finds applicaitions by name
     * @param nameLike
     * @param pageable
     * @return
     */
    Page<InsuranceRequest> findByName(String nameLike, Pageable pageable) {
        Page<InsuranceRequest> insuranceRequestPage = null;
        try {
            String currentUserName = UserRolesHelper.getCurrentUsername();
            String authority = UserRolesHelper.isAdmin() ? UserRoles.Admins :
                    UserRolesHelper.isUSATTOfficial() ? UserRoles.USATTOfficials :
                            UserRolesHelper.isTournamentDirector() ? UserRoles.TournamentDirectors :
                                    UserRoles.Everyone;
            String containsName = (StringUtils.isNotEmpty(nameLike)) ? "%" + nameLike + "%" : "%";
            insuranceRequestPage = this.repository.findAllCustom(
                    containsName, currentUserName, authority, BasePermission.WRITE.getMask(), pageable);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return insuranceRequestPage;
    }

    @Cacheable(key = "#id")
    public InsuranceRequest findById(Long id) {
        return this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance request with id " + id + " not found"));
    }

    @CachePut(key = "#result.id")
    public InsuranceRequest save(InsuranceRequest insuranceRequest) {
        boolean isCreating = (insuranceRequest.getId() == null);
        InsuranceRequestStatus oldStatus = null;
        if (!isCreating) {
            InsuranceRequest oldEntity = this.findById(insuranceRequest.getId());
            oldStatus = oldEntity.getStatus();
        }
        InsuranceRequest savedInsuranceRequest = this.repository.save(insuranceRequest);
        if (isCreating) {
            provideAccessToAdmin(savedInsuranceRequest.getId());
            provideAccessToUSATTOfficials(savedInsuranceRequest.getId());
        }

        // send email about payment completed, approval/rejection etc. if status changed
        // create/update club information in club table.
        eventPublisher.publishEvent(savedInsuranceRequest, oldStatus);

        return savedInsuranceRequest;
    }

    @CacheEvict(key = "#id")
    public void delete(long id) {
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
        GrantedAuthoritySid grantedAuthoritySid = new GrantedAuthoritySid(UserRoles.USATTOfficials);
        this.securityService.addPermission(objectId, ACL_MANAGED_OBJECT_CLASS, grantedAuthoritySid, BasePermission.READ);
        this.securityService.addPermission(objectId, ACL_MANAGED_OBJECT_CLASS, grantedAuthoritySid, BasePermission.WRITE);
    }

    /**
     * Updates status field
     * @param id
     * @param status
     */
    public void updateStatus(Long id, InsuranceRequestStatus status) {
        InsuranceRequest insuranceRequest = this.findById(id);
        insuranceRequest.setStatus(status);
        this.save(insuranceRequest);
    }
}
