package com.auroratms.email.campaign;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

/**
 * Small part of email campaign which we want to store in JSON
 */
@Slf4j
public class FilterConfiguration implements Serializable {
    // filters to apply to recipients - by 0 - all or event id.
    private List<Long> recipientFilters;

    // recipients to remove from the list
    private List<Recipient> removedRecipients;

    // if true ignore recipient filter and get all recipients in the database
    private Boolean allRecipients;

    // if true exclude those from the list who are already registered for this tournament
    private Boolean excludeRegistered;

    // state abbreviations to filter by
    private List<String> stateFilters;

    // path of the uploaded file containing recipients last name, first name and email address
    private String uploadedRecipientsFile;

    // if true send email to uploaded recipients
    private Boolean includeUploadedRecipients;

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


    public Boolean getAllRecipients() {
        return allRecipients;
    }

    public void setAllRecipients(Boolean allRecipients) {
        this.allRecipients = allRecipients;
    }

    public Boolean getExcludeRegistered() {
        return excludeRegistered;
    }

    public void setExcludeRegistered(Boolean excludeRegistered) {
        this.excludeRegistered = excludeRegistered;
    }

    public List<String> getStateFilters() {
        return stateFilters;
    }

    public void setStateFilters(List<String> stateFilters) {
        this.stateFilters = stateFilters;
    }

    public String getUploadedRecipientsFile() {
        return uploadedRecipientsFile;
    }

    public void setUploadedRecipientsFile(String uploadedRecipientsFile) {
        this.uploadedRecipientsFile = uploadedRecipientsFile;
    }

    public Boolean isIncludeUploadedRecipients() {
        return includeUploadedRecipients;
    }

    public void setIncludeUploadedRecipients(Boolean includeUploadedRecipients) {
        this.includeUploadedRecipients = includeUploadedRecipients;
    }

    public String convertToJSON () {
        String content = null;
        try {
            StringWriter stringWriter = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.writeValue(stringWriter, this);
            content = stringWriter.toString();
            content = minify(content);
        } catch (IOException e) {
            log.error("Error serializing filter configuration", e);
            throw new RuntimeException(e);
        }
        return content;
    }

    private String minify(String content) {
        String minifiedContent = content;
        if (minifiedContent != null) {
            int lengthBefore = minifiedContent.length();
            minifiedContent = minifiedContent.replaceAll("\"lastName\"", "\"l\"");
            minifiedContent = minifiedContent.replaceAll("\"firstName\"", "\"f\"");
            minifiedContent = minifiedContent.replaceAll("\"emailAddress\"", "\"e\"");
            minifiedContent = minifiedContent.replaceAll("\"state\"", "\"s\"");
            int lengthAfter = minifiedContent.length();
            log.info("Minified filter configuration size from {} to {}", lengthBefore, lengthAfter);
        }
        return minifiedContent;
    }

    private static String deminify(String content) {
        String deminifiedContent = content;
        if (deminifiedContent != null) {
            int lengthBefore = deminifiedContent.length();
            deminifiedContent = deminifiedContent.replaceAll("\"l\"", "\"lastName\"");
            deminifiedContent = deminifiedContent.replaceAll("\"f\"", "\"firstName\"");
            deminifiedContent = deminifiedContent.replaceAll("\"e\"", "\"emailAddress\"");
            deminifiedContent = deminifiedContent.replaceAll("\"s\"", "\"state\"");
            int lengthAfter = deminifiedContent.length();
            log.info("Deminified filter configuration size from {} to {}", lengthBefore, lengthAfter);
        }
        return deminifiedContent;
    }

    public static FilterConfiguration convertFromJSON(String content) {
        if (content != null) {
            try {
                // convert from JSON to configuration
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                content = deminify(content);
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
    public static class Recipient implements Serializable {
        private String lastName;

        private String firstName;

        private String emailAddress;

        private String state;

        public String getEmailAddress() {
            return emailAddress;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Recipient recipient = (Recipient) o;

            return new EqualsBuilder().append(lastName, recipient.lastName).append(firstName, recipient.firstName).append(emailAddress, recipient.emailAddress).append(state, recipient.state).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(lastName).append(firstName).append(emailAddress).append(state).toHashCode();
        }
    }

}
