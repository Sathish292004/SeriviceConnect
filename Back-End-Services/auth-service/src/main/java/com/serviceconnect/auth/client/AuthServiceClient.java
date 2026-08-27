package com.serviceconnect.auth.client;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    @Value("${auth-service.url}")
    private String authServiceUrl;


    // ============================================================
    // REST CLIENT
    // ============================================================

    private RestClient restClient() {

        return RestClient
                .builder()
                .baseUrl(authServiceUrl)
                .build();
    }


    // ============================================================
    // VERIFY SUPPORT AGENT
    // ============================================================

    public void validateSupportAgent(
            Long userId,
            String authorizationHeader) {

        if (userId == null || userId <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Agent ID is required"
            );
        }

        if (authorizationHeader == null
                || authorizationHeader.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authorization header is missing"
            );
        }


        try {

            UserRoleResponse response =
                    restClient()
                            .get()
                            .uri(
                                    "/api/v1/auth/internal/users/{userId}/role",
                                    userId
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    authorizationHeader
                            )
                            .retrieve()
                            .body(UserRoleResponse.class);


            if (response == null) {

                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                );
            }


            if (response.role() == null
                    || !"SUPPORT_AGENT".equalsIgnoreCase(
                    response.role())) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Selected user is not a support agent"
                );
            }


        } catch (ResponseStatusException exception) {

            throw exception;

        } catch (RestClientException exception) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Auth service is currently unavailable"
            );
        }
    }


    // ============================================================
    // RESPONSE
    // ============================================================

    private record UserRoleResponse(

            Long id,

            String role

    ) {
    }
}