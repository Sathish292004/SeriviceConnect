package com.serviceconnect.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;

import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtProperties jwtProperties;
    private final ServerAuthenticationEntryPoint authenticationEntryPoint;
    private final ServerAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JwtProperties jwtProperties,
            ServerAuthenticationEntryPoint authenticationEntryPoint,
            ServerAccessDeniedHandler accessDeniedHandler) {

        this.jwtProperties = jwtProperties;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {

        byte[] keyBytes = Base64.getDecoder()
                .decode(jwtProperties.secret());

        SecretKeySpec key = new SecretKeySpec(
                keyBytes,
                "HmacSHA256"
        );

        NimbusReactiveJwtDecoder decoder =
                NimbusReactiveJwtDecoder
                        .withSecretKey(key)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        jwtProperties.issuer()
                )
        );

        return decoder;
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String role = jwt.getClaimAsString("role");

            if (role == null || role.isBlank()) {
                return List.of();
            }

            return List.of(
                    new SimpleGrantedAuthority(
                            "ROLE_" +
                                    role.trim().toUpperCase()
                    )
            );
        });

        return new ReactiveJwtAuthenticationConverterAdapter(
                converter
        );
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter) {

        return http

                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler
                                )
                )

                .cors(cors -> {
                })

                .headers(headers -> headers

                        .contentTypeOptions(contentTypeOptions -> {
                        })

                        .frameOptions(frameOptions ->
                                frameOptions.mode(
                                        org.springframework.security.web.server.header
                                                .XFrameOptionsServerHttpHeadersWriter.Mode.DENY
                                )
                        )

                        .referrerPolicy(referrerPolicy ->
                                referrerPolicy.policy(
                                        org.springframework.security.web.server.header
                                                .ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy
                                                .NO_REFERRER
                                )
                        )
                )

                .authorizeExchange(exchange -> exchange

                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/health/**"
                        )
                        .permitAll()

                        .pathMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/register/provider",
                                "/api/v1/auth/password/forgot",
                                "/api/v1/auth/password/reset",
                                "/api/v1/auth/verification/email/request",
                                "/api/v1/auth/verification/email/verify",
                                "/api/v1/auth/verification/phone/request",
                                "/api/v1/auth/verification/phone/verify"
                        )
                        .permitAll()

                        .pathMatchers(
                                "/api/v1/admin/**"
                        )
                        .hasRole("ADMIN")

                        .pathMatchers(
                                "/api/v1/providers/**"
                        )
                        .hasAnyRole(
                                "PROVIDER",
                                "ADMIN"
                        )

                        .pathMatchers(
                                "/api/v1/users/**"
                        )
                        .authenticated()

                        .pathMatchers(
                                "/api/v1/bookings/**"
                        )
                        .authenticated()

                        .pathMatchers(
                                "/api/v1/catalog/**"
                        )
                        .authenticated()

                        .pathMatchers(
                                "/api/v1/reviews/**"
                        )
                        .authenticated()

                        .anyExchange()
                        .authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                )

                .build();
    }
}