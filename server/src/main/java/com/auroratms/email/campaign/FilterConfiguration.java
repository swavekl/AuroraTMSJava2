package com.auroratms.email.campaign;

import com.auroratms.sanction.SanctionRequestConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

/**
 * Small part of email campaign which we want to store in JSON
 */
@Slf4j
public class FilterConfiguration {
    // filters to apply to recipients - by 0 - all or event id.
    private List<Long> recipientFilters;

    // recipients to remove from the list
    private List<Recipient> removedRecipients;

    public List<Long> getRecipientFilters() {
        return (recipientFilters != null) ? recipientFilters : Collections.emptyList();
    }

    public void setRecipientFilters(List<Long> recipientFilters) {
        this.recipientFilters = recipientFilters;
    }

    public List<Recipient> getRemovedRecipients() {
        return (removedRecipients != null) ? removedRecipients : Collections.emptyList();
    }

    public void setRemovedRecipients(List<Recipient> removedRecipients) {
        this.removedRecipients = removedRecipients;
    }

    public String convertToJSON () {
        String content = null;
        try {
            StringWriter stringWriter = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.writeValue(stringWriter, this);
            content = stringWriter.toString();
        } catch (IOException e) {
            log.error("Error serializing filter configuration", e);
            throw new RuntimeException(e);
        }
        return content;
    }

    public static FilterConfiguration convertFromJSON(String content) {
        if (content != null) {
            try {
                // convert from JSON to configuration
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                return mapper.readValue(content,FilterConfiguration.class);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing filter configuration", e);
                throw new RuntimeException(e);
            }
        } else {
            return new FilterConfiguration();
        }
    }

    /**
     * Email recipient which should be removed after filtering
     */
    public static class Recipient {
        private String fullName;

        private String emailAddress;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }
    }

}
