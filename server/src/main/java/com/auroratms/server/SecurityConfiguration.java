package com.auroratms.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionCacheOptimizer;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;

import javax.sql.DataSource;


@Configuration
public class SecurityConfiguration {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CacheManager cacheManager;

    @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
    protected static class GlobalSecurityConfiguration extends GlobalMethodSecurityConfiguration {
        @Override
        protected MethodSecurityExpressionHandler createExpressionHandler() {
            return new OAuth2MethodSecurityExpressionHandler();
        }
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return  new GrantedAuthorityDefaults("");  // remove ROLE_ prefix from security roles
    }

    @Bean
    public AclPermissionCacheOptimizer permissionCacheOptimizer(){
        return new AclPermissionCacheOptimizer(aclService());
    }

    @Bean
    public AclPermissionEvaluator permissionEvaluator(){
        return new AclPermissionEvaluator(aclService());
    }

    @Bean (name="aclService")
    public JdbcMutableAclService aclService(){
        JdbcMutableAclService jdbcMutableAclService = new JdbcMutableAclService(dataSource,
                lookupStrategy(),
                aclCache());
        jdbcMutableAclService.setClassIdentityQuery("SELECT @@IDENTITY");
        jdbcMutableAclService.setSidIdentityQuery("SELECT @@IDENTITY");
        return jdbcMutableAclService;
    }

    @Bean
    public LookupStrategy lookupStrategy(){
        return new BasicLookupStrategy(
                dataSource,
                aclCache(),
                aclAuthorizationStrategy(),
                consoleAuditLogger());
    }

    @Bean
    public ConsoleAuditLogger consoleAuditLogger(){
        return new ConsoleAuditLogger();
    }

    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        // role which is allowed to perform administrative actions on ACLs
        return new AclAuthorizationStrategyImpl(
                new SimpleGrantedAuthority("Admins")
        );
    }

    // Use Hazelcast distributed cache for ACL cache
    @Bean
    public SpringCacheBasedAclCache aclCache() {
        final Cache aclCache = this.cacheManager.getCache("aclCache");
        return new SpringCacheBasedAclCache(aclCache,
                permissionGrantingStrategy(),
                aclAuthorizationStrategy());
    }

    @Bean
    public PermissionGrantingStrategy permissionGrantingStrategy(){
        return new DefaultPermissionGrantingStrategy(consoleAuditLogger());
    }
}
