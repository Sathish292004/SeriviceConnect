package com.serviceconnect.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("auth-service", route -> route
                        .path("/api/v1/auth/**")
                        .uri("lb://auth-service"))

                .route("user-service", route -> route
                        .path("/api/v1/users/**")
                        .uri("lb://user-service"))

                .route("admin-service", route -> route
                        .path("/api/v1/admin/**")
                        .uri("lb://admin-service"))

                .route("provider-service", route -> route
                        .path("/api/v1/providers/**")
                        .uri("lb://provider-service"))

                .route("booking-service", route -> route
                        .path("/api/v1/bookings/**")
                        .uri("lb://booking-service"))

                .route("catalog-service", route -> route
                        .path("/api/v1/catalog/**")
                        .uri("lb://catalog-service"))

                .route("review-service", route -> route
                        .path("/api/v1/reviews/**")
                        .uri("lb://review-service"))

                .build();
    }
}