package com.auroratms.usatt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface RatingHistoryRecordRepository extends JpaRepository<RatingHistoryRecord, Long> {

    /**
     * Gets player rating as of given date
     *
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

    /**
     * Gets a batch of player ratings as of date
     *
     * @param membershipIdList
     * @param dateOfRating
     * @return
     */
    @Query(nativeQuery = true,
            value = "SELECT u.* " +
                    "FROM usattratinghistory u " +
                    "JOIN " +
                    " (SELECT membership_id, MAX(final_rating_date) as max_final_rating_date" +
                    " FROM usattratinghistory" +
                    " WHERE membership_id in (:membershipIdList)" +
                    " AND final_rating_date < :dateOfRating" +
                    " GROUP BY membership_id) sub " +
                    "ON sub.membership_id = u.membership_id " +
                    "AND sub.max_final_rating_date = u.final_rating_date;"
    )
    List<RatingHistoryRecord> getBatchPlayerRatingsAsOfDate(@Param("membershipIdList") List<Long> membershipIdList,
                                                            @Param("dateOfRating") Date dateOfRating);

}
