package com.auroratms.utils.pdfdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"name", "rank"})
public class OfficialDTO {

    // Both fields are marked as 'required' for each official item:
    @JsonProperty("name")
    private String name;

    @JsonProperty("rank")
    private String rank;

    // Add constructors, getters, and setters here
}