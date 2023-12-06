package com.andersenlab.securityspringbootstarter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static com.andersenlab.securityspringbootstarter.Authority.*;
import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasAuthority;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;


@Configuration
@EnableMethodSecurity
@EnableWebSecurity
class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        return security
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(antMatcher(HttpMethod.GET,"/actuator/**")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET,"/error")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/clients")).access(hasAuthority(CLIENTS_READ.getAuthority()))
                                .requestMatchers(antMatcher(HttpMethod.POST, "/clients")).access(hasAuthority(CLIENTS_CREATE.getAuthority()))
                                .requestMatchers(antMatcher(HttpMethod.GET, "/clients/**")).access(hasAuthority(CLIENTS_READ.getAuthority()))
                                .requestMatchers(antMatcher(HttpMethod.DELETE, "/clients/**")).access(hasAuthority(CLIENTS_DELETE.getAuthority()))
                                .requestMatchers(antMatcher(HttpMethod.PUT, "/clients/check-in")).access(hasAuthority(CHECK_IN.getAuthority()))
                                .requestMatchers(antMatcher(HttpMethod.PUT, "/clients/check-out")).access(hasAuthority(CHECK_OUT.getAuthority()))
                                .requestMatchers(antMatcher(HttpMethod.GET, "/clients/stay")).access(hasAuthority(CALCULATE_PRICE.getAuthority()))
                                .requestMatchers(antMatcher(HttpMethod.GET, "/apartments")).access(hasAuthority(APARTMENTS_READ.getAuthority()))
                                .requestMatchers(antMatcher(HttpMethod.POST, "/apartments")).access(hasAuthority(APARTMENTS_CREATE.getAuthority()))
                                .requestMatchers(antMatcher(HttpMethod.GET, "/apartments/**")).access(hasAuthority(APARTMENTS_READ.getAuthority()))
                                .requestMatchers(antMatcher(HttpMethod.DELETE, "/apartments/**")).access(hasAuthority(APARTMENTS_DELETE.getAuthority()))
                                .requestMatchers(antMatcher(HttpMethod.PUT, "/apartments/adjust")).access(hasAuthority(APARTMENTS_ADJUST.getAuthority()))
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2
                                .jwt(jwtConfigurer ->
                                        jwtConfigurer
                                                .jwtAuthenticationConverter(new HotelJwtAuthenticationConverter()))
                )
                .build();
    }
}
