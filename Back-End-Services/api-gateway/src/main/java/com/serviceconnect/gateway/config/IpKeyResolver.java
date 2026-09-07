package com.serviceconnect.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component("ipKeyResolver")
public class IpKeyResolver implements KeyResolver {

    @Override
    public Mono<String> resolve(
            ServerWebExchange exchange) {

        if (exchange.getRequest().getRemoteAddress() == null) {
            return Mono.just("unknown");
        }

        if (exchange.getRequest()
                .getRemoteAddress()
                .getAddress() == null) {

            return Mono.just("unknown");
        }

        String ipAddress = exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress();

        if (ipAddress == null || ipAddress.isBlank()) {
            return Mono.just("unknown");
        }

        return Mono.just(ipAddress);
    }
}