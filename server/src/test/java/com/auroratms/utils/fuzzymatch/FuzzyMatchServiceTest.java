package com.auroratms.utils.fuzzymatch;

import com.auroratms.AbstractServiceTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class FuzzyMatchServiceTest extends AbstractServiceTest {

    @Autowired
    private FuzzyMatchService fuzzyMatchService;

    @Test
    @Disabled
    void findFuzzyMatches_shouldSuccessfullyMatchCommonAbbreviations() {
        // GIVEN: Two lists with known semantic matches that challenge algorithms
        List<String> listA = List.of(
                "WOMEN’S SINGLES",
                "U1500 RATED SINGLES",
                "MIXED CHAMPIONSHIP",
                "OPEN DOUBLES"
        );

        List<String> listB = List.of(
                "OPEN DOUBLE",
                "Womens RR",
                "Mixed Champ",
                "U1500s"
        );

        // WHEN: Calling the service, which executes a real API call to GPT-4o mini
        List<EventMatch> actualMatches = fuzzyMatchService.findFuzzyMatches(listA, listB);

        // THEN: The LLM should find exactly 4 matches
        assertEquals(4, actualMatches.size(), "Should have found 4 matches.");

        // THEN: Verify the semantic matches are correct

        // 1. Verify WOMEN'S SINGLES -> Womens RR (Semantic Abbreviation Match)
        EventMatch womenMatch = actualMatches.stream()
                .filter(m -> m.listAName().equals("WOMEN’S SINGLES"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("WOMEN’S SINGLES not found."));
        assertTrue(womenMatch.listBName().contains("Womens RR"), "Expected 'Womens RR' match.");

        // 2. Verify U1500 RATED SINGLES -> U1500s (Abbreviation/Fuzzy Match)
        EventMatch u1500Match = actualMatches.stream()
                .filter(m -> m.listAName().equals("U1500 RATED SINGLES"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("U1500 RATED SINGLES not found."));
        assertTrue(u1500Match.listBName().contains("U1500s"), "Expected 'U1500s' match.");

        // 3. Verify MIXED CHAMPIONSHIP -> Mixed Champ
        EventMatch mixedMatch = actualMatches.stream()
                .filter(m -> m.listAName().equals("MIXED CHAMPIONSHIP"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("MIXED CHAMPIONSHIP not found."));
        assertTrue(mixedMatch.listBName().contains("Mixed Champ"), "Expected 'Mixed Champ' match.");
    }

    @Test
    public void testFloridaOpenEvents() {
//        --- List B (From Blank Entry Form) ---
        List<String> listB = List.of("OPEN", "WOMEN’S SINGLES", "U19 BOYS SINGLES", "U19 GIRLS SINGLES", "U17 BOYS SINGLES", "U17 GIRLS SINGLES", "U15 BOYS SINGLES", "U15 GIRLS SINGLES", "U13 BOYS SINGLES", "U13 GIRLS SINGLES", "U11 BOYS SINGLES", "U11 GIRLS SINGLES", "SENIOR 40 +", "SENIOR 60 +", "RATING UNDER 2550", "RATING UNDER 2350", "RATING UNDER 2250", "RATING UNDER 2150", "RATING UNDER 2050", "RATING UNDER 1950", "RATING UNDER 1850", "RATING UNDER 1750", "RATING UNDER 1650", "RATING UNDER 1550", "RATING UNDER 1450", "RATING UNDER 1350", "RATING UNDER 1250", "RATING UNDER 1150", "RATING UNDER 1050", "GIANT ROUND ROBIN", "HANDICAP DOUBLES");

//        --- List A (From Registration Website) ---
        List<String> listA = List.of("Under 1050 RR", "Under 1650 RR", "Under 2150 RR", "Under 1350 RR", "Open Singles RR", "Junior U19 Boys RR", "Giant Round Robin", "Under 1950 RR", "Under 1850 RR", "Under 1550 RR", "Junior Boys 17 & Under RR", "Under 1250 RR", "Under 2350 RR", "Under 2050 RR", "Handicap Dobles", "Junior Boys 13 & Under RR", "SENIOR 40 +", "SENIOR 60+", "Under 1750 RR", "Under 1450 RR", "Under 1150 RR", "Womens RR", "Junior Girls U19 RR", "Under 2250 RR", "Under 2550 RR", "Junior Boys 15 & Under RR", "Junior Boys 11 & Under RR");

        List<EventMatch> fuzzyMatches = fuzzyMatchService.findFuzzyMatches(listB, listA);

        String[] expected = {
                "Open Singles RR -> OPEN",
                "Womens RR -> WOMEN’S SINGLES",
                "Junior U19 Boys RR -> U19 BOYS SINGLES",
                "Junior Girls U19 RR -> U19 GIRLS SINGLES",
                "Junior Boys 17 & Under RR -> U17 BOYS SINGLES",
                "Junior Girls 17 & Under RR -> U17 GIRLS SINGLES",
                "Junior Boys 15 & Under RR -> U15 BOYS SINGLES",
                "Junior Girls 15 & Under RR -> U15 GIRLS SINGLES",
                "Junior Boys 13 & Under RR -> U13 BOYS SINGLES",
                "Junior Girls 13 & Under RR -> U13 GIRLS SINGLES",
                "Junior Boys 11 & Under RR -> U11 BOYS SINGLES",
                "Junior Girls 11 & Under RR -> U11 GIRLS SINGLES",
                "SENIOR 40 + -> SENIOR 40 +",
                "SENIOR 60+ -> SENIOR 60 +",
                "Under 2550 RR -> RATING UNDER 2550",
                "Under 2350 RR -> RATING UNDER 2350",
                "Under 2250 RR -> RATING UNDER 2250",
                "Under 2150 RR -> RATING UNDER 2150",
                "Under 2050 RR -> RATING UNDER 2050",
                "Under 1950 RR -> RATING UNDER 1950",
                "Under 1850 RR -> RATING UNDER 1850",
                "Under 1750 RR -> RATING UNDER 1750",
                "Under 1650 RR -> RATING UNDER 1650",
                "Under 1550 RR -> RATING UNDER 1550",
                "Under 1450 RR -> RATING UNDER 1450",
                "Under 1350 RR -> RATING UNDER 1350",
                "Under 1250 RR -> RATING UNDER 1250",
                "Under 1150 RR -> RATING UNDER 1150",
                "Under 1050 RR -> RATING UNDER 1050",
                "Giant Round Robin -> GIANT ROUND ROBIN",
                "Handicap Dobles -> HANDICAP DOUBLES"
        };
        String[] actual = new String[fuzzyMatches.size()];
        int index = 0;
        for (EventMatch fuzzyMatch : fuzzyMatches) {
            actual [index] = String.format("%s -> %s", fuzzyMatch.listBName(), fuzzyMatch.listAName());
            index++;
        }

        assertArrayEquals(expected, actual);
    }
}