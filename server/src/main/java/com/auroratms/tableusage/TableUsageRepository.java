package com.auroratms.tableusage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TableUsageRepository extends JpaRepository<TableUsage, Long> {

    /**
     * lists table usage for the specified tournament
     * @param tournamentFk
     * @return
     */
    List<TableUsage> findAllByTournamentFkOrderByTableNumber(long tournamentFk);

    /**
     * Finds all table usages associated with given match card
     * @param matchCardFk
     * @return
     */
    List<TableUsage> findAllByMatchCardFk(long matchCardFk);
}
