package com.auroratms.email.config;

import com.auroratms.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = {"emailserverconfigs"})
public class EmailServerConfigurationService {

    @Autowired
    private EmailServerConfigurationRepository emailServerConfigurationRepository;

    public boolean existsById(String userProfileId) {
        return this.emailServerConfigurationRepository.existsById(userProfileId);
    }

    @CachePut(key = "#result.id")
    public EmailServerConfigurationEntity save (EmailServerConfigurationEntity emailServerConfigurationEntity) {
        return this.emailServerConfigurationRepository.save(emailServerConfigurationEntity);

    }

    @Cacheable(key = "#profileId")
    public EmailServerConfigurationEntity findById(String profileId) {
        return this.emailServerConfigurationRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Email server configuration for user with profile id '" + profileId + "' not found. Make sure that the email server is configured for this user."));
    }

    @CacheEvict(key = "#profileId")
    public void delete(String profileId) {
        this.emailServerConfigurationRepository.deleteById(profileId);
    }

}
