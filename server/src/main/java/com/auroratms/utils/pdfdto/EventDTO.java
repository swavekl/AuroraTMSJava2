package com.auroratms.utils.pdfdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a single tournament event, including its rules and prizes.
 */
@Data
@NoArgsConstructor
@JsonPropertyOrder({
        "ordinal_number",
        "event_name",
        "day",
        "start_time",
        "entry_fee",
        "is_doubles",
        "max_entries",
        "single_elimination",
        "max_rating",
        "gender_restriction",
        "age_restriction",
        "players_per_group",
        "draw_method",
        "tables_per_group",
        "number_of_games",
        "number_of_games_se_playoffs",
        "number_of_games_se_quarter_finals",
        "number_of_games_se_semi_finals",
        "number_of_games_se_finals",
        "play_3rd_4th_place",
        "players_to_advance",
        "advance_unrated_player",
        "players_to_seed",
        "prizes"
})
public class EventDTO {

    @JsonProperty("ordinal_number")
    private int ordinalNumber;

    @JsonProperty("event_name")
    private String eventName;

    private int day = 1;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("entry_fee")
    private String entryFee;

    @JsonProperty("is_doubles")
    private boolean isDoubles;

    // maximum entries, 0 if no limit
    @JsonProperty("max_entries")
    private int maxEntries;

    @JsonProperty("single_elimination")
    private boolean singleElimination;

    @JsonProperty("max_rating")
    private String maxRating;

    @JsonProperty("gender_restriction")
    private String genderRestriction; // Enum: "NONE", "MALE", "FEMALE"

    @JsonProperty("age_restriction")
    private AgeRestrictionDTO ageRestriction;

    @JsonProperty("players_per_group")
    private int playersPerGroup;

    @JsonProperty("draw_method")
    private String drawMethod;

    @JsonProperty("tables_per_group")
    private int numTablesPerGroup = 1;

    // best of 3, 5, 7 or 9 games per match in the main round (i.e. round robin)
    @JsonProperty("number_of_games")
    private int numberOfGames;

    // in single elimination round or if event is a single elimination only
    // number of games in rounds prior to quarter finals e.g. 5
    @JsonProperty("number_of_games_se_playoffs")
    private int numberOfGamesSEPlayoffs = 5;

    // number of games in quarter, semi finals and 3rd/4th place matches
    @JsonProperty("number_of_games_se_quarter_finals")
    private int numberOfGamesSEQuarterFinals = 5;

    @JsonProperty("number_of_games_se_semi_finals")
    private int numberOfGamesSESemiFinals = 5;

    @JsonProperty("number_of_games_se_finals")
    private int numberOfGamesSEFinals = 5;

    @JsonProperty("play_3rd_4th_place")
    private boolean play3rd4thPlace;

    @JsonProperty("players_to_advance")
    private int playersToAdvance = 1;

    @JsonProperty("advance_unrated_player")
    private boolean advanceUnratedWinner = false;

    @JsonProperty("players_to_seed")
    private int playersToSeed;

    private List<PrizeDTO> prizes;
}
