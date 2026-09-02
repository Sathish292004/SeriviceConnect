package com.serviceconnect.auth.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    private final RestClient restClient;


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public UserServiceClient(
            @Value("${services.user.url}") String userServiceUrl) {

        this.restClient =
                RestClient.builder()
                        .baseUrl(userServiceUrl)
                        .build();
    }


    // ============================================================
    // DELETE USER PROFILE
    // ============================================================

    public void deleteUserProfile(
            Long userId,
            String authorizationHeader) {

        restClient
                .delete()
                .uri(
                        "/api/users/{id}",
                        userId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .toBodilessEntity();
    }
}