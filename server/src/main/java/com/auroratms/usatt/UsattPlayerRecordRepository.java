package com.auroratms.usatt;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UsattPlayerRecordRepository extends JpaRepository<UsattPlayerRecord, Long> {

    UsattPlayerRecord getFirstByMembershipId(Long membershipId);

    UsattPlayerRecord getFirstByFirstNameAndLastName(String firstName, String lastName);

    List<UsattPlayerRecord> findAllByMembershipIdIn(Iterable<Long> ids);

    List<UsattPlayerRecord> findAllByFirstNameOrLastName(String firstName, String lastName, Pageable pageable);

    // query for finding next available USATT membership id in our range of 400,000 to 500,000
    @Query(nativeQuery = true,
            value = "SELECT CASE" +
                    " WHEN EXISTS(" +
                    "         SELECT 1" +
                    "         FROM usattplayerrecord" +
                    "         WHERE usattplayerrecord.membership_id >= 400000" +
                    "           AND usattplayerrecord.membership_id <= 500000" +
                    "     )" +
                    "     THEN (" +
                    "     SELECT (MAX(usattplayerrecord.membership_id) + 1)" +
                    "     FROM usattplayerrecord" +
                    "     WHERE usattplayerrecord.membership_id >= 400000" +
                    "       AND usattplayerrecord.membership_id <= 500000" +
                    " )" +
                    " ELSE 400000" +
                    " END as next_id;"
    )
    Long assignNext();


}
