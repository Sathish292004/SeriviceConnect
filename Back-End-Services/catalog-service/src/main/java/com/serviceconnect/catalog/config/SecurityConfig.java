package com.serviceconnect.catalog.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import com.serviceconnect.catalog.security.RestAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import org.springframework.security.web.SecurityFilterChain;

import java.util.Base64;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // ============================================================
    // JWT SECRET
    // ============================================================

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String jwtIssuer;


    // ============================================================
    // JWT DECODER
    // ============================================================

    @Bean
    public JwtDecoder jwtDecoder() {

        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);

        SecretKey secretKey = new SecretKeySpec(
                keyBytes,
                "HmacSHA256"
        );

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(jwtIssuer)
        );

        return decoder;
    }


    // ============================================================
    // SECURITY FILTER CHAIN
    // ============================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
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
                // SECURITY EXCEPTION HANDLING
                // ------------------------------------------------

                .exceptionHandling(exception ->
                        exception
                                .accessDeniedHandler(
                                        accessDeniedHandler
                                )
                )

                // ------------------------------------------------
                // AUTHORIZATION
                // ------------------------------------------------

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()

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
        // USER
        // PROVIDER
        // ADMIN
        //
        // becomes:
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