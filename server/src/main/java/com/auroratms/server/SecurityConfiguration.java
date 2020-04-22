package com.auroratms.server;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
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

//    @Autowired
    private CacheManager cacheManager = net.sf.ehcache.CacheManager.getInstance();

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


    //--- EHCache Configuration ---------------------------------------------//
    @Bean
    public EhCacheBasedAclCache aclCache(){
        return new EhCacheBasedAclCache(ehcache(),
                permissionGrantingStrategy(),
                aclAuthorizationStrategy()
        );
    }

    @Bean
    public PermissionGrantingStrategy permissionGrantingStrategy(){
        return new DefaultPermissionGrantingStrategy(consoleAuditLogger());
    }

    @Bean
    public Ehcache ehcache(){
        EhCacheFactoryBean cacheFactoryBean = new EhCacheFactoryBean();
        cacheFactoryBean.setCacheManager(cacheManager);
//        cacheFactoryBean.setCacheManager(cacheManager());
        cacheFactoryBean.setCacheName("aclCache");
        cacheFactoryBean.setMaxBytesLocalHeap("1M");
        cacheFactoryBean.setMaxEntriesLocalHeap(0L);
        cacheFactoryBean.afterPropertiesSet();
        return cacheFactoryBean.getObject();
    }

//    @Bean
//    public net.sf.ehcache.CacheManager cacheManager(){
//        EhCacheManagerFactoryBean cacheManager = new EhCacheManagerFactoryBean();
//        cacheManager.setAcceptExisting(true);
//        cacheManager.setCacheManagerName(CacheManager.getInstance().getName());
//        cacheManager.afterPropertiesSet();
//        return cacheManager.getObject();
//    }

    // C:\Users\Swavek\.gradle\caches\modules-2\files-2.1\org.springframework.security\spring-security-acl\5.1.1.RELEASE\8b30ed37d1062a226e3c311954a7372a2e79b2e3\spring-security-acl-5.1.1.RELEASE.jar!
    // \createAclSchemaMySQL.sql
}
