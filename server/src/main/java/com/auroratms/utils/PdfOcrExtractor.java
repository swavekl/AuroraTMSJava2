package com.auroratms.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PdfOcrExtractor {

    private final String tessdataPath;

    public PdfOcrExtractor(String tessdataPath) {
        this.tessdataPath = tessdataPath;
    }

    /**
     * Extracts OCR'd text from all pages of a PDF using the native Tesseract CLI process.
     */
    public List<String> extractPagesText(File pdfFile) throws IOException {
        List<String> pagesOfText = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(pdfFile))) {

            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int numberOfPages = document.getNumberOfPages();
            int maxPagesToExtract = Math.min(1, document.getNumberOfPages());
            log.info("Number of pages in a PDF: " + numberOfPages + " extracting  from " + maxPagesToExtract);

            for (int pageIndex = 0; pageIndex < maxPagesToExtract; pageIndex++) {
                // Render page to image (600 DPI)
                BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 600, ImageType.RGB);
                BufferedImage processedImage = ImagePreprocessor.preprocess(image);

                // Run CLI OCR instead of Tess4J JNA
                String pageText = doCliOcr(processedImage, pageIndex);
                log.info("Extracted page text " + (pageIndex + 1) + "\n" + pageText);
                pagesOfText.add(pageText);
            }
        }
        return pagesOfText;
    }

    /**
     * Writes the BufferedImage to a temp PNG file, invokes Tesseract CLI, and reads stdout.
     */
    private String doCliOcr(BufferedImage image, int pageIndex) throws IOException {
        File tempImgFile = File.createTempFile("ocr_page_" + pageIndex + "_", ".png");
        try {
            ImageIO.write(image, "png", tempImgFile);

            // Execute CLI: tesseract <tempFile> stdout -l eng --tessdata-dir <path>
            ProcessBuilder pb = new ProcessBuilder(
                    "tesseract",
                    tempImgFile.getAbsolutePath(),
                    "stdout",
                    "-l", "eng",
                    "--tessdata-dir", tessdataPath
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();
            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Tesseract CLI failed on page " + pageIndex + " with exit code " + exitCode);
            }

            return output.toString().trim();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("OCR process interrupted for page " + pageIndex, e);
        } finally {
            if (tempImgFile.exists()) {
                tempImgFile.delete();
            }
        }
    }
}
