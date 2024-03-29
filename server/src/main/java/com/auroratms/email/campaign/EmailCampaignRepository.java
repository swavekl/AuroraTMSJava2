package com.auroratms.email.campaign;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for storing email campaigns objects
 */
public interface EmailCampaignRepository extends JpaRepository<EmailCampaignEntity, Long> {

    /**
     * Finds email campaigns owned by the user and optionally filtered by nameContains
     *
     * @param nameContains
     * @param owner
     * @param authority
     * @param permission
     * @param pageable
     * @return
     */
    @Query(nativeQuery = true,
            value = "SELECT *" +
                    " FROM email_campaign" +
                    " WHERE LOWER(email_campaign.name) LIKE LOWER(:nameContains)" +
                    " AND email_campaign.id in (" +
                    "    SELECT acl_object_identity.object_id_identity AS obj_id" +
                    "    FROM acl_object_identity," +
                    "         acl_class," +
                    "         acl_sid," +
                    "         acl_entry" +
                    "    WHERE acl_object_identity.object_id_class = acl_class.id" +
                    "      AND acl_class.class = 'com.auroratms.email.campaign.EmailCampaignEntity'" +
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
                    " FROM email_campaign" +
                    " WHERE LOWER(email_campaign.name) LIKE LOWER(:nameContains)" +
                    " AND email_campaign.id in (" +
                    "    SELECT acl_object_identity.object_id_identity AS obj_id" +
                    "    FROM acl_object_identity," +
                    "         acl_class," +
                    "         acl_sid," +
                    "         acl_entry" +
                    "    WHERE acl_object_identity.object_id_class = acl_class.id" +
                    "      AND acl_class.class = 'com.auroratms.email.campaign.EmailCampaignEntity'" +
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
    Page<EmailCampaignEntity> findAllCustom(
            @Param("nameContains") String nameContains,
            @Param("owner") String owner,
            @Param("authority") String authority,
            @Param("permission") Integer permission,
            Pageable pageable);

}
