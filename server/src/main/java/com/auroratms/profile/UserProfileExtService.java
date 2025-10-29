package com.auroratms.profile;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@CacheConfig(cacheNames = {"user-profile-ext"}) // cached by profile id
@Transactional
public class UserProfileExtService {

    private UserProfileExtRepository repository;

    /**
     * Autowired constructor
     * @param repository
     */
    public UserProfileExtService(UserProfileExtRepository repository) {
        this.repository = repository;
    }

    @Cacheable(key = "#profileId")
    public UserProfileExt getByProfileId(String profileId) {
        return this.repository.getFirstByProfileId(profileId);
    }

    @CachePut(key = "#result.profileId")
    public UserProfileExt getByMembershipId (Long membershipId) {
        return this.repository.getFirstByMembershipId(membershipId);
    }

    public boolean existsByProfileId(String profileId) {
        return this.repository.existsUserProfileExtByProfileId(profileId);
    }

    public boolean existsByMembershipId(Long membershipId) {
        return this.repository.existsUserProfileExtByMembershipId(membershipId);
    }

    @CachePut(key = "#result.profileId")
    public UserProfileExt save(UserProfileExt userProfileExt) {
        return this.repository.save(userProfileExt);
    }

    @CacheEvict(key = "#profileId")
    public void delete (String profileId) {
        this.repository.deleteById(profileId);
        this.repository.flush();
    }

    /**
     * Finds by profile Ids and creates a map for fast lookup
     * @param profileIds
     * @return
     */
    public Map<String, UserProfileExt> findByProfileIds(List<String> profileIds) {
        List<UserProfileExt> resultList = this.repository.findAllByProfileIdIn(profileIds);
        Map<String, UserProfileExt> map = new HashMap<>();
        for (UserProfileExt userProfileExt : resultList) {
            map.put(userProfileExt.getProfileId(), userProfileExt);
        }
        return map;
    }

    /**
     * Finds all user profile exts for the members with the memberhip ids in the list
     * @param membershipIds
     * @return
     */
    public List<UserProfileExt> findByMembershipIds(List<Long> membershipIds) {
        return this.repository.findAllByMembershipIdIn(membershipIds);
    }
}
