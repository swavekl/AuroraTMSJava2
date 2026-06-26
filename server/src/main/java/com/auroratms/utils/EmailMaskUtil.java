package com.auroratms.utils;

public final class EmailMaskUtil {

    private EmailMaskUtil() {
    }

    /**
     * Masks an email address for display.
     *
     * Examples:
     *   stanloc@yahoo.com   -> s*****c@y***m
     *   ab@gmail.com        -> a*b@g***l
     *   a@x.com             -> a@x***m
     *   john.doe@test.org   -> j******e&t***g
     */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }

        int at = email.indexOf('@');
        if (at <= 0 || at == email.length() - 1) {
            return "********";
        }

        String localPart = email.substring(0, at);
        String domainPart = email.substring(at + 1);

        return mask(localPart) + "@" + mask(domainPart);
    }

    private static String mask(String value) {
        if (value.length() == 1) {
            return value;
        }

        if (value.length() == 2) {
            return value.charAt(0) + "*";
        }

        return value.charAt(0)
                + "*".repeat(value.length() - 2)
                + value.charAt(value.length() - 1);
    }
}
