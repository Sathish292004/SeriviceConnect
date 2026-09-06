package com.serviceconnect.provider.service;

import com.serviceconnect.provider.dto.request.ProviderAvailabilityRequest;
import com.serviceconnect.provider.dto.response.ProviderAvailabilityResponse;
import com.serviceconnect.provider.entity.Provider;
import com.serviceconnect.provider.entity.ProviderAvailability;
import com.serviceconnect.provider.repository.ProviderAvailabilityRepository;
import com.serviceconnect.provider.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class ProviderAvailabilityService {

    private final ProviderAvailabilityRepository availabilityRepository;
    private final ProviderRepository providerRepository;

    // ============================================================
    // CREATE AVAILABILITY
    // ============================================================

    public ProviderAvailabilityResponse create(
            Long providerId,
            Long authenticatedUserId,
            ProviderAvailabilityRequest request
    ) {

        Provider provider = findProvider(providerId);

        validateOwnership(
                provider,
                authenticatedUserId
        );

        DayOfWeek dayOfWeek =
                parseDayOfWeek(request.dayOfWeek());

        validateTimeRange(
                request.startTime(),
                request.endTime()
        );

        validateNoOverlap(
                providerId,
                null,
                dayOfWeek,
                request.startTime(),
                request.endTime()
        );

        OffsetDateTime now =
                OffsetDateTime.now();

        ProviderAvailability availability =
                new ProviderAvailability();

        availability.setProviderId(providerId);

        availability.setDayOfWeek(
                dayOfWeek
        );

        availability.setStartTime(
                request.startTime()
        );

        availability.setEndTime(
                request.endTime()
        );

        availability.setActive(true);

        availability.setCreatedAt(now);

        availability.setUpdatedAt(now);

        return toResponse(
                availabilityRepository.save(
                        availability
                )
        );
    }

    // ============================================================
    // PROVIDER - GET OWN AVAILABILITY
    // Includes active + inactive
    // ============================================================

    @Transactional(readOnly = true)
    public List<ProviderAvailabilityResponse>
    getProviderAvailability(
            Long providerId,
            Long authenticatedUserId
    ) {

        Provider provider =
                findProvider(providerId);

        validateOwnership(
                provider,
                authenticatedUserId
        );

        return sort(
                availabilityRepository
                        .findByProviderId(providerId)
        )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ============================================================
    // CUSTOMER - GET ACTIVE AVAILABILITY
    // Only APPROVED providers
    // ============================================================

    @Transactional(readOnly = true)
    public List<ProviderAvailabilityResponse>
    getActiveAvailability(
            Long providerId
    ) {

        Provider provider =
                findProvider(providerId);

        if (!"APPROVED".equalsIgnoreCase(
                provider.getStatus()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Provider not available"
            );
        }

        return sort(
                availabilityRepository
                        .findByProviderIdAndActiveTrue(
                                providerId
                        )
        )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ============================================================
    // UPDATE AVAILABILITY
    // ============================================================

    public ProviderAvailabilityResponse update(
            Long availabilityId,
            Long authenticatedUserId,
            ProviderAvailabilityRequest request
    ) {

        ProviderAvailability availability =
                findAvailability(availabilityId);

        Provider provider =
                findProvider(
                        availability.getProviderId()
                );

        validateOwnership(
                provider,
                authenticatedUserId
        );

        DayOfWeek dayOfWeek =
                parseDayOfWeek(request.dayOfWeek());

        validateTimeRange(
                request.startTime(),
                request.endTime()
        );

        validateNoOverlap(
                availability.getProviderId(),
                availabilityId,
                dayOfWeek,
                request.startTime(),
                request.endTime()
        );

        availability.setDayOfWeek(
                dayOfWeek
        );

        availability.setStartTime(
                request.startTime()
        );

        availability.setEndTime(
                request.endTime()
        );

        availability.setUpdatedAt(
                OffsetDateTime.now()
        );

        return toResponse(
                availabilityRepository.save(
                        availability
                )
        );
    }

    // ============================================================
    // DELETE AVAILABILITY
    // ============================================================

    public void delete(
            Long availabilityId,
            Long authenticatedUserId
    ) {

        ProviderAvailability availability =
                findAvailability(availabilityId);

        Provider provider =
                findProvider(
                        availability.getProviderId()
                );

        validateOwnership(
                provider,
                authenticatedUserId
        );

        availabilityRepository.delete(
                availability
        );
    }

    // ============================================================
    // ENABLE / DISABLE AVAILABILITY
    // ============================================================

    public ProviderAvailabilityResponse setActive(
            Long availabilityId,
            Long authenticatedUserId,
            boolean active
    ) {

        ProviderAvailability availability =
                findAvailability(availabilityId);

        Provider provider =
                findProvider(
                        availability.getProviderId()
                );

        validateOwnership(
                provider,
                authenticatedUserId
        );

        availability.setActive(active);

        availability.setUpdatedAt(
                OffsetDateTime.now()
        );

        return toResponse(
                availabilityRepository.save(
                        availability
                )
        );
    }

    // ============================================================
    // FIND PROVIDER
    // ============================================================

    private Provider findProvider(
            Long providerId
    ) {

        return providerRepository
                .findById(providerId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Provider not found"
                        )
                );
    }

    // ============================================================
    // FIND AVAILABILITY
    // ============================================================

    private ProviderAvailability findAvailability(
            Long availabilityId
    ) {

        return availabilityRepository
                .findById(availabilityId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Provider availability not found"
                        )
                );
    }

    // ============================================================
    // OWNERSHIP
    // ============================================================

    private void validateOwnership(
            Provider provider,
            Long authenticatedUserId
    ) {

        if (!provider.getUserId()
                .equals(authenticatedUserId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this provider profile"
            );
        }
    }

    // ============================================================
    // DAY PARSING
    // ============================================================

    private DayOfWeek parseDayOfWeek(
            String value
    ) {

        if (value == null ||
                value.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Day of week is required"
            );
        }

        try {

            return DayOfWeek.valueOf(
                    value.trim()
                            .toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException ex) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid day of week. Use MONDAY to SUNDAY"
            );
        }
    }

    // ============================================================
    // TIME VALIDATION
    // ============================================================

    private void validateTimeRange(
            LocalTime startTime,
            LocalTime endTime
    ) {

        if (startTime == null ||
                endTime == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start time and end time are required"
            );
        }

        if (!startTime.isBefore(endTime)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start time must be before end time"
            );
        }
    }

    // ============================================================
    // OVERLAP VALIDATION
    //
    // Existing: 09:00 - 12:00
    //
    // Rejected:
    // 10:00 - 11:00
    // 08:00 - 10:00
    // 11:00 - 13:00
    // 09:30 - 12:30
    //
    // Allowed:
    // 12:00 - 13:00
    // ============================================================

    private void validateNoOverlap(
            Long providerId,
            Long excludedAvailabilityId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {

        List<ProviderAvailability> sameDay =
                availabilityRepository
                        .findByProviderIdAndDayOfWeekAndActiveTrue(
                                providerId,
                                dayOfWeek
                        );

        boolean overlap =
                sameDay.stream()
                        .filter(existing ->
                                excludedAvailabilityId == null
                                        || !existing.getId()
                                        .equals(
                                                excludedAvailabilityId
                                        )
                        )
                        .anyMatch(existing ->
                                startTime.isBefore(
                                        existing.getEndTime()
                                )
                                        &&
                                        endTime.isAfter(
                                                existing.getStartTime()
                                        )
                        );

        if (overlap) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Availability window overlaps an existing active window"
            );
        }
    }

    // ============================================================
    // SORT
    // Monday -> Sunday
    // ============================================================

    private List<ProviderAvailability> sort(
            List<ProviderAvailability> availability
    ) {

        return availability.stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        (ProviderAvailability a) ->
                                                a.getDayOfWeek()
                                                        .getValue()
                                )
                                .thenComparing(
                                        ProviderAvailability::getStartTime
                                )
                )
                .toList();
    }

    // ============================================================
    // ENTITY -> RESPONSE
    // ============================================================

    private ProviderAvailabilityResponse toResponse(
            ProviderAvailability availability
    ) {

        return new ProviderAvailabilityResponse(
                availability.getId(),
                availability.getProviderId(),
                availability.getDayOfWeek().name(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.isActive(),
                availability.getCreatedAt(),
                availability.getUpdatedAt()
        );
    }
}