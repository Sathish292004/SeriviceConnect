package com.serviceconnect.provider.dto.response;

public record ProviderOnboardingStatusResponse(

        boolean profileExists,

        boolean completed,

        String status,

        boolean canServeCustomers
) {
}