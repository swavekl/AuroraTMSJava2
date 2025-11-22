package com.auroratms.server;

import com.auroratms.utils.filerepo.IFileRepository;
import com.okta.spring.boot.oauth.Okta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.acls.AclPermissionCacheOptimizer;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.writers.CacheControlHeadersWriter;

import javax.sql.DataSource;
import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CacheManager cacheManager;

    /**
     * New Spring boot 3.0 way of configuring spring security
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        String fileRepoUrlPattern = "/" + IFileRepository.REPOSITORY_URL_ROOT + "/**/images/**";
        // for websocket handshake request we pass the JWT access token via URL parameter - access_token
        // enable retrieving access token from query parameter
        DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
        resolver.setAllowUriQueryParameter(true);

        // authentication is not required for all of these files
        // but it is required for the rest of requests
        http.authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                "/",
                                "/ui/**",
                                "/publicapi/**",
                                "/images/**",
                                "/api/users/**",
                                fileRepoUrlPattern,
                                "/index.html",
                                "/*.css",
                                "/*.css.map",
                                "/*.ico",
                                "/*.js",
                                "/*.js.map",
                                "/assets/**",
                                "/media/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(server -> server
                        .bearerTokenResolver(resolver)
                        .jwt(withDefaults()));
        // all communication except for images over secure https channel
        http.requiresChannel(channel -> channel
                .requestMatchers("/images/**", fileRepoUrlPattern)
                .requiresInsecure());
        http.requiresChannel(channel -> channel
                .anyRequest().requiresSecure());

        configureCaching(http);

        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        // enable passing back the CSRF token via cookie
        http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/users/login**", "/api/users/register**"));

        // Send a 401 message to the browser (w/o this, you'll see a blank page)
        Okta.configureResourceServer401ResponseBody(http);
        return http.build();
    }

    /**
     * Turn off caching for all but image files
     *
     * @param http
     * @throws Exception
     */
    private void configureCaching(HttpSecurity http) throws Exception {
        http.headers(headers -> headers.cacheControl(control -> control.disable()));
        http.headers(headers -> headers.addHeaderWriter(new HeaderWriter() {

            final CacheControlHeadersWriter originalWriter = new CacheControlHeadersWriter();

            @Override
            public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
                String requestURI = request.getRequestURI();
                // check if request is for one of the image files
                String[] imageFileExtensions = {".png", ".ico", ".jpeg", ".jpg"};
                String foundExtension = Arrays.stream(imageFileExtensions)
                        .filter(e -> requestURI.endsWith(e))
                        .findFirst()
                        .orElse(null);
                // if not then write no-cache in header
                if (foundExtension == null) {
                    originalWriter.writeHeaders(request, response);
                }
            }
        }));
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

    @Bean(name = "aclService")
    @DependsOnDatabaseInitialization
    public JdbcMutableAclService aclService() {
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
