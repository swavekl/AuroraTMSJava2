package com.auroratms.tournamententry;

import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.users.UserRolesHelper;
import com.auroratms.utils.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@CacheConfig (cacheNames = {"tournament-entries"})
@Transactional
public class TournamentEntryService {
    
    @Autowired
    TournamentEntryRepository repository;

    @Autowired
    TournamentService tournamentService;

    @Autowired
    SecurityService securityService;

    @Autowired
    private UserProfileService userProfileService;

    @CachePut(key = "#result.id")
    public TournamentEntry create(TournamentEntry tournamentEntry) {
        TournamentEntry savedTournamentEntry = repository.save(tournamentEntry);
        securityService.provideAccessToAdmin(savedTournamentEntry.getId(), TournamentEntry.class);
        String ownerId = tournamentService.getTournamentOwner(tournamentEntry.getTournamentFk());
        securityService.provideAccessToTournamentDirector(savedTournamentEntry.getId(), ownerId, TournamentEntry.class);

        // if tournament director is making an entry on behalf of the user then we need to set
        // the owner of this entry to the player who is registering.
        String currentUserName = UserRolesHelper.getCurrentUsername();
        String profileByLoginId = userProfileService.getProfileByLoginId(currentUserName);
        if (!tournamentEntry.profileId.equals(profileByLoginId)) {
            UserProfile entrantProfile = userProfileService.getProfile(tournamentEntry.profileId);
            String entrantLoginId = entrantProfile.getLogin();
            securityService.changeOwner(savedTournamentEntry.getId(), TournamentEntry.class, entrantLoginId);
        }
        return savedTournamentEntry;
    }

    @Cacheable(key = "#entryId")
    public TournamentEntry get(Long entryId) {
        return repository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("TournamentEntry " + entryId + " not found"));
    }

    /**
     *
     * @param tournamentId
     * @param profileId
     * @return
     */
    public List<TournamentEntry> listForTournamentAndUser(Long tournamentId, String profileId) {
        List<TournamentEntry> entries = repository.findByTournamentFkAndProfileId(tournamentId, profileId);
        if (entries.size() > 0) {
            cacheIt(entries.get(0));
        }
        return entries;
    }

    /**
     *
     * @param profileId
     * @return
     */
    public List<TournamentEntry> listForUser(String profileId) {
        List<TournamentEntry> entries = repository.findAllByProfileId(profileId);
        if (entries.size() > 0) {
            cacheIt(entries.get(0));
        }
        return entries;
    }

    /**
     *
     * @param profileId
     * @param date
     * @return
     */
    public List<TournamentEntry> listTodaysTournamentForUser(String profileId, Date date){
        List<TournamentEntry> entries = Collections.emptyList();
        // find all tournaments which are played today
        Collection<Tournament> todaysTournaments = tournamentService.listDaysTournaments(date);
        List<Long> tournamentIdList = new ArrayList<>(todaysTournaments.size());
        for (Tournament tournament : todaysTournaments) {
            tournamentIdList.add(tournament.getId());
        }
        // find which of today's tournament were entered by this player
        if (todaysTournaments.size() > 0) {
            entries = repository.findByTournamentFkInAndProfileId(tournamentIdList, profileId);
            if (entries.size() > 0) {
                cacheIt(entries.get(0));
            }
        }
        return entries;
    }

    /**
     * Finds all entries for tournament
     * @param tournamentId
     * @return
     */
    public List<TournamentEntry> listForTournament(Long tournamentId) {
        return repository.findAllByTournamentFk(tournamentId);
    }

    /**
     * Gets a subset of tournament entries identified by ids in the list
     * @param tournamentEntryIds ids of entries to get
     * @return
     */
    public List<TournamentEntry> listEntries(List<Long> tournamentEntryIds) {
        return repository.findAllByIdIn(tournamentEntryIds);
    }

    @CachePut(key = "#entry.id")
    public void cacheIt (TournamentEntry entry) {
        // do nothing just cache it
    }

    @CachePut(key = "#result.id")
    public TournamentEntry update(TournamentEntry TournamentEntry) {
        if (repository.existsById(TournamentEntry.getId())) {
            return repository.save(TournamentEntry);
        } else {
            throw new ResourceNotFoundException("TournamentEntry " + TournamentEntry.getId() + " not found");
        }
    }

    @CacheEvict(key = "#id")
    public void delete(long id) {
        repository.deleteById(id);
    }

    /**
     * Gets count of entries for the tournament
     * @param tournamentId
     * @return
     */
    public int getCountOfEntries(long tournamentId) {
        return repository.countTournamentEntryByTournamentFkEquals(tournamentId);
    }

    public List<Long> findAllEntryIds (long tournamentId) {
        return repository.findAllEntryIds(tournamentId);
    }
}
