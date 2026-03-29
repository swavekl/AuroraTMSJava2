package com.auroratms.email.awssessqs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class SesNotification {

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Message")
    private String message;

    public SesMessage getUnwrappedMessage(ObjectMapper mapper) {
        try {
            return mapper.readValue(this.message, SesMessage.class);
        } catch (Exception e) {
            log.error("Failed to unwrap SNS-to-SES message", e);
            return null;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SesMessage {
        @JsonProperty("notificationType")
        private String notificationType;
        private Bounce bounce;
        private Complaint complaint; // This enables getComplaint()
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Bounce {
        private String bounceType;
        private List<Recipient> bouncedRecipients;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Complaint {
        private List<Recipient> complainedRecipients;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Recipient {
        private String emailAddress;
    }
}
