package com.serviceconnect.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import reactor.core.publisher.Mono;

@Component
public class RedisRateLimiterFailureFilter
        implements GlobalFilter, Ordered {

    private static final String RATE_LIMITED_AUTH_PATH_PREFIX =
            "/api/v1/auth/";

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        /*
         * The native Spring Cloud Gateway RedisRateLimiter owns the
         * Redis interaction. This filter deliberately does not attempt
         * to duplicate that logic.
         *
         * Redis availability and rate-limiter failure metrics will be
         * handled through the Gateway's rate-limiter infrastructure.
         *
         * Authentication endpoints remain protected by the native
         * RequestRateLimiter filters configured on their routes.
         */

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}