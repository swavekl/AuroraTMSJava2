package com.auroratms.usatt;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsattPlayerRecordRepository extends JpaRepository<UsattPlayerRecord, Long> {

    List<UsattPlayerRecord> findAllByMembershipIdIn(Iterable<Long> ids);

    List<UsattPlayerRecord> findAllByFirstNameAndLastName(String firstName, String lastName);

    List<UsattPlayerRecord> findAllByFirstNameAndLastName(String firstName, String lastName, Pageable pageable);

    UsattPlayerRecord findAllByMembershipId(Long membershipId);
}
