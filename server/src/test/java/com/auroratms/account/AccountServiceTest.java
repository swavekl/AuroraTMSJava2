package com.auroratms.account;

import com.auroratms.AbstractServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class AccountServiceTest extends AbstractServiceTest {

    @Autowired
    private AccountService accountService;

    @Test
    public void testStoreAndRetrieve() {
        String userProfileId = new Random().toString();
        System.out.println("random userProfileId = " + userProfileId);
        boolean exists = this.accountService.existsById(userProfileId);
        assertFalse(exists, "random account shouldn't exist");

        AccountEntity account = new AccountEntity();
        account.setAccountId("acct_1234567890");
        account.setProfileId(userProfileId);
        account.setEmail("bozo@yahoo.com");
        account.setActivated(false);
        this.accountService.save(account);

        boolean checkExistsAgain = this.accountService.existsById(userProfileId);
        assertTrue(checkExistsAgain, "doesn't exist ");

        AccountEntity foundAccount = this.accountService.findById(userProfileId);
        assertEquals(account, foundAccount, "Account not the same");

        foundAccount.setActivated(true);
        this.accountService.save(foundAccount);

        AccountEntity foundAccountAgain = this.accountService.findById(userProfileId);
        assertTrue(foundAccountAgain.isActivated(), "Should be activated");

        this.accountService.delete(userProfileId);
        boolean existsNoMore = this.accountService.existsById(userProfileId);
        assertFalse(existsNoMore, "Should be deleted");
    }
}
