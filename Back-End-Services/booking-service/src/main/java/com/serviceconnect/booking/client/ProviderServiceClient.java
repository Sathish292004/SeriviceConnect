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
public class ProviderServiceClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${provider-service.url}")
    private String providerServiceUrl;


    // ============================================================
    // VALIDATE APPROVED PROVIDER
    // ============================================================

    public void validateApprovedProvider(
            Long providerId) {

        ProviderResponse provider =
                getProvider(providerId);

        if (provider == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Provider not found"
            );
        }

        if (!"APPROVED".equalsIgnoreCase(
                provider.status())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provider is not currently available"
            );
        }
    }


    // ============================================================
    // GET PROVIDER ID BY USER ID
    // ============================================================

    public Long getProviderIdByUserId(
            Long userId) {

        if (userId == null || userId <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User ID is required"
            );
        }


        String authorization =
                getAuthorizationHeader();


        try {

            ProviderResponse response =
                    restClientBuilder
                            .baseUrl(providerServiceUrl)
                            .build()
                            .get()
                            .uri(
                                    "/api/v1/providers/user/{userId}",
                                    userId
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    authorization
                            )
                            .retrieve()
                            .body(ProviderResponse.class);


            if (response == null) {

                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Provider profile not found"
                );
            }


            if (response.id() == null) {

                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Provider ID not found"
                );
            }


            return response.id();


        } catch (ResponseStatusException exception) {

            throw exception;

        } catch (RestClientException exception) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Provider service is currently unavailable"
            );
        }
    }


    // ============================================================
    // GET PROVIDER FROM PROVIDER SERVICE
    // ============================================================

    private ProviderResponse getProvider(
            Long providerId) {

        if (providerId == null || providerId <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provider ID is required"
            );
        }


        String authorization =
                getAuthorizationHeader();


        try {

            ProviderResponse response =
                    restClientBuilder
                            .baseUrl(providerServiceUrl)
                            .build()
                            .get()
                            .uri(
                                    "/api/v1/providers/{providerId}/public",
                                    providerId
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    authorization
                            )
                            .retrieve()
                            .body(ProviderResponse.class);


            if (response == null) {

                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Provider not found"
                );
            }


            return response;


        } catch (ResponseStatusException exception) {

            throw exception;

        } catch (RestClientException exception) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Provider service is currently unavailable"
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
    // PROVIDER RESPONSE
    // ============================================================

    private record ProviderResponse(

            Long id,

            Long userId,

            String businessName,

            String description,

            String phone,

            String email,

            String address,

            String city,

            String state,

            String postalCode,

            Double latitude,

            Double longitude,

            String status

    ) {
    }
}