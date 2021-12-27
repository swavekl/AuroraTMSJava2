package com.auroratms.tableusage;

import com.auroratms.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing table usage
 */
@Service
@CacheConfig(cacheNames = {"tableusage"})
@Transactional
public class TableUsageService {

    @Autowired
    private TableUsageRepository repository;

    /**
     * Creates table status entries for all tables of the tournament
     *
     * @param tournamentFk
     */
    public List<TableUsage> create(long tournamentFk, int numberOfTables) {
        // find out how many tables are already configured and add those that are not
        List<TableUsage> existingTables = this.repository.findAllByTournamentFkOrderByTableNumber(tournamentFk);
        List<TableUsage> tablesToCreate = new ArrayList<>();
        for (int tableNumber = 1; tableNumber <= numberOfTables; tableNumber++) {
            boolean found = false;
            for (TableUsage tableUsage : existingTables) {
                if (tableUsage.getTableNumber() == tableNumber) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                TableUsage tableUsage = new TableUsage();
                tableUsage.setTournamentFk(tournamentFk);
                tableUsage.setTableNumber(tableNumber);
                tableUsage.setMatchCardFk(0L);
                tablesToCreate.add(tableUsage);
            }
        }

        this.repository.saveAllAndFlush(tablesToCreate);

        // delete extra tables that are not at the tournament
        for (TableUsage existingTable : existingTables) {
            if (existingTable.getTableNumber() > numberOfTables) {
                this.repository.deleteById(existingTable.getId());
            }
        }

        return list(tournamentFk);
    }

    /**
     * Lists table status for this tournament
     * @param tournamentFk
     * @return
     */
    public List<TableUsage> list(long tournamentFk) {
        return repository.findAllByTournamentFkOrderByTableNumber(tournamentFk);
    }

    /**
     *
     * @param tableUsageId
     * @return
     */
    @Cacheable(key = "#id")
    public TableUsage findById (long tableUsageId) {
        return repository.findById(tableUsageId)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find table usage with id " + tableUsageId));
    }

    /**
     *
     * @param tableUsage
     * @return
     */
    @CachePut(key = "#result.id")
    public TableUsage update(TableUsage tableUsage) {
        TableUsage savedTableUsage = null;
        try {
            savedTableUsage = repository.saveAndFlush(tableUsage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return savedTableUsage;
    }

    /**
     *
     * @param tableUsageList
     */
    public List<TableUsage> updateAll(List<TableUsage> tableUsageList) {
        return repository.saveAll(tableUsageList);
    }

    @CacheEvict(key = "#id")
    public void delete (long id) {
        repository.deleteById(id);
    }

}
