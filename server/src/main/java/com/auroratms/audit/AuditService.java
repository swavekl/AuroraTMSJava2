package com.auroratms.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuditService {

    @Autowired
    private AuditRepository auditRepository;

    public void save (AuditEntity AuditEntity) {
        auditRepository.save(AuditEntity);
    }

    public List<AuditEntity> findByIdentifierAndType(String eventIdentifier, String eventType) {
        return auditRepository.findAuditEntityByEventIdentifierAndEventTypeOrderByEventTimestampAsc(eventIdentifier, eventType);
    }
}
