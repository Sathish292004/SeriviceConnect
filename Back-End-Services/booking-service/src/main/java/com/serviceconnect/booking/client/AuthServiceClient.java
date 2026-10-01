package com.serviceconnect.booking.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${services.auth.url:http://localhost:8081}")
    private String authServiceUrl;

    // Cache user emails in-memory to prevent repeated inter-service calls
    private final Map<Long, String> emailCache = new ConcurrentHashMap<>();

    /**
     * Retrieves the real registered email for a user from auth-service.
     * Returns null if user is not found or auth-service is unavailable.
     */
    public String getUserEmail(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }

        // Check cache
        if (emailCache.containsKey(userId)) {
            return emailCache.get(userId);
        }

        String authorization = null;
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null && attributes.getRequest() != null) {
                authorization = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            }
        } catch (Exception ignored) {
        }

        try {
            var request = restClientBuilder
                    .baseUrl(authServiceUrl)
                    .build()
                    .get()
                    .uri("/api/v1/auth/internal/users/{userId}/email", userId);

            if (authorization != null && !authorization.isBlank()) {
                request.header(HttpHeaders.AUTHORIZATION, authorization);
            }

            Map<String, String> body = request.retrieve()
                    .body(new ParameterizedTypeReference<Map<String, String>>() {});

            if (body != null && body.containsKey("email")) {
                String email = body.get("email");
                if (email != null && !email.isBlank()) {
                    emailCache.put(userId, email.trim());
                    return email.trim();
                }
            }
        } catch (Exception ex) {
            log.warn("Could not retrieve registered email for user {} from auth-service: {}", userId, ex.getMessage());
        }

        return null;
    }
}
