package com.auroratms.profile;

import org.springframework.cache.annotation.CacheConfig;
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
}
