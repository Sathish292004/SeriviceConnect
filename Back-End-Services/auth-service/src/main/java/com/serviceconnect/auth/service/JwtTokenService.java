package com.serviceconnect.auth.service;

import com.serviceconnect.auth.config.JwtProperties;
import com.serviceconnect.auth.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    private SecretKey signingKey() {

        return Keys.hmacShaKeyFor(
                Base64.getDecoder()
                        .decode(jwtProperties.secret())
        );
    }

    public String generateAccessToken(User user) {

        Date issuedAt = new Date();

        Date expiration = new Date(
                issuedAt.getTime()
                        + jwtProperties.accessTokenExpiration()
        );

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuer(jwtProperties.issuer())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .id(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .signWith(signingKey())
                .compact();
    }
}