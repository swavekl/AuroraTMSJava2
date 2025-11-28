package com.auroratms.utils.pdfdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the age restriction details for an event.
 */
@Data
@NoArgsConstructor
@JsonPropertyOrder({"type", "age", "restriction_date"})
public class AgeRestrictionDTO {

    /**
     * The type of age restriction (e.g., "none", "minimum", "maximum").
     */
    private String type;

    /**
     * The specific age value related to the restriction.
     */
    private String age;

    @JsonProperty("restriction_date")
    private String ageRestrictionDate;

}