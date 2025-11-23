package com.auroratms.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class BlankEntryFormParserService {

    private final ChatModel chatModel;

    private final PdfOcrExtractor ocrExtractor;

    @Value("classpath:schema/tournament_schema.json")
    private Resource tournamentSchemaFile;

    @Value("classpath:schema/events_schema.json")
    private Resource eventsSchemaFile;

    @Value("classpath:prompt/prompt_instructions_tournament.txt")
    private Resource tournamentPromptFile;

    @Value("classpath:prompt/prompt_instructions_events.txt")
    private Resource eventsPromptFile;

    public BlankEntryFormParserService(ChatModel chatModel,
                                       @Value("${tess4j.tessdata-path}") String tessDataPath) {
        this.chatModel = chatModel;
        this.ocrExtractor = new PdfOcrExtractor(tessDataPath);
    }

    /**
     * Main entry point — parses a tournament blank entry PDF
     * into validated JSON according to your schema.
     */
    public String parseTournamentPdf(File pdfFile) throws Exception {

        // STEP 1: OCR FIRST PAGE (captures banners, tournament title, venue header)
        String ocrFirstPage = ocrExtractor.extractFirstPageOcr(pdfFile);
        System.out.println("========== first page OCR =======================");
        System.out.println(ocrFirstPage);
        System.out.println("=================================================");
        System.out.println("ocrFirstPage.length() = " + ocrFirstPage.length());
        // Extract text from remaining pages but skip the last pages which contain stanadard USATT language
        String pdfText = extractTextFromPdf(pdfFile, ocrFirstPage);
        System.out.println("======== all pages =======================");
        System.out.println(pdfText);
        System.out.println("=================================================");
        System.out.println("pdfText.length() = " + pdfText.length());

//        pdfText = ocrFirstPage + pdfText;

        String tournamentsJsonResponse = parseText(tournamentPromptFile, tournamentSchemaFile, pdfText, "gpt-4o");

        String eventsJsonResponse = parseText(eventsPromptFile, eventsSchemaFile, pdfText, "gpt-4.1");

        // remove the last \n}
        String combinedResult = tournamentsJsonResponse.substring(0, tournamentsJsonResponse.length() - 2);
        combinedResult += ",\n" + eventsJsonResponse.substring(2);
        System.out.println(combinedResult);

        return combinedResult;
    }

    /**
     *
     * @param promptFile prompt file to use for the chat model
     * @param schemaFile JSON schema file to use for validation
     * @param pdfText    text to be parsed by the chat model
     * @return extracted JSON
     * @throws Exception
     */
    private String parseText(Resource promptFile, Resource schemaFile, String pdfText, String modelName) throws Exception {
        // Prepare prompt
        String promptTemplateText = null;
        try {
            // Load prompt instructions
            promptTemplateText = Files.readString(promptFile.getFile().toPath(), StandardCharsets.UTF_8);
            System.out.println("promptTemplateText.length = " + promptTemplateText.length());
        } catch (IOException e) {
            throw new RuntimeException("Unable initialize model", e);
        }
        String promptText = promptTemplateText.replace("<pdf_text>", pdfText);
        // chose model appropriate for the task
        OpenAiChatOptions chatOptions = new OpenAiChatOptions();
        chatOptions.setModel(modelName);
        chatOptions.setTemperature(0.2d);
//        chatOptions.setResponseFormat(ResponseFormat.builder()
//                .type(ResponseFormat.Type.JSON_SCHEMA)
//                .build());
        Prompt prompt = new Prompt(promptText, chatOptions);
//        Prompt prompt = new Prompt(promptText);
        System.out.println("promptText.length = " + promptText.length());

        // Call the model
        long start = System.currentTimeMillis();
        String jsonResponse = chatModel.call(prompt).getResult().getOutput().getText();

        long duration = System.currentTimeMillis() - start;
        System.out.println("duration secs = " + (duration / 1000));
//        System.out.println(jsonResponse);
        if (jsonResponse != null) {
            // remove markdown fencing
            jsonResponse = jsonResponse
                    .replaceAll("^```json", "")
                    .replaceAll("^```", "")
                    .replaceAll("```$", "")
                    .trim();
        }

        // Validate JSON structure
        validateAgainstSchema(schemaFile, jsonResponse);

        return jsonResponse;

    }

    /**
     *
     * @param pdfFile
     * @param ocrFirstPage
     * @return
     * @throws IOException
     */
    public String extractTextFromPdf(File pdfFile, String ocrFirstPage) throws IOException {
        StringBuilder cleanedText = new StringBuilder();

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(pdfFile))) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int i = 1; i <= totalPages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(document).trim();
                if (i == 1) {
                    // first page contains more
                    if (ocrFirstPage.length() < pageText.length()) {
                        cleanedText.append(pageText).append("\n\n");
                    } else {
                        cleanedText.append(ocrFirstPage).append("\n\n");
                    }
                    continue;
                }

                // remove stock text in case it is on the same page with relevant text
                pageText = removeSafeSportsText(pageText);

                // Skip standard USATT boilerplate pages
                if (shouldSkipPage(pageText)) {
//                    System.out.println("Skipping boilerplate page " + i);
                    continue;
                }

                cleanedText.append(pageText).append("\n\n");
            }
        }
        String retValue = cleanedText.toString();
        retValue = retValue.replaceAll("\\t+", " ");

        return retValue;
    }

    /**
     * Remove boiler plate policy text to reduce number of tokens
     * @param pageText
     * @return
     */
    private String removeSafeSportsText(String pageText) {
        int start = pageText.indexOf("I understand USATT’s Safe Sport Policy");
        if (start > 0) {
            pageText = pageText.substring(0, start);
        }
        return pageText;
    }

    /**
     * Determines whether a given page should be excluded before sending to the LLM.
     */
    private boolean shouldSkipPage(String pageText) {
        if (pageText == null || pageText.isEmpty()) return false;

        String normalized = pageText.toLowerCase();
        normalized = normalized.replaceAll("\\t+", " ");

        return normalized.contains("usatt safe sport protocol")
                || normalized.contains("communication of safe sport policy to tournament participants")
                || normalized.contains("waiver of liability")
                || normalized.contains("code of conduct");
    }

    /**
     *
     * @param schemaFile
     * @param jsonString
     * @throws Exception
     */
    private void validateAgainstSchema(Resource schemaFile, String jsonString) throws Exception {
        try (InputStream schemaStream = schemaFile.getInputStream()) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(schemaStream));
            Schema schema = SchemaLoader.load(rawSchema);
            JSONObject data = new JSONObject(jsonString);
            schema.validate(data);
        } catch (ValidationException e) {
            System.out.println("JSON Schema Validation Failed:");
            System.out.println("Summary: " + e.getMessage());
            System.out.println("Keyword: " + e.getKeyword());
            System.out.println("Pointer: " + e.getPointerToViolation());

            System.out.println("\nAll error messages:");
            e.getAllMessages().forEach(msg -> System.out.println(" - " + msg));

            System.out.println("\nDetailed tree:");
            printValidationErrors(e, "");
            System.out.println(jsonString);
        } catch (JSONException e) {
            System.out.println(jsonString);
            System.out.println("Summary: " + e.getMessage());
        }
    }

    /**
     *
     * @param e
     * @param indent
     */
    private void printValidationErrors(ValidationException e, String indent) {
        System.out.println(indent + "* " + e.getMessage() + " at " + e.getPointerToViolation());
        for (ValidationException cause : e.getCausingExceptions()) {
            printValidationErrors(cause, indent + "  ");
        }
    }
}

