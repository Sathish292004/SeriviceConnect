package com.serviceconnect.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {


    // ============================================================
    // SECURITY FILTER CHAIN
    // ============================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                // ------------------------------------------------
                // CSRF
                // ------------------------------------------------

                .csrf(csrf ->
                        csrf.disable()
                )


                // ------------------------------------------------
                // STATELESS SESSION
                // ------------------------------------------------

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                // ------------------------------------------------
                // AUTHORIZATION
                // ------------------------------------------------

                .authorizeHttpRequests(auth -> auth

                        // Actuator health
                        .requestMatchers(
                                "/actuator/health"
                        ).permitAll()

                        // Everything else requires JWT
                        .anyRequest().authenticated()
                )


                // ------------------------------------------------
                // JWT RESOURCE SERVER
                // ------------------------------------------------

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }


    // ============================================================
    // JWT AUTHENTICATION CONVERTER
    // ============================================================

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken>
    jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();


        // --------------------------------------------------------
        // Read custom "role" claim from JWT
        // --------------------------------------------------------

        authoritiesConverter.setAuthoritiesClaimName(
                "role"
        );


        // --------------------------------------------------------
        // Convert:
        //
        // USER
        // PROVIDER
        // ADMIN
        //
        // into:
        //
        // ROLE_USER
        // ROLE_PROVIDER
        // ROLE_ADMIN
        // --------------------------------------------------------

        authoritiesConverter.setAuthorityPrefix(
                "ROLE_"
        );


        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();


        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );


        // --------------------------------------------------------
        // Authentication name = JWT subject
        //
        // sub = user ID
        // --------------------------------------------------------

        converter.setPrincipalClaimName(
                "sub"
        );

        return converter;
    }
}