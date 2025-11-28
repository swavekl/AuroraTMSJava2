package com.auroratms.utils.pdfdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the details of a prize awarded for a division in an event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"division", "place", "place_end", "prize_money", "award"})
public class PrizeDTO {

    /**
     * The division or category the prize applies to (e.g., "Main", "Consolation").
     */
    private String division;

    /**
     * The starting place for the prize (e.g., "1st", "3rd").
     */
    private String place;

    /**
     * The ending place for the prize (if a range, otherwise same as 'place').
     */
    @JsonProperty("place_end")
    private String placeEnd;

    /**
     * The monetary value of the prize.
     */
    @JsonProperty("prize_money")
    private String prizeMoney;

    /**
     * The non-monetary award (e.g., "Trophy", "Medal").
     */
    private String award;
}