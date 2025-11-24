package com.auroratms.tournament;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

@RepositoryRestResource
public interface TournamentRepository extends JpaRepository<TournamentEntity, Long> {

    @Query (nativeQuery = true,
            value = "SELECT *" +
                    " FROM tournament" +
                    " WHERE tournament.id in (" +
                    "    SELECT acl_object_identity.object_id_identity AS obj_id" +
                    "    FROM acl_object_identity," +
                    "         acl_class," +
                    "         acl_sid," +
                    "         acl_entry" +
                    "    WHERE acl_object_identity.object_id_class = acl_class.id" +
                    "      AND acl_class.class = 'com.auroratms.tournament.TournamentEntity'" +
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
            " FROM tournament" +
            " WHERE tournament.id in (" +
            "    SELECT acl_object_identity.object_id_identity AS obj_id" +
            "    FROM acl_object_identity," +
            "         acl_class," +
            "         acl_sid," +
            "         acl_entry" +
            "    WHERE acl_object_identity.object_id_class = acl_class.id" +
            "      AND acl_class.class = 'com.auroratms.tournament.TournamentEntity'" +
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
    Page<TournamentEntity> findWriteable(@Param("owner") String owner,
                                   @Param("authority") String authority,
                                   @Param("permission") Integer permission,
                                   Pageable pageable);

    @Query (nativeQuery = true,
            value = "SELECT *" +
                    " FROM tournament" +
                    " WHERE DATE(tournament.start_date) <= DATE(:day)" +
                    " AND DATE(:day) < DATE(ADDDATE(tournament.end_date, 1))"
    )
    Collection<TournamentEntity> findDaysTournaments(Date day);

    /**
     * Finds all tournaments which have a start date after given date
     * @param day
     * @return
     */
    Collection<TournamentEntity> findAllByStartDateAfterOrderByStartDateDesc(Date day);

    /**
     * Finds tournament by name
     * @param name
     * @return
     */
    Optional<TournamentEntity> getByName(String name);

    /**
     * Checks if exists
     * @param name
     * @return
     */
    boolean existsByName(String name);
}
