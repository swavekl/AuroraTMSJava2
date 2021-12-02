package com.auroratms.club;

import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.tournament.TournamentEntity;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Club service
 */
@Service
@CacheConfig(cacheNames = {"clubs"})
@Transactional
public class ClubService {

    private ClubRepository clubRepository;

    public ClubService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    public List<ClubEntity> findAllByIdIn(List<Long> clubIdsList) {
        return this.clubRepository.findAllByIdIn(clubIdsList);
    }

    @Cacheable(key = "#id")
    public ClubEntity findById(Long id) {
        return this.clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club with id " + id + " not found"));
    }

    @CachePut(key = "#result.id")
    public ClubEntity save (ClubEntity clubEntity) {
        return this.clubRepository.save(clubEntity);
    }

    @CacheEvict(key = "#id")
    public void delete (long id) {
        this.clubRepository.deleteById(id);
    }

    public Page<ClubEntity> findByNameLike(String nameContains, Pageable pageable) {
        return this.clubRepository.findAllByClubNameContainsOrAlternateClubNamesContains(nameContains, nameContains, pageable);
    }

    public List<ClubEntity> findByNameAndState(String clubName, String state) {
        return this.clubRepository.findAllByClubNameAndState(clubName, state);
    }
}
