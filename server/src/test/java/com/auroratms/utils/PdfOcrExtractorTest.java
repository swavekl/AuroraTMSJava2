package com.auroratms.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

public class PdfOcrExtractorTest {

    private static String tessDataPath;

    @BeforeAll
    static void setup() {
        // Point at src/main/resources/tessdata
        tessDataPath = "src/main/resources/tessdata";

        File dir = new File(tessDataPath);
        if (!dir.exists()) {
            throw new RuntimeException("Tessdata folder missing at: " + tessDataPath);
        }
    }

    @Test
    void testOcrExtractsTournamentName() throws Exception {
        PdfOcrExtractor extractor = new PdfOcrExtractor(tessDataPath);

        File pdf = new File("src/test/resources/pdfs/1013-87-FremontTTA.pdf");
        Assertions.assertTrue(pdf.exists(), "Test PDF must exist");

        String text = extractor.extractFirstPageOcr(pdf);

        Assertions.assertNotNull(text);
        Assertions.assertFalse(text.isBlank(), "OCR returned empty text");

        // This is the field PDFBox could not extract
        Assertions.assertTrue(
                text.toLowerCase().contains("fremont"),
                "OCR text should contain tournament name"
        );

        Assertions.assertTrue(
                text.contains("2025") || text.contains("October"),
                "OCR text should extract some recognizable date elements"
        );
    }

    @Test
    void testOcrExtractsTournamentName2() throws Exception {
        PdfOcrExtractor extractor = new PdfOcrExtractor(tessDataPath);

        File pdf = new File("src/test/resources/pdfs/1002-41-HCTTC CIRCUIT.pdf");
        Assertions.assertTrue(pdf.exists(), "Test PDF must exist");

        String text = extractor.extractFirstPageOcr(pdf);

        Assertions.assertNotNull(text);
        Assertions.assertFalse(text.isBlank(), "OCR returned empty text");
        System.out.println("text = " + text);
        // This is the field PDFBox could not extract
        Assertions.assertTrue(
                text.toLowerCase().contains("hcttc circuit"),
                "OCR text should contain tournament name"
        );

        Assertions.assertTrue(
                text.contains("event") && text.contains("cost"),
                "OCR text should extract event table header"
        );
    }

    @Test
    void testOcrFailsGracefullyWithBadTessdata() throws Exception {
        PdfOcrExtractor extractor = new PdfOcrExtractor("nonexistent-folder");

        File pdf = new File("src/test/resources/pdfs/1013-87-FremontTTA.pdf");

        Exception ex = Assertions.assertThrows(Exception.class, () ->
                extractor.extractFirstPageOcr(pdf)
        );
        System.out.println("ex.getMessage() = " + ex.getMessage());
        Assertions.assertTrue(
                ex.getMessage().contains("Specified datapath nonexistent-folder/ does not exist") ||
                        ex.getMessage().contains("Failed loading language"),
                "Should throw tessdata-related error"
        );
    }

    @Test
    void testOcrReturnsMinimalTextWhenPageIsBlank() throws Exception {
        PdfOcrExtractor extractor = new PdfOcrExtractor(tessDataPath);

        File pdf = new File("src/test/resources/pdfs/blank.pdf"); // you may create this

        if (pdf.exists()) {
            String text = extractor.extractFirstPageOcr(pdf);

            Assertions.assertNotNull(text);
            Assertions.assertTrue(
                    text.length() < 20,
                    "Blank page OCR should produce very small output"
            );
        }
    }
}
