package com.auroratms.utils.pdfdto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"offer_name", "deadline", "entry_fee", "cancellation_fee"})
public class FeeScheduleItemDTO {
    // description of price offer - e.g. early bird special
    @JsonProperty("offer_name")
    private String offerName;

    // deadline when this offer expires
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    @JsonProperty("deadline")
    private String deadline;

    // entry fee before this deadline
    @JsonProperty("entry_fee")
    private double entryFee;

    // cancellation fee
    @JsonProperty("cancellation_fee")
    private double cancellationFee;
}
