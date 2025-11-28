package com.auroratms.utils;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfOcrExtractor {

    private final String tessdataPath;

    public PdfOcrExtractor(String tessdataPath) {
        this.tessdataPath = tessdataPath;
    }

    /**
     * Extracts OCR'd text from only the first page of a PDF.
     */
    public List<String> extractPagesText(File pdfFile) throws IOException {
        List<String> pagesOfText = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(pdfFile))) {

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            int numberOfPages = document.getNumberOfPages();
            for (int pageIndex = 0; pageIndex < numberOfPages; pageIndex++) {
                // Render first page to image (300 DPI improves OCR accuracy)
                BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 600, ImageType.RGB);
                // Preprocess image for maximum clarity
                BufferedImage processedImage = ImagePreprocessor.preprocess(image);

                Tesseract tesseract = new Tesseract();
                tesseract.setDatapath(tessdataPath); // path to tessdata folder
                tesseract.setLanguage("eng");

                try {
                    String pageText = tesseract.doOCR(processedImage);
                    pagesOfText.add(pageText);
                } catch (TesseractException e) {
                    throw new IOException("OCR processing failed for page " + pageIndex + ": " + e.getMessage(), e);
                }
            }

        }
        return pagesOfText;
    }
}
