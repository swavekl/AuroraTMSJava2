package com.auroratms.utils;

import com.auroratms.AbstractServiceTest;
import jakarta.transaction.Transactional;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//@SpringBootTest
@Transactional
class BlankEntryFormParserServiceTest extends AbstractServiceTest {

    @Autowired
    private BlankEntryFormParserService blankEntryFormParserService;

    private static Schema tournamentSchema;

    @BeforeAll
    static void setupSchema() throws IOException {
        System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
        // Load JSON schema from classpath
        try (InputStream is = new ClassPathResource("schema/tournament_schema.json").getInputStream()) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(is));
            tournamentSchema = SchemaLoader.load(rawSchema);
        }
    }

    @Test
    @Disabled
    void testEdgeballOpenPdf() throws Exception {
        runPdfTest("pdfs/1055-24-EdgeballOpen.pdf", "Edgeball Open");
    }

    @Test
    @Disabled
    void testPongPlanetButterflyOpenPdf() throws Exception {
        runPdfTest("pdfs/1156-8-PongPlanet.pdf", "2025 PongPlanet Butterfly Open");
    }

    @Test
    @Disabled
    void testFremontTTAOpenPdf() throws Exception {
        runPdfTest("pdfs/1013-87-FremontTTA.pdf", "Fremont TTA Open");
    }

    @Test
    @Disabled
    void testOrlandoWinterOpen() throws Exception {
        runPdfTest("pdfs/1184-5-OrlandoWinterOpen.pdf", "BUTTERFLY OPEN 2025");
    }

    @Test
    @Disabled
    void testAtlantaGiantRR() throws Exception {
        runPdfTest("pdfs/1112-51AtlantaGiantRR.pdf", "2025 Atlanta Fall GRR\tTable Tennis Tournament");
    }

    @Test
    @Disabled
    void testAthensGiantRR() throws Exception {
        runPdfTest("pdfs/1112-59-Athens GA Table Tennis Fall Round Robin.pdf", "Athens GA Table Tennis Fall Round Robin");
    }

    @Test
    @Disabled
    void testButterflyAITTA() throws Exception {
        runPdfTest("pdfs/1128-22-Butterfly AITTA January 2026 OPEN.pdf", "Butterfly AITTA January 2026 OPEN");
    }

    @Test
    @Disabled
    void testCITTAFallRR() throws Exception {
        runPdfTest("pdfs/1215-2-CITTA.pdf", "2025 CITTA Fall Round Robin");
    }

    @Test
    @Disabled
    void testSouthBendRR() throws Exception {
        runPdfTest("pdfs/1092-32-2025 South Bend Open.pdf", "2025 South Bend Open");
    }

    @Test
    @Disabled
    void testWestfordRR() throws Exception {
        runPdfTest("pdfs/1142-30-Westford TTC Nov 2025 Open.pdf", "2025 South Bend Open");
    }

    @Test
    @Disabled
    void testMastiffRR() throws Exception {
        runPdfTest("pdfs/1220-2-Mastiff TTTC 2025 December Open.pdf", "Mastiff TTTC 2025 December Open");
    }

    @Test
    @Disabled
    void testGamblerNovemberOpen() throws Exception {
        runPdfTest("pdfs/1069-40-2025 Gambler CTTC Novermber Open.pdf", "2025 Gambler CTTC Novermber Open");
    }

    @Test
    @Disabled
    void testHCTTC_Circuit() throws Exception {
        runPdfTest("pdfs/1002-41-HCTTC CIRCUIT.pdf", "202511 HCTTC CIRCUIT");
    }

    @Test
    @Disabled
    void testPrincetonPongButterfly() throws Exception {
        runPdfTest("pdfs/1057-121-Princeton Pong Butterfly.pdf", "Princeton Pong Butterfly");
    }

    @Test
    @Disabled
    void testLillyYipOPen() throws Exception {
        runPdfTest("pdfs/1121-55-LYTTC November Open 2025.pdf", "LYTTC November Open 2025");
    }

    @Test
    @Disabled
    void testOklahomaOpen() throws Exception {
        runPdfTest("pdfs/1137-3-Oklahoma (Open) Table Tennis Championships.pdf", "Oklahoma (Open) Table Tennis Championships");
    }

    @Test
    @Disabled
    void testGreatLakesDataRacks() throws Exception {
        runPdfTest("pdfs/1079-10-2025 GREAT LAKES DATA RACKS & CABINETS.pdf",
                "GREAT LAKES DATA RACKS & CABINETS TABLE TENNIS");
    }

    @Test
//    @Disabled
    void testShippensburgFallOpen() throws Exception {
        runPdfTest("pdfs/1207-1-Shippensburg Fall Open.pdf",
                "Shippensburg Fall Open");
    }

    @Test
    @Disabled
    void testLobPalaceDecemberOpen() throws Exception {
        runPdfTest("pdfs/1120-11-Lob Palace December Open.pdf",
                "Lob Palace December Open");
    }

    // ======================================================================
    // Doubles only tournaments
    // ======================================================================
    @Test
    @Disabled
    void testCTTCDoubles() throws Exception {
        runPdfTest("pdfs/1115-25-CTTC Doubles.pdf",
                "DOUBLE THE FUN");  // unable to extract 'CTTC Doubles'
    }

    // ======================================================================
    //   Team tournaments
    // ======================================================================
    @Test
    @Disabled
    void testAshvilleTeams() throws Exception {
        runPdfTest("pdfs/1174-8-Asheville Fall Open.pdf", "Asheville Fall Open");
    }

    @Test
    @Disabled
    void testColumbusTeams() throws Exception {
        runPdfTest("pdfs/1115-26-COLUMBUS TABLE TENNIS TEAM INVITATIONAL.pdf",
                "COLUMBUS TABLE TENNIS TEAM INVITATIONAL");
    }

    @Test
    @Disabled
    void testSpinAndSmashTeams() throws Exception {
        runPdfTest("pdfs/1087-134-Spin & Smash 3-player Teams Tournament.pdf",
                "Spin & Smash 3-player Teams Tournament");
    }

    private void runPdfTest(String pdfPath, String tournamentNameHint) throws Exception {

        String aiOutput = blankEntryFormParserService.parseTournamentPdf(
                new ClassPathResource(pdfPath).getFile());

        JSONObject result = new JSONObject(aiOutput);
        System.out.printf("%s parsed successfully. Tournament: %s%n", pdfPath, result.optString("tournament_name", "unknown"));

        // Optional sanity checks
        assertTrue(result.optString("tournament_name").toLowerCase().contains(
                        tournamentNameHint.toLowerCase().split(" ")[0]),
                "Tournament name should roughly match hint");
        assertTrue(result.has("events"), "JSON must include events list");

        String jsonPath = pdfPath.replace("pdfs/", "expected-json/");
        jsonPath = jsonPath.replace(".pdf", ".json");
        try {
            File jsonFile = new ClassPathResource(jsonPath).getFile();
            if (jsonFile.exists() && jsonFile.length() > 0) {
                String expectedJsonContent = Files.readString(Paths.get(jsonFile.getAbsolutePath()));
                expectedJsonContent = expectedJsonContent.replaceAll("\r\n", "\n");
                if (expectedJsonContent.endsWith("\n")) {
                    expectedJsonContent = expectedJsonContent.substring(0, expectedJsonContent.length() - 1);
                }
                assertEquals(expectedJsonContent, aiOutput);
            }
        } catch (IOException e) {
            // ignore
        }
    }
}
