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

        String authorization =
                getAuthorizationHeader();

        try {

            String phone =
                    restClientBuilder
                            .baseUrl(USER_SERVICE_URL)
                            .build()
                            .get()
                            .uri(
                                    "/api/users/{id}/phone",
                                    customerId
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    authorization
                            )
                            .retrieve()
                            .body(String.class);

            if (phone == null
                    || phone.isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer phone not found"
                );
            }

            return phone;

        } catch (ResponseStatusException exception) {

            throw exception;

        } catch (RestClientException exception) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "User service is currently unavailable"
            );
        }
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

        String authorization =
                getAuthorizationHeader();

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

            if (response == null) {

                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                );
            }

            return response;

        } catch (ResponseStatusException exception) {

            throw exception;

        } catch (RestClientException exception) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "User service is currently unavailable"
            );
        }
    }


    // ============================================================
    // GET AUTHORIZATION HEADER
    // ============================================================

    private String getAuthorizationHeader() {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes();


        if (attributes == null) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No current HTTP request found"
            );
        }


        String authorization =
                attributes.getRequest()
                        .getHeader(
                                HttpHeaders.AUTHORIZATION
                        );


        if (authorization == null
                || authorization.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authorization header is missing"
            );
        }


        return authorization;
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