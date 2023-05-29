package com.auroratms.insurance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InsuranceRequestRepository extends JpaRepository<InsuranceRequest, Long> {
    @Query(nativeQuery = true,
            value = "SELECT *" +
                    " FROM insurance_request" +
                    " WHERE insurance_request.event_name LIKE :nameContains" +
                    " AND insurance_request.id in (" +
                    "    SELECT acl_object_identity.object_id_identity AS obj_id" +
                    "    FROM acl_object_identity," +
                    "         acl_class," +
                    "         acl_sid," +
                    "         acl_entry" +
                    "    WHERE acl_object_identity.object_id_class = acl_class.id" +
                    "      AND acl_class.class = 'com.auroratms.insurance.InsuranceRequest'" +
                    "      AND ((acl_sid.id = acl_object_identity.owner_sid" +
                    "        AND acl_sid.sid = :owner)" +
                    "        OR (acl_sid.id = acl_entry.sid" +
                    "            AND acl_object_identity.id = acl_entry.acl_object_identity" +
                    "            AND ((acl_sid.sid = :authority " +
                    "            AND acl_sid.principal = 0)" +
                    "            OR  (acl_sid.sid = :owner" +
                    "            AND acl_sid.principal = 1))" +
                    "            AND acl_entry.mask = :permission" +
                    "            AND acl_entry.granting = 1))" +
                    ")",
            countQuery = "SELECT count(*)" +
                    " FROM insurance_request" +
                    " WHERE insurance_request.event_name LIKE :nameContains" +
                    " AND insurance_request.id in (" +
                    "    SELECT acl_object_identity.object_id_identity AS obj_id" +
                    "    FROM acl_object_identity," +
                    "         acl_class," +
                    "         acl_sid," +
                    "         acl_entry" +
                    "    WHERE acl_object_identity.object_id_class = acl_class.id" +
                    "      AND acl_class.class = 'com.auroratms.insurance.InsuranceRequest'" +
                    "      AND ((acl_sid.id = acl_object_identity.owner_sid" +
                    "        AND acl_sid.sid = :owner)" +
                    "        OR (acl_sid.id = acl_entry.sid" +
                    "            AND acl_object_identity.id = acl_entry.acl_object_identity" +
                    "            AND ((acl_sid.sid = :authority " +
                    "            AND acl_sid.principal = 0)" +
                    "            OR  (acl_sid.sid = :owner" +
                    "            AND acl_sid.principal = 1))" +
                    "            AND acl_entry.mask = :permission" +
                    "            AND acl_entry.granting = 1))" +
                    ")"
    )
    Page<InsuranceRequest> findAllCustom(
            @Param("nameContains") String nameContains,
            @Param("owner") String owner,
            @Param("authority") String authority,
            @Param("permission") Integer permission,
            Pageable pageable);

}
