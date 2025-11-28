package com.auroratms.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeNormalizer {

    // Matches:
    //  - 830am, 830 AM, 8:30am, 8:30 AM
    //  - 5pm, 5 pm, 5p.m.
    //  - 3 (hour-only)
    //  - Does NOT match dollar amounts or ratings like U2200
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "\\b(\\d{1,2})(:?)(\\d{0,2})\\s*(a\\.?m\\.?|p\\.?m\\.?|am|pm|AM|PM)?\\b"
    );

    public static String normalizeSingleTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "";
        }

        raw = raw.trim();

        // Extract groups using the same pattern
        Matcher m = TIME_PATTERN.matcher(raw);
        if (!m.matches()) {
            return raw; // not a time
        }

        String hourStr = m.group(1);
        String colon = m.group(2);
        String minuteStr = m.group(3);
        String ampm = m.group(4);

        int hour = Integer.parseInt(hourStr);

        // Normalize AM/PM text (AM, PM or "")
        String suffix = "";
        if (ampm != null) {
            suffix = ampm.replace(".", "").toUpperCase(); // a.m. → AM
        }

        // Normalize minutes
        if (minuteStr == null || minuteStr.isEmpty()) {
            minuteStr = "00";
        } else if (minuteStr.length() == 1) {
            minuteStr = "0" + minuteStr; // "5" → "05"
        }

        // Normalize hour range only if AM/PM is present
        if (!suffix.isEmpty()) {
            // Standardize hours:
            // 12 stays 12
            // 00→12 for AM times
            if (hour == 0) hour = 12;
            if (hour > 12) hour = hour % 12 == 0 ? 12 : hour % 12;
        }

        return hour + ":" + minuteStr + (suffix.isEmpty() ? "" : " " + suffix);
    }

    /**
     * Scans an entire page of text and normalizes ANY time patterns found.
     */
    public static String normalizeTimeInPage(String pageText) {
        if (pageText == null || pageText.isEmpty()) {
            return "";
        }

        Matcher matcher = TIME_PATTERN.matcher(pageText);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String rawTime = matcher.group();
            String normalized = normalizeSingleTime(rawTime);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(normalized));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }
}
