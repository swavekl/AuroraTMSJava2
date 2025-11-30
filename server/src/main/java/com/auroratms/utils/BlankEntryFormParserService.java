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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

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
    public String parseTournamentPdf(File pdfFile, ImportProgressInfo importProgressInfo) throws Exception {

        importProgressInfo.phaseName = "Extracting text form PDF";
        importProgressInfo.phaseCompleted = 0;
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
        importProgressInfo.phaseCompleted = 100;
        importProgressInfo.overallCompleted = 25;

        importProgressInfo.phaseName = "Analyzing tournament information";
        importProgressInfo.phaseCompleted = 0;
        String tournamentsJsonResponse = parseText(tournamentPromptFile, tournamentSchemaFile, pdfText, "gpt-4o",
                new BeanOutputConverter<>(TournamentDTO.class));
        importProgressInfo.phaseCompleted = 100;
        importProgressInfo.overallCompleted = 50;

        importProgressInfo.phaseName = "Analyzing events information";
        importProgressInfo.phaseCompleted = 0;
        String eventsJsonResponse = parseText(eventsPromptFile, eventsSchemaFile, pdfText, "gpt-4.1",
                new BeanOutputConverter<>(EventListDTO.class));

        // remove the last \n}
        String combinedResult = tournamentsJsonResponse.substring(0, tournamentsJsonResponse.length() - 2);
        combinedResult += ",\n" + eventsJsonResponse.substring(2);
        System.out.println(combinedResult);
        importProgressInfo.phaseCompleted = 100;
        importProgressInfo.overallCompleted = 90;

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
                pageText = pageText.replaceAll("\\t+", " ");
                ocrPageText = ocrPageText.replaceAll("\\t+", " ");

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

                cleanedText.append(pageText).append("\n\n");
            }
        }
        String retValue = cleanedText.toString();

        return retValue;
    }

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

