package com.serviceconnect.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RateLimitResponseGlobalFilter
        implements GlobalFilter, Ordered {

    private static final String RETRY_AFTER_SECONDS = "60";

    private final ObjectMapper objectMapper;

    public RateLimitResponseGlobalFilter(
            ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        return chain.filter(exchange)
                .onErrorResume(
                        exception -> {

                            if (exchange.getResponse().isCommitted()) {
                                return Mono.error(exception);
                            }

                            return Mono.error(exception);
                        }
                )
                .then(
                        Mono.defer(() -> {

                            if (exchange.getResponse()
                                    .getStatusCode()
                                    != HttpStatus.TOO_MANY_REQUESTS) {

                                return Mono.empty();
                            }

                            if (exchange.getResponse().isCommitted()) {
                                return Mono.empty();
                            }

                            return writeRateLimitResponse(exchange);
                        })
                );
    }

    private Mono<Void> writeRateLimitResponse(
            ServerWebExchange exchange) {

        String requestId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(
                                RequestIdGlobalFilter
                                        .REQUEST_ID_HEADER
                        );

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "timestamp",
                Instant.now().toString()
        );

        body.put(
                "status",
                HttpStatus.TOO_MANY_REQUESTS.value()
        );

        body.put(
                "error",
                "Too Many Requests"
        );

        body.put(
                "message",
                "Too many requests. Please try again later."
        );

        body.put(
                "path",
                exchange.getRequest()
                        .getPath()
                        .value()
        );

        body.put(
                "requestId",
                requestId
        );

        byte[] responseBytes;

        try {

            responseBytes =
                    objectMapper.writeValueAsBytes(body);

        } catch (Exception exception) {

            responseBytes = (
                    """
                    {
                      "status": 429,
                      "error": "Too Many Requests",
                      "message": "Too many requests. Please try again later.",
                      "path": "%s",
                      "requestId": "%s"
                    }
                    """
                            .formatted(
                                    exchange.getRequest()
                                            .getPath()
                                            .value(),
                                    requestId == null
                                            ? ""
                                            : requestId
                            )
            ).getBytes(
                    StandardCharsets.UTF_8
            );
        }

        exchange.getResponse()
                .setStatusCode(
                        HttpStatus.TOO_MANY_REQUESTS
                );

        exchange.getResponse()
                .getHeaders()
                .setContentType(
                        MediaType.APPLICATION_JSON
                );

        exchange.getResponse()
                .getHeaders()
                .set(
                        "Retry-After",
                        RETRY_AFTER_SECONDS
                );

        if (requestId != null
                && !requestId.isBlank()) {

            exchange.getResponse()
                    .getHeaders()
                    .set(
                            RequestIdGlobalFilter
                                    .REQUEST_ID_HEADER,
                            requestId
                    );
        }

        exchange.getResponse()
                .getHeaders()
                .setContentLength(
                        responseBytes.length
                );

        return exchange.getResponse()
                .writeWith(
                        Mono.just(
                                exchange.getResponse()
                                        .bufferFactory()
                                        .wrap(responseBytes)
                        )
                );
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}