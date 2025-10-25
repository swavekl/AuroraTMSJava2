package com.auroratms.usatt;

import java.util.List;

/**
 * small custom repository for getting membership ids or null
 */
public interface UsattPlayerRecordRepositoryCustom {

    List<Object[]> findMembershipStatus(List<String> playerFullNames);
}
