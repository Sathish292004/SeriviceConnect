package com.serviceconnect.booking.client;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${services.user.url}")
    private String userServiceUrl;


    // ============================================================
    // GET CUSTOMER PHONE
    // ============================================================

    public String getCustomerPhone(
            Long customerId) {

        if (customerId == null || customerId <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Customer ID is required"
            );
        }


        String authorization =
                getAuthorizationHeader();


        try {

            String phone =
                    restClientBuilder
                            .baseUrl(userServiceUrl)
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
}