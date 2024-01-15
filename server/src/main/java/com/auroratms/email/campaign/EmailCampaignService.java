package com.auroratms.email.campaign;

import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.users.UserRoles;
import com.auroratms.users.UserRolesHelper;
import com.auroratms.utils.SecurityService;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@CacheConfig(cacheNames = {"emailcampaign"})
@Transactional
@Slf4j
@PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
public class EmailCampaignService {
    
    @Autowired
    private EmailCampaignRepository repository;

    @Autowired
    private SecurityService securityService;

    private static final Class ACL_MANAGED_OBJECT_CLASS = EmailCampaignEntity.class;

    Page<EmailCampaign> findByName(String nameLike, Pageable pageable) {
        try {
            String currentUserName = UserRolesHelper.getCurrentUsername();
            String authority = UserRolesHelper.isAdmin() ? UserRoles.Admins :
                            UserRolesHelper.isTournamentDirector() ? UserRoles.TournamentDirectors :
                                    UserRoles.Everyone;
            String containsName = (StringUtils.isNotEmpty(nameLike)) ? "%" + nameLike + "%" : "%";
            Page<EmailCampaignEntity> pageOfEntities = this.repository.findAllCustom(
                    containsName, currentUserName, authority, BasePermission.WRITE.getMask(), pageable);

            Iterator<EmailCampaignEntity> iterator = pageOfEntities.iterator();
            List<EmailCampaign> EmailCampaignList = new ArrayList<>();
            while (iterator.hasNext()) {
                EmailCampaignEntity entity = iterator.next();
                EmailCampaign EmailCampaign = new EmailCampaign().convertFromEntity(entity);
                EmailCampaignList.add(EmailCampaign);
            }
            return new PageImpl<>(EmailCampaignList);
        } catch (Exception e) {
            log.error("Error listing email campaigns", e);
            throw e;
        }
    }

    @Cacheable(key = "#id")
    public EmailCampaign findById(Long id) {
        EmailCampaignEntity emailCampaignEntity = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email campaign with id " + id + " not found"));
        return new EmailCampaign().convertFromEntity(emailCampaignEntity);
    }

    @CachePut(key = "#result.id")
    public EmailCampaign save(EmailCampaign emailCampaign) {
        boolean isCreating = (emailCampaign.getId() == null);
        EmailCampaignEntity emailCampaignEntity = emailCampaign.convertToEntity();
        EmailCampaignEntity savedEmailCampaignEntity = this.repository.save(emailCampaignEntity);
        if (isCreating) {
            provideAccessToAdmin(savedEmailCampaignEntity.getId());
        }

        return new EmailCampaign().convertFromEntity(savedEmailCampaignEntity);
    }

    @CacheEvict(key = "#id")
    public void delete(long id) {
        try {
            EmailCampaignEntity emailCampaign = this.repository.findById(id).get();
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
}
