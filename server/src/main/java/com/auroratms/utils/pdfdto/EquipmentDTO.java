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
@JsonPropertyOrder({"tables", "ball_type"})
public class EquipmentDTO {

    // Both fields are marked as 'required':
    @JsonProperty("tables")
    private String tables;

    @JsonProperty("ball_type")
    private String ballType;

    // Add constructors, getters, and setters here
}