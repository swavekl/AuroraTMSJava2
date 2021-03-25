package com.auroratms.account;

import com.auroratms.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service which is interfacing with the HashiCorp Vault for storing Stripe Account information
 */
@Service
@CacheConfig(cacheNames = {"accounts"})
public class AccountService {

    @Autowired
    AccountRepository accountRepository;

    public boolean existsById(String userProfileId) {
        return this.accountRepository.existsById(userProfileId);
    }

    @CachePut(key = "#result.profileId")
    public AccountEntity save (AccountEntity accountEntity) {
        return this.accountRepository.save(accountEntity);

    }

    @Cacheable(key = "#userProfileId")
    public AccountEntity findById(String userProfileId) {
        return this.accountRepository.findById(userProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Account " + userProfileId + " not found"));
    }

    @CacheEvict(key = "#userProfileId")
    public void delete (String userProfileId) {
        this.accountRepository.deleteById(userProfileId);
    }

}
