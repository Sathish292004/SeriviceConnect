package com.serviceconnect.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final String issuer;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer}") String issuer) {

        byte[] keyBytes = Base64.getDecoder().decode(secret);

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = issuer;
    }

    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(String token) {

        return Long.valueOf(
                extractClaims(token).getSubject()
        );
    }

    public String extractEmail(String token) {

        return extractClaims(token)
                .get("email", String.class);
    }

    public String extractRole(String token) {

        return extractClaims(token)
                .get("role", String.class);
    }

    public boolean isValid(String token) {

        try {
            extractClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}