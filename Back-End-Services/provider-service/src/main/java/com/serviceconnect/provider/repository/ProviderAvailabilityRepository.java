package com.serviceconnect.provider.repository;

import com.serviceconnect.provider.entity.ProviderAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface ProviderAvailabilityRepository
        extends JpaRepository<ProviderAvailability, Long> {

    List<ProviderAvailability> findByProviderId(
            Long providerId
    );

    List<ProviderAvailability> findByProviderIdAndActiveTrue(
            Long providerId
    );

    List<ProviderAvailability>
    findByProviderIdAndDayOfWeekAndActiveTrue(
            Long providerId,
            DayOfWeek dayOfWeek
    );

    boolean existsByProviderIdAndDayOfWeekAndStartTimeAndEndTime(
            Long providerId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    );
}