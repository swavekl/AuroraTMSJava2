package com.auroratms.usatt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface RatingHistoryRecordRepository extends JpaRepository<RatingHistoryRecord, Long> {

    /**
     * Gets player rating as of given date
     * @param membershipId
     * @param dateOfRating
     * @return
     */
    @Query(nativeQuery = true,
            value = "SELECT * FROM usattratinghistory " +
                    " WHERE membership_id = :membershipId AND final_rating_date = (" +
                    "    SELECT MAX(final_rating_date) FROM usattratinghistory" +
                    "    WHERE membership_id = :membershipId AND final_rating_date < :dateOfRating" +
                    ");"
    )
    List<RatingHistoryRecord> getPlayerRatingAsOfDate(@Param("membershipId") Long membershipId,
                                                      @Param("dateOfRating") Date dateOfRating);
}
