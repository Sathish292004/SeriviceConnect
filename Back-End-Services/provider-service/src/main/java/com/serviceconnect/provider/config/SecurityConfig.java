package com.serviceconnect.provider.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;
import java.util.List;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String jwtIssuer;


    // ============================================================
    // JWT DECODER
    // ============================================================

    @Bean
    public JwtDecoder jwtDecoder() {

        byte[] keyBytes =
                Base64.getDecoder()
                        .decode(jwtSecret);

        SecretKey key =
                new SecretKeySpec(
                        keyBytes,
                        "HmacSHA256"
                );

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withSecretKey(key)
                        .macAlgorithm(
                                MacAlgorithm.HS256
                        )
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        jwtIssuer
                )
        );

        return decoder;
    }


    // ============================================================
    // JWT ROLE CONVERTER
    // ============================================================

    @Bean
    public JwtAuthenticationConverter
    jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                jwt -> {

                    String role =
                            jwt.getClaimAsString(
                                    "role"
                            );

                    if (role == null
                            || role.isBlank()) {

                        return List.of();
                    }

                    return List.of(
                            new SimpleGrantedAuthority(
                                    "ROLE_" +
                                            role
                                                    .trim()
                                                    .toUpperCase()
                            )
                    );
                }
        );

        return converter;
    }


    // ============================================================
    // SECURITY FILTER CHAIN
    // ============================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

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

                .authorizeHttpRequests(auth ->
                        auth

                                .requestMatchers(
                                        "/actuator/health",
                                        "/actuator/health/**"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/api/v1/providers/**"
                                )
                                .authenticated()

                                .anyRequest()
                                .authenticated()
                )


                // ------------------------------------------------
                // JWT RESOURCE SERVER
                // ------------------------------------------------

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                )


                // ------------------------------------------------
                // DISABLE FORM LOGIN / BASIC
                // ------------------------------------------------

                .formLogin(form ->
                        form.disable()
                )

                .httpBasic(basic ->
                        basic.disable()
                );


        return http.build();
    }
}