package com.auroratms.profile;

import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

/**
 * Service for changing principal id in the acl_sid table after user changes their
 * profile's email.  Okta doesn't support separating login id from email so we are
 * forced to do this.
 */
@DependsOnDatabaseInitialization
@Service
public class SidService extends JdbcMutableAclService {

    private final JdbcTemplate jdbcTemplate;
    private final AclCache aclCache;

    public SidService(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
        super(dataSource, lookupStrategy, aclCache);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.aclCache = aclCache;
    }

    public void updateSid(String oldUsername, String newUsername) {
        this.updateSidInternal(oldUsername, newUsername);
        // evict old principal id
        this.aclCache.clearCache();
    }

    @Transactional
    public void updateSidInternal(String oldUsername, String newUsername) {
        this.jdbcTemplate.update("update acl_sid set sid = ? where sid = ?", newUsername, oldUsername);
    }
}
