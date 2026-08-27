package com.serviceconnect.auth.config;

import com.serviceconnect.auth.security.RestAccessDeniedHandler;
import com.serviceconnect.auth.security.RestAuthenticationEntryPoint;

import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;

import java.util.Base64;


@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProperties jwtProperties;


    // ============================================================
    // PASSWORD ENCODER
    // ============================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    // ============================================================
    // AUTHENTICATION MANAGER
    // ============================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }


    // ============================================================
    // JWT DECODER
    // ============================================================

    @Bean
    public JwtDecoder jwtDecoder() {

        byte[] keyBytes =
                Base64.getDecoder()
                        .decode(jwtProperties.secret());

        SecretKey key =
                Keys.hmacShaKeyFor(keyBytes);


        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withSecretKey(key)
                        .macAlgorithm(
                                MacAlgorithm.HS256
                        )
                        .build();


        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        jwtProperties.issuer()
                )
        );


        return decoder;
    }


    // ============================================================
    // JWT AUTHENTICATION CONVERTER
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

                        return java.util.List.of();
                    }


                    return java.util.List.of(
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
            JwtAuthenticationConverter jwtAuthenticationConverter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
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
                // EXCEPTION HANDLING
                // ------------------------------------------------

                .exceptionHandling(exception ->
                        exception

                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )

                                .accessDeniedHandler(
                                        accessDeniedHandler
                                )
                )


                // ------------------------------------------------
                // AUTHORIZATION
                // ------------------------------------------------

                .authorizeHttpRequests(auth -> auth


                        // Public actuator
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**"
                        )
                        .permitAll()


                        // Public authentication endpoints
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/register/provider",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh"
                        )
                        .permitAll()


                        // Authenticated authentication endpoints
                        .requestMatchers(
                                "/api/v1/auth/me",
                                "/api/v1/auth/logout"
                        )
                        .authenticated()


                        // Everything else requires JWT
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
                // DISABLE BASIC / FORM LOGIN
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