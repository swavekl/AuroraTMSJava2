package com.auroratms.club;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Club service
 */
@Service
public class ClubService {

    private ClubRepository clubRepository;

    public ClubService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    public List<ClubEntity> findAllByIdIn(List<Long> clubIdsList) {
        return this.clubRepository.findAllByIdIn(clubIdsList);
    }

    public ClubEntity save (ClubEntity clubEntity) {
        return this.clubRepository.save(clubEntity);
    }

    public void delete (long id) {
        this.clubRepository.deleteById(id);
    }

}
