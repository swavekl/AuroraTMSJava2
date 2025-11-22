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

public class PdfOcrExtractor {

    private final String tessdataPath;

    public PdfOcrExtractor(String tessdataPath) {
        this.tessdataPath = tessdataPath;
    }

    /**
     * Extracts OCR'd text from only the first page of a PDF.
     */
    public String extractFirstPageOcr(File pdfFile) throws IOException {
//        try (PDDocument document = PDDocument.load(pdfFile)) {
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(pdfFile))) {

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            // Render first page to image (300 DPI improves OCR accuracy)
            BufferedImage image = pdfRenderer.renderImageWithDPI(0, 600, ImageType.RGB);
            // Preprocess image for maximum clarity
            BufferedImage processedImage = ImagePreprocessor.preprocess(image);

            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessdataPath); // path to tessdata folder
            tesseract.setLanguage("eng");

            try {
                return tesseract.doOCR(processedImage);
            } catch (TesseractException e) {
                throw new IOException("OCR processing failed: " + e.getMessage(), e);
            }
        }
    }
}
