package com.auroratms.account;

import com.auroratms.AbstractServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

import static org.junit.Assert.*;

@Transactional
public class AccountServiceTest extends AbstractServiceTest {

    @Autowired
    private AccountService accountService;

    @Test
    public void testStoreAndRetrieve() {
        String userProfileId = new Random().toString();
        System.out.println("random userProfileId = " + userProfileId);
        boolean exists = this.accountService.existsById(userProfileId);
        assertFalse("random account shouldn't exist", exists);

        AccountEntity account = new AccountEntity();
        account.setAccountId("acct_1234567890");
        account.setProfileId(userProfileId);
        account.setAccountPublicKey("pk_test_12345678");
        account.setAccountSecretKey("sk_test_abcdefgh");
        account.setActivated(false);
        this.accountService.save(account);

        boolean checkExistsAgain = this.accountService.existsById(userProfileId);
        assertTrue("doesn't exist ", checkExistsAgain);

        AccountEntity foundAccount = this.accountService.findById(userProfileId);
        assertEquals("Account not the same", account, foundAccount);

        foundAccount.setActivated(true);
        this.accountService.save(foundAccount);

        AccountEntity foundAccountAgain = this.accountService.findById(userProfileId);
        assertTrue("Should be activated", foundAccountAgain.isActivated());

        this.accountService.delete(userProfileId);
        boolean existsNoMore = this.accountService.existsById(userProfileId);
        assertFalse("Should be deleted", existsNoMore);
    }
}
