package com.auroratms.officials;

import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class OfficialService {

    @Autowired
    private OfficialRepository repository;

    @Autowired
    private UserProfileExtService userProfileExtService;

    Page<Official> list(Pageable pageable) {
        Page<Official> page = repository.findAll(pageable);
        fillMembershipIds(page.getContent());
        return page;
    }

    public Page<Official> findByFirstNameLikeOrLastNameLike(String firstNameLike, String lastNameLike, Pageable pageable) {
        Page<Official> page = repository.findAllByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(firstNameLike, lastNameLike, pageable);
        fillMembershipIds(page.getContent());
        return page;
    }

    public Page<Official> findByFirstNameLikeOrLastNameLikeAndState(String firstNameLike, String lastNameLike, String state, Pageable pageable) {
        Page<Official> page = repository.findAllByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCaseAndState(firstNameLike, lastNameLike, state, pageable);
        fillMembershipIds(page.getContent());
        return page;
    }

    public Page<Official> findByState(String state, Pageable pageable) {
        Page<Official> page = repository.findAllByState(state, pageable);
        fillMembershipIds(page.getContent());
        return page;
    }

    public Official save(Official official) {
        return repository.save(official);
    }

    public Official findById(Long officialId) {
        Official official = this.repository.findById(officialId)
                .orElseThrow(() -> new ResourceNotFoundException("Official with id " + officialId + " not found"));
        fillMembershipIds(Collections.singletonList(official));
        return official;
    }

    private void fillMembershipIds(List<Official> officialsList) {
        if (officialsList.size() > 0) {
            List<String> profileIds = new ArrayList<>(officialsList.size());
            for (Official official : officialsList) {
                profileIds.add(official.getProfileId());
            }
            Map<String, UserProfileExt> profileExtMap = userProfileExtService.findByProfileIds(profileIds);
            for (Official official : officialsList) {
                UserProfileExt userProfileExt = profileExtMap.get(official.getProfileId());
                if (userProfileExt != null) {
                    official.setMembershipId(userProfileExt.getMembershipId());
                }
            }
        }
    }

    public void delete(Long officialId) {
        this.repository.deleteById(officialId);
    }

}
