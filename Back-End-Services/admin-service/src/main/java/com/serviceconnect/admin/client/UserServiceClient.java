package com.serviceconnect.admin.client;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;


@Component
@RequiredArgsConstructor
public class UserServiceClient {


    private final RestClient.Builder restClientBuilder;


    @Value("${services.user-service.url}")
    private String userServiceUrl;

    // ============================================================
    // GET ALL USERS
    // ============================================================

    public List<UserResponse> getAllUsers(
            String authorizationHeader) {

        return restClientBuilder
                .baseUrl(userServiceUrl)
                .build()
                .get()
                .uri("/api/users")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<UserResponse>>() {}
                );
    }


    // ============================================================
    // GET USER BY ID
    // ============================================================

    public UserResponse getUserById(
            Long userId,
            String authorizationHeader) {

        return restClientBuilder
                .baseUrl(userServiceUrl)
                .build()
                .get()
                .uri("/api/users/{id}", userId)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .body(UserResponse.class);
    }


    // ============================================================
    // DELETE USER
    // ============================================================

    public void deleteUser(
            Long userId,
            String authorizationHeader) {

        restClientBuilder
                .baseUrl(userServiceUrl)
                .build()
                .delete()
                .uri("/api/users/{id}", userId)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .toBodilessEntity();
    }


    // ============================================================
    // USER RESPONSE
    // ============================================================

    public record UserResponse(

            Long id,

            String firstName,

            String lastName,

            String phone

    ) {
    }
}