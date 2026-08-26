package com.serviceconnect.booking.client;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestClient.Builder restClientBuilder;

    private static final String USER_SERVICE_URL =
            "http://localhost:8082";


    // ============================================================
    // GET CUSTOMER PHONE
    // ============================================================

    public String getCustomerPhone(
            Long customerId) {

        UserResponse response =
                getUser(customerId);

        return response.phone();
    }


    // ============================================================
    // GET USER ROLE
    // ============================================================

    public String getUserRole(
            Long userId) {

        UserResponse response =
                getUser(userId);

        return response.role();
    }


    // ============================================================
    // GET USER FROM USER SERVICE
    // ============================================================

    private UserResponse getUser(
            Long userId) {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes();


        // ========================================================
        // HTTP REQUEST NOT FOUND
        // ========================================================

        if (attributes == null) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No current HTTP request found"
            );
        }


        // ========================================================
        // GET AUTHORIZATION HEADER
        // ========================================================

        String authorization =
                attributes.getRequest()
                        .getHeader(
                                HttpHeaders.AUTHORIZATION
                        );


        // ========================================================
        // AUTHORIZATION HEADER MISSING
        // ========================================================

        if (authorization == null
                || authorization.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authorization header is missing"
            );
        }


        // ========================================================
        // CALL USER SERVICE
        // ========================================================

        try {

            UserResponse response =
                    restClientBuilder
                            .baseUrl(USER_SERVICE_URL)
                            .build()
                            .get()
                            .uri(
                                    "/api/users/{id}",
                                    userId
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    authorization
                            )
                            .retrieve()
                            .body(UserResponse.class);


            // ====================================================
            // EMPTY RESPONSE
            // ====================================================

            if (response == null) {

                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                );
            }


            return response;


        } catch (ResponseStatusException exception) {

            // Preserve our intentional HTTP status.
            throw exception;


        } catch (RestClientException exception) {

            // User service/network problem.
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "User service is currently unavailable"
            );
        }
    }


    // ============================================================
    // USER RESPONSE
    // ============================================================

    private record UserResponse(

            Long id,

            String firstName,

            String lastName,

            String phone,

            String role

    ) {
    }
}