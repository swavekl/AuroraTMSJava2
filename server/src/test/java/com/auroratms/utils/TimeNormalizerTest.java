package com.auroratms.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeNormalizerTest {

    @Test
    void testStandardTimes() {
        assertEquals("8:30 AM", TimeNormalizer.normalizeSingleTime("8:30 AM"));
        assertEquals("2:00 PM", TimeNormalizer.normalizeSingleTime("2 PM"));
        assertEquals("11:00 AM", TimeNormalizer.normalizeSingleTime("11 am"));
    }

    @Test
    void testCompactTimes() {
        assertEquals("8:30 AM", TimeNormalizer.normalizeSingleTime("830am"));
        assertEquals("8:30 AM", TimeNormalizer.normalizeSingleTime("830 AM"));
    }

    @Test
    void testHourOnly() {
        assertEquals("3:00 PM", TimeNormalizer.normalizeSingleTime("3 PM"));
        assertEquals("11:00 AM", TimeNormalizer.normalizeSingleTime("11AM"));
        assertEquals("3:00", TimeNormalizer.normalizeSingleTime("3"));
    }

    @Test
    void testMinuteFix() {
        assertEquals("12:05 PM", TimeNormalizer.normalizeSingleTime("12:5 pm"));
        assertEquals("7:09 AM", TimeNormalizer.normalizeSingleTime("7:9 AM"));
    }

    @Test
    void testDotNotation() {
        assertEquals("8:30 AM", TimeNormalizer.normalizeSingleTime("8:30 a.m."));
        assertEquals("4:00 PM", TimeNormalizer.normalizeSingleTime("4 p.m."));
    }

    @Test
    void testNormalizeEntirePage() {
        String pageText =
                "U2200 Sat 5pm $55\n" +
                        "U2100 Sun 11am $45\n" +
                        "U2000 Sat 9am $40";

        String normalized = TimeNormalizer.normalizeTimeInPage(pageText);

        assertEquals(
                "U2200 Sat 5:00 PM $55\n" +
                        "U2100 Sun 11:00 AM $45\n" +
                        "U2000 Sat 9:00 AM $40",
                normalized
        );
    }

    @Test
    void testIgnoreNonTimes() {
        assertEquals("U2200", TimeNormalizer.normalizeTimeInPage("U2200"));
        assertEquals("$55", TimeNormalizer.normalizeTimeInPage("$55"));
    }
}
