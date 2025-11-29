package com.auroratms.utils.pdfdto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy", timezone = "UTC")
    @JsonProperty("restriction_date")
    private Date ageRestrictionDate;

}