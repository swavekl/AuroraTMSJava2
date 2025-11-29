package com.auroratms.utils;

import com.auroratms.utils.pdfdto.EventListDTO;
import com.auroratms.utils.pdfdto.TournamentAndEventsDTO;
import com.auroratms.utils.pdfdto.TournamentDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * Produces a combined object with tournament and events information for easy access from clients
     * @param jsonString
     * @return
     */
    public TournamentAndEventsDTO convertToCombinedObject(String jsonString) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        // Jackson automatically handles mapping inherited fields from TournamentDTO
        // and the specific fields of TournamentAndEventsDTO.
        return objectMapper.readValue(jsonString, TournamentAndEventsDTO.class);
    }

    /**
     * Main entry point — parses a tournament blank entry PDF
     * into validated JSON according to your schema.
     */
    public String parseTournamentPdf(File pdfFile) throws Exception {

        // STEP 1: OCR FIRST PAGE (captures banners, tournament title, venue header)
        List<String> pagesTextFromOCR = ocrExtractor.extractPagesText(pdfFile);
//        System.out.println("========== first page OCR =======================");
//        System.out.println(pagesTextFromOCR);
//        System.out.println("=================================================");
//        System.out.println("pagesTextFromOCR.length() = " + pagesTextFromOCR.length());
        // Extract text from remaining pages but skip the last pages which contain standard USATT language
        String pdfText = extractTextFromPdf(pdfFile, pagesTextFromOCR);
        System.out.println("======== all input pages =======================");
        System.out.println(pdfText);
        System.out.println("=================================================");
        System.out.println("pdfText.length() = " + pdfText.length());

        String tournamentsJsonResponse = parseText(tournamentPromptFile, tournamentSchemaFile, pdfText, "gpt-4o",
                new BeanOutputConverter<>(TournamentDTO.class));

        String eventsJsonResponse = parseText(eventsPromptFile, eventsSchemaFile, pdfText, "gpt-4.1",
                new BeanOutputConverter<>(EventListDTO.class));

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
    private String parseText(Resource promptFile,
                             Resource schemaFile,
                             String pdfText,
                             String modelName,
                             BeanOutputConverter<?> beanOutputConverter) throws Exception {
        // Prepare prompt
        String promptTemplateText = null;
        try {
            // Load prompt instructions
            promptTemplateText = Files.readString(promptFile.getFile().toPath(), StandardCharsets.UTF_8);
            // Get the JSON Schema
            String output_format = getDraft7JsonSchema(beanOutputConverter.getFormat());
//            System.out.println("jsonSchemaMap = " + jsonSchemaMap);
            promptTemplateText = promptTemplateText.replace("<output_format>", output_format);
//            System.out.println("promptTemplateText\n" + promptTemplateText);
//            System.out.println("promptTemplateText.length = " + promptTemplateText.length());
        } catch (IOException e) {
            throw new RuntimeException("Unable initialize model", e);
        }

        // 2. Create the message objects
        SystemMessage systemMessage = new SystemMessage(promptTemplateText);
        UserMessage userMessage = new UserMessage(pdfText); // Your document text
//        System.out.println("pdfText.length = " + pdfText.length());

        // 3. Combine them into a List of Messages
        List<Message> messages = List.of(systemMessage, userMessage);

//        // Define the JSON Schema structure for the OpenAI API call
//        ResponseFormat format = ResponseFormat.builder()
//                .type(ResponseFormat.Type.JSON_SCHEMA)
//                .jsonSchema(ResponseFormat.JsonSchema.builder()
//                        .schema(jsonSchemaMap)
//                        .strict(true)
//                        .build())
//                .build();

        // chose model appropriate for the task
        OpenAiChatOptions chatOptions = new OpenAiChatOptions();
        chatOptions.setModel(modelName);
        chatOptions.setTemperature(0.0d);
//        chatOptions.setResponseFormat(format);

        // 4. Create the Prompt object (This is fully supported by ChatModel)
        Prompt prompt = new Prompt(messages, chatOptions);

        // Call the model
        System.out.println("Parsing text...");
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
        String jsonSchema = getDraft7JsonSchema(beanOutputConverter.getJsonSchema());
        validateAgainstSchema(jsonResponse, jsonSchema);

        return jsonResponse;
    }

    /**
     *
     * @param pdfFile
     * @param pagesTExtFromOCR
     * @return
     * @throws IOException
     */
    public String extractTextFromPdf(File pdfFile, List<String> pagesTExtFromOCR) throws IOException {
        StringBuilder cleanedText = new StringBuilder();

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(pdfFile))) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int i = 1; i <= totalPages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(document).trim();
                String ocrPageText = (i <= pagesTExtFromOCR.size()) ? pagesTExtFromOCR.get(i - 1) : "";
                // reduce multiple spaces with one
                pageText = pageText.replaceAll(" +", " ");
                ocrPageText = ocrPageText.replaceAll(" +", " ");
                pageText = pageText.replaceAll("_+", "_");
                ocrPageText = ocrPageText.replaceAll("_+", "_");

                // OCR pages may contain more text in the header graphic e.g. tournament name
                if (ocrPageText.length() > pageText.length()) {
                    pageText = ocrPageText;
                }

                // remove stock text in case it is on the same page with relevant text
                pageText = removeSafeSportsText(pageText);

                // Skip standard USATT boilerplate pages
                if (shouldSkipPage(pageText)) {
                    System.out.println("Skipping boilerplate page " + i);
                    continue;
                }
// more buggy than AI can infer
//                pageText = normalizeTimeInPage(pageText);

                cleanedText.append(pageText).append("\n\n");
            }
        }
        String retValue = cleanedText.toString();
        retValue = retValue.replaceAll("\\t+", " ");

        return retValue;
    }

//    /**
//     * Normalizes time in the entire page.
//     * @param pageText
//     * @return
//     */
//    public String normalizeTimeInPage(String pageText) {
//        StringBuilder normalizedPageText = new StringBuilder();
//        String[] linesOfText = pageText.split("\n");
//        for (String lineOfText : linesOfText) {
//            String normalized = normalizeTime(lineOfText);
//            normalizedPageText.append(normalized).append("\n");
//        }
//        return normalizedPageText.toString();
//    }

    /**
     * Remove boiler plate policy text to reduce number of tokens
     *
     * @param pageText
     * @return
     */
    private String removeSafeSportsText(String pageText) {
        String [] boilerplateTextArray = {
                "I understand USATT’s Safe Sport Policy",
                "Sport Policy",
                "Minor Athlete Abuse Prevention Policy"
        };
        for (String boilerplateText : boilerplateTextArray) {
            int start = pageText.indexOf(boilerplateText);
            if (start > 0) {
                pageText = pageText.substring(0, start);
            }
        }

        return pageText;
    }

//// --- Time Normalization Method ---
//
//    // Regex designed to capture various time formats:
//    // Group 1: Hours (h or hh or hhh, e.g., '8', '12', '830')
//    // Group 2: Separator (optional, ':' or '.')
//    // Group 3: Minutes (optional, 1 or 2 digits)
//    // Group 4: AM/PM indicator (optional, case insensitive, with or without periods)
//    private static final Pattern TIME_PATTERN = Pattern.compile(
//            "\\b(?<![a-zA-Z])(\\d{1,4})\\s*([:\\.]?)\\s*(\\d{1,2})?\\s*([ap]\\.?m?\\.?\\s*)?",
//            Pattern.CASE_INSENSITIVE
//    );
//
//    /**
//     * Normalizes a raw time string (which can be a full line of PDF text)
//     * into the standard "h:mm AM/PM" format, and replaces the original time
//     * string within the input text with the normalized version.
//     *
//     * @param rawTime The raw line of text (e.g., "Event starts at 8:30AM sharp for all singles.").
//     * @return The text with the time normalized and replaced (e.g., "Event starts at 8:30 AM sharp for all singles."),
//     * or the original string if no time is found.
//     */
//    public static String normalizeTime(String rawTime) {
//        if (rawTime == null || rawTime.trim().isEmpty()) {
//            return "";
//        }
//
//        Matcher matcher = TIME_PATTERN.matcher(rawTime);
//
//        // Variables to store the details of the CONFIRMED time (with AM/PM)
//        // OR the LAST found match (for fallback).
//        int finalMatchStart = -1;
//        int finalMatchEnd = -1;
//        String finalHoursPart = null;
//        String finalMinutesPart = null;
//        String finalAmpmsPart = null;
//        boolean confirmedTimeFound = false;
//
//        // Variables to track the last non-confirmed match (for fallback)
//        int lastMatchStart = -1;
//        int lastMatchEnd = -1;
//        String lastHoursPart = null;
//        String lastMinutesPart = null;
//        String lastAmpmsPart = null;
//
//            while (matcher.find()) {
//                String currentAmpmsPart = matcher.group(4);
//
//                // PRIORITY LOGIC: If we find an AM/PM indicator, this is the definitive time.
//                if (currentAmpmsPart != null && !currentAmpmsPart.trim().isEmpty()) {
//                    finalMatchStart = matcher.start();
//                    finalMatchEnd = matcher.end();
//                    finalHoursPart = matcher.group(1);
//                    finalMinutesPart = matcher.group(3);
//                    finalAmpmsPart = currentAmpmsPart;
//                    confirmedTimeFound = true;
//                    break; // Found the definitive time, stop searching.
//                }
//
//                // If it's not a confirmed time, store it as the last simple match
//                // (e.g., '13:00' or an ambiguous number), in case no AM/PM is ever found.
//                lastMatchStart = matcher.start();
//                lastMatchEnd = matcher.end();
//                lastHoursPart = matcher.group(1);
//                lastMinutesPart = matcher.group(3);
//                lastAmpmsPart = currentAmpmsPart;
//            }
//
//        // Determine which match details to use:
//        if (confirmedTimeFound) {
//            // Use the confirmed AM/PM match (e.g., '9am')
//            // Details are already in final variables.
//        } else if (lastMatchStart != -1) {
//            // Fallback: Use the last simple match found (e.g., '13:00' or '7' in 'Event 1 starts at 7')
//            finalMatchStart = lastMatchStart;
//            finalMatchEnd = lastMatchEnd;
//            finalHoursPart = lastHoursPart;
//            finalMinutesPart = lastMinutesPart;
//            finalAmpmsPart = lastAmpmsPart;
//        } else {
//            // No match found at all.
//            return rawTime;
//        }
//
//        // If we reach here, we have the match details to process
//        int hours = 0;
//        int minutes = 0;
//        String ampm = "";
//
//        // 1. Extract and standardize components
//
//        // Logic to handle implicit formats like "830"
//        try {
//            if (finalMinutesPart == null && finalHoursPart.length() >= 3 && finalHoursPart.length() <= 4 && matcher.group(2).isEmpty()) {
//                try {
//                    int combined = Integer.parseInt(finalHoursPart);
//                    hours = combined / 100;
//                    minutes = combined % 100;
//                } catch (NumberFormatException e) {
//                    try {
//                        hours = Integer.parseInt(finalHoursPart);
//                    } catch (NumberFormatException ignored) {
//                        System.err.println("Warning: Invalid hour format: " + finalHoursPart);
//                        return rawTime;
//                    }
//                }
//            } else {
//                // Handles explicit formats like "8", "8:30"
//                try {
//                    hours = Integer.parseInt(finalHoursPart);
//                } catch (NumberFormatException ignored) { /* Handled above */ }
//
//                if (finalMinutesPart != null) {
//                    try {
//                        minutes = Integer.parseInt(finalMinutesPart);
//                    } catch (NumberFormatException ignored) { /* Keep as 0 */ }
//                }
//            }
//        } catch (Exception e) {
//            System.out.println("rawTime " + rawTime + "\n" + e);
//        }
//
//        // 2. Normalize AM/PM indicator
//        if (finalAmpmsPart != null && !finalAmpmsPart.trim().isEmpty()) {
//            String lowerCaseAmpm = finalAmpmsPart.toLowerCase().replaceAll("[\\s\\.]", "").trim();
//            if (lowerCaseAmpm.startsWith("a")) {
//                ampm = "AM";
//            } else if (lowerCaseAmpm.startsWith("p")) {
//                ampm = "PM";
//            }
//        }
//
//        // 3. Infer AM/PM if missing (Guessing based on typical start times)
//
//        // Handle 24-hour conversion if detected and no AM/PM provided
//        if (hours > 12 && ampm.isEmpty()) {
//            hours = hours - 12; // Convert to 12-hour format
//            ampm = "PM";
//        } else if (ampm.isEmpty()) {
//            // If AM/PM is missing, guess based on 1-12 hour format
//            if (hours >= 1 && hours <= 6) {
//                ampm = "PM"; // Assume afternoon/evening (e.g., 3 PM)
//            } else if (hours >= 7 && hours <= 11) {
//                ampm = "AM"; // Assume morning (e.g., 8 AM)
//            } else if (hours == 12) {
//                ampm = "PM"; // Default 12 to 12 PM (Noon)
//            }
//        }
//
//        // Special case hour adjustments for 12
//        if (hours == 0) {
//            hours = 12; // 0 hour converts to 12 AM
//        }
//
//        // 4. Format standardization (h:mm AM/PM)
//        String standardizedMinutes = String.format("%02d", minutes);
//        String normalizedTime;
//
//        if (!ampm.isEmpty()) {
//            // If AM/PM is present, ensure hours are 1-12 (e.g., 13 -> 1, 0 -> 12)
//            int displayHours = hours;
//            if (hours > 12) {
//                displayHours = hours - 12;
//            } else if (hours == 0) {
//                displayHours = 12;
//            }
//            normalizedTime = String.format("%d:%s %s", displayHours, standardizedMinutes, ampm);
//        } else {
//            // If AM/PM was truly missing, output using the hour as extracted
//            normalizedTime = String.format("%d:%s", hours, standardizedMinutes);
//        }
//
//        String before = rawTime.substring(0, finalMatchStart);
//        String after = rawTime.substring(finalMatchEnd);
//        after = StringUtils.isEmpty(after) ? after : (" " + after);
//
//        return before + normalizedTime + after;
//    }


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
                || normalized.contains("code of conduct")
                || normalized.contains("in consideration of being permitted to participate");
    }

    /* Manual Fallback Approach */
    public String getDraft7JsonSchema(String jsonSchema) {

        // Explicitly replace the unsupported draft URI with the required Draft 7 URI
        return jsonSchema.replace(
                "https://json-schema.org/draft/2020-12/schema",
                "http://json-schema.org/draft-07/schema#"
        );
    }

    /**
     *
     * @param jsonString
     * @param jsonSchema
     * @throws Exception
     */
    private void validateAgainstSchema(String jsonString, String jsonSchema) throws Exception {
        try {
            JSONObject rawSchema = new JSONObject(new JSONTokener(jsonSchema));
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
//            System.out.println(jsonString);
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

