package com.auroratms.utils.fuzzymatch;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for performing matching of event names between those in Omnipong and those on the blank entry form
 */
@Service
public class FuzzyMatchService {

    private final ChatModel chatModel;
    private final BeanOutputConverter<List<EventMatch>> listConverter; // Handles JSON -> List<EventMatch>

    // ChatModel is autowired by Spring Boot
    public FuzzyMatchService(ChatModel chatModel) {
        this.chatModel = chatModel;

        // Instantiate the BeanOutputConverter with the complex generic type reference.
        // This is the correct way to handle List<CustomObject> in the low-level API.
        this.listConverter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<List<EventMatch>>() {}
        );
    }

    public List<EventMatch> findFuzzyMatches(List<String> listA, List<String> listB) {

        String listAText = String.join("\n- ", listA);
        String listBText = String.join("\n- ", listB);

        // 1. Construct the System Message with JSON Schema
        String systemInstruction = String.format(
                "You are a Table Tennis Data Alignment Agent. Your task is to perform a one-to-one fuzzy match " +
                        "between the event names in LIST A and LIST B based on semantic meaning and table tennis abbreviations. " +
                        "Output a strict JSON array of matched pairs. Do not include any explanation or markdown formatting." +
                        "\n\nJSON Schema your output must adhere to:\n%s",
                listConverter.getFormat() // Inject the required JSON schema from the converter
        );

        List<Message> messages = List.of(
                new SystemMessage(systemInstruction),
                new UserMessage(String.format("""
                Please perform the fuzzy match.

                --- LIST A: SOURCE NAMES ---
                - %s

                --- LIST B: TARGET NAMES ---
                - %s
                
                Find the best match for every item in LIST A within LIST B.
                """, listAText, listBText))
        );

        // 2. Model Options (GPT-4o Mini)
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4o-mini")
                .temperature(0.0d)
                .build();

        // 3. Construct the Prompt and Call the Model
        Prompt prompt = new Prompt(messages, options);

        String jsonOutput = chatModel.call(prompt).getResult().getOutput().getText();

        // 4. Convert the raw JSON string to List<EventMatch>
        return listConverter.convert(jsonOutput);
    }
}