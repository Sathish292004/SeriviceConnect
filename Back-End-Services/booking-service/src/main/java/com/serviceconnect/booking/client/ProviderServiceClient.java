package com.serviceconnect.booking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

@Component
public class ProviderServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.provider.url}")
    private String providerServiceUrl;


    public ProviderServiceClient(
            RestTemplate restTemplate
    ) {
        this.restTemplate = restTemplate;
    }


    // ============================================================
    // VALIDATE APPROVED PROVIDER
    // ============================================================

    public boolean validateApprovedProvider(
            Long providerId
    ) {

        if (providerId == null) {
            return false;
        }

        return validateApprovedProvider(
                providerId,
                getAuthorizationHeader()
        );
    }


    public boolean validateApprovedProvider(
            Long providerId,
            String authorizationHeader
    ) {

        if (providerId == null) {
            return false;
        }

        try {

            String url =
                    providerServiceUrl
                            + "/api/v1/providers/"
                            + providerId
                            + "/public";

            HttpHeaders headers =
                    new HttpHeaders();

            if (authorizationHeader != null
                    && !authorizationHeader.isBlank()) {

                headers.set(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                );
            }

            org.springframework.http.HttpEntity<Void> entity =
                    new org.springframework.http.HttpEntity<>(
                            headers
                    );

            ResponseEntity<ProviderResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            ProviderResponse.class
                    );

            return response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().id() != null
                    && response.getBody().status() != null
                    && "APPROVED".equalsIgnoreCase(
                    response.getBody().status()
            );

        } catch (RestClientException ex) {

            return false;
        }
    }


    // ============================================================
    // GET PROVIDER ID BY USER ID
    // ============================================================

    public Long getProviderIdByUserId(
            Long userId
    ) {

        return getProviderIdByUserId(
                userId,
                getAuthorizationHeader()
        );
    }


    public Long getProviderIdByUserId(
            Long userId,
            String authorizationHeader
    ) {

        if (userId == null) {
            return null;
        }

        try {

            String url =
                    providerServiceUrl
                            + "/api/v1/providers/user/"
                            + userId;

            HttpHeaders headers =
                    new HttpHeaders();

            if (authorizationHeader != null
                    && !authorizationHeader.isBlank()) {

                headers.set(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                );
            }

            org.springframework.http.HttpEntity<Void> entity =
                    new org.springframework.http.HttpEntity<>(
                            headers
                    );

            ResponseEntity<ProviderResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            ProviderResponse.class
                    );

            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody() == null) {

                return null;
            }

            return response.getBody().id();

        } catch (RestClientException ex) {

            return null;
        }
    }


    // ============================================================
    // GET ACTIVE PROVIDER AVAILABILITY
    // ============================================================

    public boolean checkAvailability(
            Long providerId,
            OffsetDateTime requestedStartAt,
            OffsetDateTime requestedEndAt,
            String authorizationHeader
    ) {

        if (providerId == null
                || requestedStartAt == null
                || requestedEndAt == null) {

            return false;
        }

        if (!requestedStartAt.isBefore(
                requestedEndAt
        )) {

            return false;
        }

        try {

            String url =
                    providerServiceUrl
                            + "/api/v1/providers/"
                            + providerId
                            + "/availability/active";

            HttpHeaders headers =
                    new HttpHeaders();

            if (authorizationHeader != null
                    && !authorizationHeader.isBlank()) {

                headers.set(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                );
            }

            org.springframework.http.HttpEntity<Void> entity =
                    new org.springframework.http.HttpEntity<>(
                            headers
                    );

            ResponseEntity<List<ProviderAvailabilityResponse>>
                    response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<>() {
                            }
                    );

            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody() == null
                    || response.getBody().isEmpty()) {

                return false;
            }


            /*
             * Provider availability is currently stored as:
             *
             * DAY OF WEEK
             * START TIME
             * END TIME
             *
             * The incoming booking request contains an offset,
             * so we evaluate the local date/time represented by
             * that requested OffsetDateTime.
             */


            if (!requestedStartAt.toLocalDate()
                    .equals(
                            requestedEndAt.toLocalDate()
                    )) {

                return false;
            }


            DayOfWeek requestedDay =
                    requestedStartAt.getDayOfWeek();

            LocalTime requestedStart =
                    requestedStartAt.toLocalTime();

            LocalTime requestedEnd =
                    requestedEndAt.toLocalTime();


            return response.getBody()
                    .stream()
                    .filter(
                            ProviderAvailabilityResponse::active
                    )
                    .anyMatch(
                            availability -> {

                                if (availability.dayOfWeek() == null
                                        || availability.startTime() == null
                                        || availability.endTime() == null) {

                                    return false;
                                }


                                DayOfWeek availabilityDay;

                                try {

                                    availabilityDay =
                                            DayOfWeek.valueOf(
                                                    availability
                                                            .dayOfWeek()
                                                            .trim()
                                                            .toUpperCase()
                                            );

                                } catch (
                                        IllegalArgumentException ex
                                ) {

                                    return false;
                                }


                                if (availabilityDay
                                        != requestedDay) {

                                    return false;
                                }


                                /*
                                 * The COMPLETE requested interval
                                 * must fit inside one active window.
                                 *
                                 * Example:
                                 *
                                 * Availability:
                                 * 09:00 - 17:00
                                 *
                                 * Booking:
                                 * 10:00 - 12:00 -> allowed
                                 *
                                 * Booking:
                                 * 08:00 - 10:00 -> rejected
                                 *
                                 * Booking:
                                 * 16:00 - 18:00 -> rejected
                                 */

                                return !requestedStart.isBefore(
                                        availability.startTime()
                                )
                                        && !requestedEnd.isAfter(
                                        availability.endTime()
                                );
                            }
                    );

        } catch (RestClientException ex) {

            return false;
        }
    }


    // ============================================================
    // GET PROVIDER
    // ============================================================

    private ProviderResponse getProvider(
            Long providerId,
            String authorizationHeader
    ) {

        if (providerId == null) {
            return null;
        }

        try {

            String url =
                    providerServiceUrl
                            + "/api/v1/providers/"
                            + providerId
                            + "/public";

            HttpHeaders headers =
                    new HttpHeaders();

            if (authorizationHeader != null
                    && !authorizationHeader.isBlank()) {

                headers.set(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                );
            }

            org.springframework.http.HttpEntity<Void> entity =
                    new org.springframework.http.HttpEntity<>(
                            headers
                    );

            ResponseEntity<ProviderResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            ProviderResponse.class
                    );

            if (!response.getStatusCode().is2xxSuccessful()) {

                return null;
            }

            return response.getBody();

        } catch (RestClientException ex) {

            return null;
        }
    }


    // ============================================================
    // GET CURRENT AUTHORIZATION HEADER
    // ============================================================

    private String getAuthorizationHeader() {

        try {

            RequestAttributes attributes =
                    RequestContextHolder
                            .getRequestAttributes();

            if (attributes
                    instanceof ServletRequestAttributes
                    servletRequestAttributes) {

                String authorization =
                        servletRequestAttributes
                                .getRequest()
                                .getHeader(
                                        HttpHeaders.AUTHORIZATION
                                );

                if (authorization != null
                        && !authorization.isBlank()) {

                    return authorization;
                }
            }

        } catch (Exception ignored) {
        }

        return null;
    }


    // ============================================================
    // PROVIDER RESPONSE
    // ============================================================

    private record ProviderResponse(

            Long id,

            Long userId,

            String businessName,

            String status
    ) {
    }


    // ============================================================
    // PROVIDER AVAILABILITY RESPONSE
    // ============================================================

    private record ProviderAvailabilityResponse(

            Long id,

            Long providerId,

            String dayOfWeek,

            LocalTime startTime,

            LocalTime endTime,

            boolean active,

            OffsetDateTime createdAt,

            OffsetDateTime updatedAt
    ) {
    }
}