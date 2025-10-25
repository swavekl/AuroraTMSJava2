package com.auroratms.usatt;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom repository for getting players with and without membership ids
 */
@Repository
public class UsattPlayerRecordRepositoryImpl implements UsattPlayerRecordRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Object[]> findMembershipStatus(List<String> playerFullNames) {
        if (playerFullNames == null || playerFullNames.isEmpty()) {
            return List.of();
        }

        // Build dynamic UNION ALL list for MySQL 5.7
        String nameList = playerFullNames.stream()
                .map(n -> "SELECT '" + n.replace("'", "''") + "' AS full_name")
                .collect(Collectors.joining(" UNION ALL "));

        String sql =
                "SELECT n.full_name, m.membership_id, m.gender, m.city, m.state, m.zip, m.tournament_rating " +
                        "FROM (" + nameList + ") n " +
                        "LEFT JOIN usattplayerrecord m " +
                        "  ON LOWER(CONCAT(m.last_name, ', ', m.first_name)) = LOWER(n.full_name)";

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(sql).getResultList();

        return results;
    }
}
