package com.serviceconnect.admin.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final String issuer;


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public JwtService(

            @Value("${security.jwt.secret}")
            String secret,

            @Value("${security.jwt.issuer}")
            String issuer) {

        this.signingKey =
                Keys.hmacShaKeyFor(
                        Base64.getDecoder()
                                .decode(secret)
                );

        this.issuer = issuer;
    }


    // ============================================================
    // EXTRACT AND VALIDATE JWT CLAIMS
    // ============================================================

    public Claims extractClaims(
            String token) {

        return Jwts.parser()

                .verifyWith(
                        signingKey
                )

                .requireIssuer(
                        issuer
                )

                .build()

                .parseSignedClaims(
                        token
                )

                .getPayload();
    }
}