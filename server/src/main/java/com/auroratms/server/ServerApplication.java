package com.auroratms.server;

import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.text.*;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Stream;

@EnableResourceServer
@SpringBootApplication
@EnableJpaRepositories("com.auroratms")
@EntityScan("com.auroratms")
@ComponentScan("com.auroratms")
@EnableCaching
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean
    ApplicationRunner init(TournamentRepository repository) {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        return args -> {
            Stream.of("2019 Aurora Cup",
                    "2019 America's Team Championship",
                    "St. Joseph Valley Open",
                    "JOOLA Teams tournament",
                    "US Open",
                    "US Nationals",
                    "2019 Aurora Spring Open",
                    "2019 Aurora Summer Open",
                    "2019 Aurora Fall Open",
                    "Mikeljohns Veterans Tournament"
                    )
                    .forEach(name -> {
                        try {
                            Tournament tournament = new Tournament();
                            tournament.setName(name);
                            tournament.setCity("Aurora");
                            tournament.setState("IL");
                            tournament.setStartDate(dateFormat.parse("01/18/2019"));
                            tournament.setEndDate(dateFormat.parse("01/20/2019"));
                            repository.save(tournament);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    });
            repository.findAll().forEach(System.out::println);
        };
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> simpleCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
//        config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
//        config.setAllowedOrigins(Collections.singletonList("http://gateway-pc:4200"));
        config.setAllowedOrigins(Collections.singletonList("https://gateway-pc:4200"));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

//    @EnableGlobalMethodSecurity(prePostEnabled = true)
//    protected static class GlobalSecurityConfiguration extends GlobalMethodSecurityConfiguration {
//        @Override
//        protected MethodSecurityExpressionHandler createExpressionHandler() {
//            return new OAuth2MethodSecurityExpressionHandler();
//        }
//    }
//
//    @Bean
//    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
//        return  new GrantedAuthorityDefaults("");  // remove ROLE_ prefix from security roles
//    }

    @Bean
    protected ResourceServerConfigurerAdapter resourceServerConfigurerAdapter() {
        return new ResourceServerConfigurerAdapter() {

            @Override
            public void configure(HttpSecurity http) throws Exception {
                http.authorizeRequests()
                        .antMatchers("/", "/login", "/images/**", "/api/users/**").permitAll()
                        .anyRequest().authenticated();  // authenticate everything else!;
//                        .and()
//                        .oauth2ResourceServer().jwt();
                // all communication except for images over secure https channel
                http.requiresChannel()
                        .antMatchers("/images/**").requiresInsecure();
                http.requiresChannel()
                        .anyRequest().requiresSecure();

//                // disable session fixation
//                http.sessionManagement()
//                        .sessionFixation()
//                        .none();
            }
        };
    }
}
