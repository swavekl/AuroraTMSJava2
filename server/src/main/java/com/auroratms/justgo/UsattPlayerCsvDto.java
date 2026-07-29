package com.auroratms.justgo;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;

import java.util.List;

@Data
public class UsattPlayerCsvDto {

    @CsvBindByName(column = "USATT Id")
    @CsvBindByPosition(position = 0)
    private String membershipId;

    @CsvBindByName(column = "First Name")
    @CsvBindByPosition(position = 1)
    private String firstName;

    @CsvBindByName(column = "Middle Name")
    @CsvBindByPosition(position = 2)
    private String middleName = "";

    @CsvBindByName(column = "Nick Name")
    @CsvBindByPosition(position = 3)
    private String nickName = "";

    @CsvBindByName(column = "Last Name")
    @CsvBindByPosition(position = 4)
    private String lastName;

    @CsvBindByName(column = "Date Of Birth")
    @CsvBindByPosition(position = 5)
    private String dateOfBirth;

    @CsvBindByName(column = "ZipCode")
    @CsvBindByPosition(position = 6)
    private String zipCode;

    @CsvBindByName(column = "Gender")
    @CsvBindByPosition(position = 7)
    private String gender;

    @CsvBindByName(column = "CityTown")
    @CsvBindByPosition(position = 8)
    private String cityTown;

    @CsvBindByName(column = "State")
    @CsvBindByPosition(position = 9)
    private String state;

    @CsvBindByName(column = "Primary Club")
    @CsvBindByPosition(position = 10)
    private String primaryClub = "";

    @CsvBindByName(column = "FinalRating")
    @CsvBindByPosition(position = 11)
    private String finalRating = "";

    @CsvBindByName(column = "League Rating")
    @CsvBindByPosition(position = 12)
    private String leagueRating = "";

    @CsvBindByName(column = "Rating As Of Date")
    @CsvBindByPosition(position = 13)
    private String ratingAsOfDate = "";

    @CsvBindByName(column = "Latest tournament play date")
    @CsvBindByPosition(position = 14)
    private String latestTournamentPlayDate = "";

    @CsvBindByName(column = "Latest Membership expiry date")
    @CsvBindByPosition(position = 15)
    private String latestMembershipExpiryDate = "";

    @CsvBindByName(column = "Latest Membership")
    @CsvBindByPosition(position = 16)
    private String latestMembership = "";

    @CsvBindByName(column = "Background Check Completed Expiry date")
    @CsvBindByPosition(position = 17)
    private String backgroundCheckExpiry = "";

    @CsvBindByName(column = "Background Check Completed Status")
    @CsvBindByPosition(position = 18)
    private String backgroundCheckStatus = "";

    @CsvBindByName(column = "Safesport Trained Expiry Date")
    @CsvBindByPosition(position = 19)
    private String safesportExpiry = "";

    @CsvBindByName(column = "Safesport Trained Status")
    @CsvBindByPosition(position = 20)
    private String safesportStatus = "";

    @CsvBindByName(column = "JustGo Id")
    @CsvBindByPosition(position = 21)
    private String justGoId = "";

    public static List<String> getHeaderOrder() {
        return List.of(
                "USATT Id", "First Name", "Middle Name", "Nick Name", "Last Name",
                "Date Of Birth", "ZipCode", "Gender", "CityTown", "State",
                "Primary Club", "FinalRating", "League Rating", "Rating As Of Date",
                "Latest tournament play date", "Latest Membership expiry date",
                "Latest Membership", "Background Check Completed Expiry date",
                "Background Check Completed Status", "Safesport Trained Expiry Date",
                "Safesport Trained Status", "JustGo Id"
        );
    }
}
