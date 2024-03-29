package com.auroratms.clubaffiliationapp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA repository for club affiliation application
 */
public interface ClubAffiliationApplicationRepository extends JpaRepository<ClubAffiliationApplicationEntity, Long> {

    @Query(nativeQuery = true,
            value = "SELECT *" +
                    " FROM clubaffiliationapplication" +
                    " WHERE LOWER(clubaffiliationapplication.name) LIKE LOWER(:nameContains)" +
                    " AND clubaffiliationapplication.id in (" +
                    "    SELECT acl_object_identity.object_id_identity AS obj_id" +
                    "    FROM acl_object_identity," +
                    "         acl_class," +
                    "         acl_sid," +
                    "         acl_entry" +
                    "    WHERE acl_object_identity.object_id_class = acl_class.id" +
                    "      AND acl_class.class = 'com.auroratms.clubaffiliationapp.ClubAffiliationApplicationEntity'" +
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
                    " FROM clubaffiliationapplication" +
                    " WHERE LOWER(clubaffiliationapplication.name) LIKE LOWER(:nameContains)" +
                    " AND clubaffiliationapplication.id in (" +
                    "    SELECT acl_object_identity.object_id_identity AS obj_id" +
                    "    FROM acl_object_identity," +
                    "         acl_class," +
                    "         acl_sid," +
                    "         acl_entry" +
                    "    WHERE acl_object_identity.object_id_class = acl_class.id" +
                    "      AND acl_class.class = 'com.auroratms.clubaffiliationapp.ClubAffiliationApplicationEntity'" +
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
    Page<ClubAffiliationApplicationEntity> findAllCustom(
            @Param("nameContains") String nameContains,
            @Param("owner") String owner,
            @Param("authority") String authority,
            @Param("permission") Integer permission,
            Pageable pageable);

    @Query(nativeQuery = true,
            value =
                    "SELECT *" +
                    " FROM clubaffiliationapplication," +
                    "     (SELECT name AS maxname, MAX(affiliation_expiration_date) AS maxexpdate" +
                    "      FROM clubaffiliationapplication" +
                    "      WHERE LOWER(name) LIKE LOWER(:nameContains)" +
                    "      GROUP BY name) caa" +
                    " WHERE name = caa.maxname and affiliation_expiration_date = caa.maxexpdate",
            countQuery =
                    "SELECT count(*)" +
                    " FROM clubaffiliationapplication," +
                    "     (SELECT name AS maxname, MAX(affiliation_expiration_date) AS maxexpdate" +
                    "      FROM clubaffiliationapplication" +
                    "      WHERE LOWER(name) LIKE LOWER(:nameContains)" +
                    "      GROUP BY name) caa" +
                    " WHERE name = caa.maxname and affiliation_expiration_date = caa.maxexpdate"
    )
    Page<ClubAffiliationApplicationEntity> findAllByNameLikeIgnoreCaseAndMostRecentExpirationDate(String nameContains, Pageable pageable);
}
