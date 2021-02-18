package com.auroratms.usatt;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsattPlayerRecordRepository extends JpaRepository<UsattPlayerRecord, Long> {

    UsattPlayerRecord getFirstByMembershipId(Long membershipId);

    UsattPlayerRecord getFirstByFirstNameAndLastName(String firstName, String lastName);

    List<UsattPlayerRecord> findAllByMembershipIdIn(Iterable<Long> ids);

    List<UsattPlayerRecord> findAllByFirstNameOrLastName(String firstName, String lastName, Pageable pageable);


}
