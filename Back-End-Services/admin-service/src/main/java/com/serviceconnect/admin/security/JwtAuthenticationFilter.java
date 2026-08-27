package com.serviceconnect.admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceconnect.admin.dto.response.ErrorResponse;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {


    private final JwtService jwtService;

    private final ObjectMapper objectMapper;


    @Override
    protected void doFilterInternal(

            HttpServletRequest request,

            HttpServletResponse response,

            FilterChain filterChain

    ) throws ServletException, IOException {


        String authorizationHeader =
                request.getHeader(
                        "Authorization"
                );


        // ========================================================
        // CHECK AUTHORIZATION HEADER
        // ========================================================

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(
                "Bearer ")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        String token =
                authorizationHeader.substring(7);


        try {


            // ====================================================
            // EXTRACT JWT CLAIMS
            // ====================================================

            Claims claims =
                    jwtService.extractClaims(
                            token
                    );


            // ====================================================
            // USER ID
            // ====================================================

            Long userId =
                    Long.valueOf(
                            claims.getSubject()
                    );


            // ====================================================
            // EMAIL
            // ====================================================

            String email =
                    claims.get(
                            "email",
                            String.class
                    );


            // ====================================================
            // ROLE
            // ====================================================

            String role =
                    claims.get(
                            "role",
                            String.class
                    );


            if (role == null
                    || role.isBlank()) {

                throw new SecurityException(
                        "JWT role is missing"
                );
            }


            // ====================================================
            // CREATE AUTHORITY
            // ====================================================

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(

                            "ROLE_" +
                                    role
                                            .trim()
                                            .toUpperCase()
                    );


            // ====================================================
            // CREATE AUTHENTICATION
            // ====================================================

            UsernamePasswordAuthenticationToken authentication =

                    new UsernamePasswordAuthenticationToken(

                            email,

                            null,

                            List.of(authority)
                    );


            // ====================================================
            // STORE USER ID
            // ====================================================

            authentication.setDetails(
                    userId
            );


            // ====================================================
            // SET SECURITY CONTEXT
            // ====================================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );


            filterChain.doFilter(
                    request,
                    response
            );


        } catch (Exception exception) {


            // ====================================================
            // INVALID JWT
            // ====================================================

            SecurityContextHolder
                    .clearContext();


            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );


            response.setContentType(
                    MediaType.APPLICATION_JSON_VALUE
            );


            ErrorResponse errorResponse =
                    new ErrorResponse(
                            401,
                            "Invalid or expired JWT token"
                    );


            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            errorResponse
                    )
            );
        }
    }
}