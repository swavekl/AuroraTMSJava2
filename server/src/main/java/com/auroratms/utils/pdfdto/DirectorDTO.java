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
@JsonPropertyOrder({"name", "phone", "email"})
public class DirectorDTO {
    // 'name' and 'phone' are required in your schema for a director:
    @JsonProperty("name")
    private String name;

    @JsonProperty("phone")
    private String phone;

    // 'email' is NOT required, so it is handled correctly by NON_NULL
    @JsonProperty("email")
    private String email;
}