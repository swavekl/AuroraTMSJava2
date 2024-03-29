package com.auroratms.clubaffiliationapp;

import com.auroratms.clubaffiliationapp.notification.ClubAffiliationApplicationEventPublisher;
import com.auroratms.error.ResourceNotFoundException;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@CacheConfig(cacheNames = {"clubaffiliationapplication"})
@Transactional
@PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('USATTClubManagers')")
public class ClubAffiliationApplicationService {

    @Autowired
    private ClubAffiliationApplicationRepository repository;

    @Autowired
    private SecurityService securityService;

    @Autowired
    ClubAffiliationApplicationEventPublisher eventPublisher;

    private static final Class ACL_MANAGED_OBJECT_CLASS = ClubAffiliationApplicationEntity.class;

    /**
     * Finds applicaitions by name
     *
     * @param nameLike
     * @param pageable
     * @return
     */
    Page<ClubAffiliationApplication> findByName(String nameLike, Pageable pageable) {
        PageImpl<ClubAffiliationApplication> applicationPage = null;
        try {
            String currentUserName = UserRolesHelper.getCurrentUsername();
            String authority = UserRolesHelper.isAdmin() ? UserRoles.Admins :
                    UserRolesHelper.isUSATTClubManager() ? UserRoles.USATTClubManagers :
                            UserRolesHelper.isTournamentDirector() ? UserRoles.TournamentDirectors :
                                    UserRoles.Everyone;
            String containsName = (StringUtils.isNotEmpty(nameLike)) ? "%" + nameLike + "%" : "%";
            Page<ClubAffiliationApplicationEntity> page = this.repository.findAllCustom(
                    containsName, currentUserName, authority, BasePermission.WRITE.getMask(), pageable);
            List<ClubAffiliationApplicationEntity> entities = page.getContent();
            List<ClubAffiliationApplication> applications = new ArrayList<>(entities.size());
            for (ClubAffiliationApplicationEntity entity : entities) {
                ClubAffiliationApplication application = new ClubAffiliationApplication();
                application.convertFromEntity(entity);
                applications.add(application);
            }
            applicationPage = new PageImpl<>(applications, pageable, page.getTotalElements());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return applicationPage;
    }

    /**
     * Finds latest approved applications with name filter
     * @param nameLike
     * @param pageable
     * @return
     */
    public Page<ClubAffiliationApplication> findByNameAndLatest(String nameLike, Pageable pageable) {
        try {
            String containsName = (StringUtils.isNotEmpty(nameLike)) ? "%" + nameLike + "%" : "%";
            Page<ClubAffiliationApplicationEntity> page = this.repository.findAllByNameLikeIgnoreCaseAndMostRecentExpirationDate(
                    containsName, pageable);
            List<ClubAffiliationApplicationEntity> entities = page.getContent();
            List<ClubAffiliationApplication> applications = new ArrayList<>(entities.size());
            for (ClubAffiliationApplicationEntity entity : entities) {
                ClubAffiliationApplication application = new ClubAffiliationApplication();
                application.convertFromEntity(entity);
                applications.add(application);
            }
            return new PageImpl<>(applications, pageable, page.getTotalElements());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Cacheable(key = "#id")
    public ClubAffiliationApplication findById(Long id) {
        ClubAffiliationApplicationEntity entity = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club affiliation application with id " + id + " not found"));
        return new ClubAffiliationApplication().convertFromEntity(entity);
    }

    @CachePut(key = "#result.id")
    public ClubAffiliationApplication save(ClubAffiliationApplication clubAffiliationApplication) {
        boolean isCreating = (clubAffiliationApplication.getId() == null);
        ClubAffiliationApplicationStatus oldStatus = null;
        if (!isCreating) {
            ClubAffiliationApplication oldEntity = this.findById(clubAffiliationApplication.getId());
            oldStatus = oldEntity.getStatus();
        }
        ClubAffiliationApplicationEntity entity = clubAffiliationApplication.convertToEntity();
        ClubAffiliationApplicationEntity savedEntity = this.repository.save(entity);
        if (isCreating) {
            provideAccessToAdmin(savedEntity.getId());
            provideAccessToUSATTOfficials(savedEntity.getId());
        }

        ClubAffiliationApplication retValue = new ClubAffiliationApplication().convertFromEntity(entity);
        // send email about payment completed, approval/rejection etc. if status changed
        // create/update club information in club table.
        eventPublisher.publishEvent(retValue, oldStatus);

        return retValue;
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
        GrantedAuthoritySid grantedAuthoritySid = new GrantedAuthoritySid(UserRoles.USATTClubManagers);
        this.securityService.addPermission(objectId, ACL_MANAGED_OBJECT_CLASS, grantedAuthoritySid, BasePermission.READ);
        this.securityService.addPermission(objectId, ACL_MANAGED_OBJECT_CLASS, grantedAuthoritySid, BasePermission.WRITE);
    }

    /**
     *
     * @param applicationId
     * @param newStatus
     */
    public void updateStatus(Long applicationId, ClubAffiliationApplicationStatus newStatus) {
        ClubAffiliationApplication clubAffiliationApplication = this.findById(applicationId);
        clubAffiliationApplication.setStatus(newStatus);
        this.save(clubAffiliationApplication);
    }
}
