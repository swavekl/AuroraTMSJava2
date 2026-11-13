package com.auroratms.users.emailverification.cleanupjob;

import com.auroratms.profile.UserProfile;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

/** Pure eligibility rules for deleting abandoned self-service registrations. */
public final class AccountCleanupPolicy {

    private static final String PROTECTED_GMAIL_PREFIX = "swaveklorenc+";
    private static final Pattern CLEANUP_TEST_TAG = Pattern.compile("nonokta\\d+");

    private AccountCleanupPolicy() {
    }

    /** Returns true for the named protected users and ordinary Swavek Gmail plus-alias test accounts. */
    public static boolean isProtectedEmail(String email) {
        if (email == null) {
            return false;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("swaveklorenc+td@gmail.com")
                || normalized.equals("swaveklorenc@gmail.com")
                || normalized.equals("swaveklorenc@yahoo.com")) {
            return true;
        }
        if (!normalized.startsWith(PROTECTED_GMAIL_PREFIX) || !normalized.endsWith("@gmail.com")) {
            return false;
        }
        String tag = normalized.substring(PROTECTED_GMAIL_PREFIX.length(), normalized.length() - "@gmail.com".length());
        return !CLEANUP_TEST_TAG.matcher(tag).matches();
    }

    /** Allows deletion only for eligible staged registrations and incomplete active profiles. */
    public static boolean shouldDelete(UserProfile profile) {
        if (profile == null || isProtectedEmail(profile.getEmail())) {
            return false;
        }
        if ("STAGED".equalsIgnoreCase(profile.getUserStatus())) {
            return true;
        }
        return "ACTIVE".equalsIgnoreCase(profile.getUserStatus()) && !isProfileComplete(profile);
    }

    /** Applies the same required-profile-field check used by user onboarding. */
    public static boolean isProfileComplete(UserProfile profile) {
        return profile != null
                && StringUtils.isNotEmpty(profile.getFirstName())
                && StringUtils.isNotEmpty(profile.getLastName())
                && StringUtils.isNotEmpty(profile.getMobilePhone())
                && StringUtils.isNotEmpty(profile.getEmail())
                && StringUtils.isNotEmpty(profile.getStreetAddress())
                && StringUtils.isNotEmpty(profile.getCity())
                && StringUtils.isNotEmpty(profile.getState())
                && StringUtils.isNotEmpty(profile.getZipCode())
                && StringUtils.isNotEmpty(profile.getCountryCode())
                && StringUtils.isNotEmpty(profile.getGender())
                && profile.getDateOfBirth() != null;
    }
}
