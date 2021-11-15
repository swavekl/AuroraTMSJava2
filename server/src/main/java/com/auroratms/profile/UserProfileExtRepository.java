package com.auroratms.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface UserProfileExtRepository extends JpaRepository<UserProfileExt, String> {

    UserProfileExt getFirstByMembershipId(Long membershipId);

    UserProfileExt getFirstByProfileId(String profileId);

    boolean existsUserProfileExtByProfileId(String profileId);

    boolean existsUserProfileExtByMembershipId(Long membershipId);

    List<UserProfileExt> findAllByProfileIdIn(List<String> profileIds);
}
