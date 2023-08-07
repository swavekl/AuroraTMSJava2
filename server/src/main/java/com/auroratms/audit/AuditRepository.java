package com.auroratms.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditRepository extends JpaRepository<AuditEntity, Long> {

    List<AuditEntity> findAuditEntityByEventIdentifierAndEventTypeOrderByEventTimestampAsc(String eventIdentifier, String eventType);

}

