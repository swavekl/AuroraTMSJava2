package com.auroratms.server;

import com.okta.spring.boot.oauth.Okta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

@SpringBootApplication
@EnableJpaRepositories("com.auroratms")
@EntityScan("com.auroratms")
@ComponentScan("com.auroratms")
@EnableCaching
public class ServerApplication {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> simpleCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Collections.singletonList("https://gateway-pc:4200"));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    protected WebSecurityConfigurerAdapter webSecurityConfigurerAdapter() {
        return new WebSecurityConfigurerAdapter() {

            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http.authorizeRequests()
                        .antMatchers("/", "/login", "/images/**", "/api/users/**").permitAll()
                        .anyRequest().authenticated()  // authenticate everything else!;
                        .and()
                        .oauth2ResourceServer().jwt();
                // all communication except for images over secure https channel
                http.requiresChannel()
                        .antMatchers("/images/**").requiresInsecure();
                http.requiresChannel()
                        .anyRequest().requiresSecure();

                http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
//                // disable session fixation
//                http.sessionManagement()
//                        .sessionFixation()
//                        .none();

//                http.csrf().disable();
                // enable passing back the CSRF token via cookie
                http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringAntMatchers("/api/users/login**");

                // Send a 401 message to the browser (w/o this, you'll see a blank page)
                Okta.configureResourceServer401ResponseBody(http);
            }

//            @Bean
//            public AuthoritiesExtractor oktaAuthoritiesExtractor() {
//                return new OktaAuthoritiesExtractor();
//            }
        };
    }

    /**
     * Configure decoder for decoding JWT tokens coming from Okta so we can see among other things the roles user is in
     * @return
     */
    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }

    /**
     * Okta returns user roles in a an array of 'groups' inside JWT token.
     * The default Spring security JwtGrantedAuthoritiesConverter looks for 'scp' instead of groups
     * Configure Authentication converter with a specially configured Authorities converter
     * @return
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        // configure authorities
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("groups");
        // we dont use ROLE_ for roles so make prefix empty, otherwise it would be for example ROLE_Everyone
        // see GrantedAuthorityDefaults in SecurityConfiguration
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    // initialization
    //    @Bean
//    @Transactional
//    ApplicationRunner init(TournamentRepository repository, UsattDataService usattDataService) {
//        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
//        return args -> {
////            String filename = "C:\\myprojects\\DubinaRecords.csv";
//            String filename = "C:\\myprojects\\TD Ratings File 9.26.2019.csv";
//            List<UsattPlayerRecord> usattPlayerInfos = usattDataService.readAllPlayersFromFile(filename);
//
//            usattDataService.insertPlayerData(usattPlayerInfos);
//            long count = usattDataService.getTotalCount();
//            System.out.println("count = " + count);
//
//
//            Stream.of("2019 Aurora Cup",
//                    "2019 America's Team Championship",
//                    "St. Joseph Valley Open",
//                    "JOOLA Teams tournament",
//                    "US Open",
//                    "US Nationals",
//                    "2019 Aurora Spring Open",
//                    "2019 Aurora Summer Open",
//                    "2019 Aurora Fall Open",
//                    "Mikeljohns Veterans Tournament"
//                    )
//                    .forEach(name -> {
//                        try {
//                            TournamentEntity tournament = new TournamentEntity();
//                            tournament.setName(name);
//                            tournament.setCity("Aurora");
//                            tournament.setState("IL");
//                            tournament.setStartDate(dateFormat.parse("01/18/2019"));
//                            tournament.setEndDate(dateFormat.parse("01/20/2019"));
//                            repository.save(tournament);
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                    });
////            repository.findAll().forEach(System.out::println);
//        };
//    }

}
