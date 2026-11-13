package com.auroratms.users.emailverification.cleanupjob;

import com.auroratms.profile.UserProfile;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class AccountCleanupPolicyTest {

    @Test
    void protectsNamedAccountsAndOrdinaryPlusAliasesButAllowsReservedCleanupAliases() {
        assertTrue(AccountCleanupPolicy.isProtectedEmail("swaveklorenc+td@gmail.com"));
        assertTrue(AccountCleanupPolicy.isProtectedEmail("swaveklorenc@gmail.com"));
        assertTrue(AccountCleanupPolicy.isProtectedEmail("swaveklorenc@yahoo.com"));
        assertTrue(AccountCleanupPolicy.isProtectedEmail("swaveklorenc+test@gmail.com"));
        assertFalse(AccountCleanupPolicy.isProtectedEmail("swaveklorenc+nonokta1@gmail.com"));
        assertFalse(AccountCleanupPolicy.isProtectedEmail("swaveklorenc+nonokta23@gmail.com"));
        assertFalse(AccountCleanupPolicy.isProtectedEmail("someone@gmail.com"));

        UserProfile cleanupTest = new UserProfile();
        cleanupTest.setUserStatus("STAGED");
        cleanupTest.setEmail("swaveklorenc+nonokta1@gmail.com");
        assertTrue(AccountCleanupPolicy.shouldDelete(cleanupTest));
    }

    @Test
    void stagedUsersAreEligibleWithoutCompletedProfiles() {
        UserProfile staged = new UserProfile();
        staged.setEmail("new-user@example.com");
        staged.setUserStatus("STAGED");

        assertTrue(AccountCleanupPolicy.shouldDelete(staged));
    }

    @Test
    void activeIncompleteUsersAreEligibleButCompleteProfilesArePreserved() {
        UserProfile incomplete = new UserProfile();
        incomplete.setUserStatus("ACTIVE");
        incomplete.setEmail("new-user@example.com");
        assertTrue(AccountCleanupPolicy.shouldDelete(incomplete));

        UserProfile complete = completeProfile();
        assertTrue(AccountCleanupPolicy.isProfileComplete(complete));
        assertFalse(AccountCleanupPolicy.shouldDelete(complete));
    }

    @Test
    void unexpectedLifecycleStatesAndProtectedStagedUsersArePreserved() {
        UserProfile suspended = new UserProfile();
        suspended.setUserStatus("SUSPENDED");
        suspended.setEmail("new-user@example.com");
        assertFalse(AccountCleanupPolicy.shouldDelete(suspended));

        UserProfile protectedStaged = new UserProfile();
        protectedStaged.setUserStatus("STAGED");
        protectedStaged.setEmail("swaveklorenc+td@gmail.com");
        assertFalse(AccountCleanupPolicy.shouldDelete(protectedStaged));
    }

    private UserProfile completeProfile() {
        UserProfile profile = new UserProfile();
        profile.setUserStatus("ACTIVE");
        profile.setFirstName("Test");
        profile.setLastName("User");
        profile.setMobilePhone("555-1111");
        profile.setEmail("test@example.com");
        profile.setStreetAddress("1 Test St");
        profile.setCity("Chicago");
        profile.setState("IL");
        profile.setZipCode("60601");
        profile.setCountryCode("US");
        profile.setGender("Male");
        profile.setDateOfBirth(new Date());
        return profile;
    }
}
