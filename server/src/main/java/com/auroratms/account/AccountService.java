package com.auroratms.account;

import com.auroratms.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service which is interfacing with the HashiCorp Vault for storing Stripe Account information
 */
@Service
public class AccountService {

    @Autowired
    AccountRepository accountRepository;

    public boolean existsById(String userProfileId) {
        return this.accountRepository.existsById(userProfileId);
    }

    public AccountEntity save (AccountEntity accountEntity) {
        return this.accountRepository.save(accountEntity);

    }

    public AccountEntity findById(String userProfileId) {
        return this.accountRepository.findById(userProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Account " + userProfileId + " not found"));
    }

    public void delete (String userProfileId) {
        this.accountRepository.deleteById(userProfileId);
    }

}
