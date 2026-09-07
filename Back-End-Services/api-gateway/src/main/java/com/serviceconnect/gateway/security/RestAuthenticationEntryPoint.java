package com.serviceconnect.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceconnect.gateway.filter.RequestIdGlobalFilter;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RestAuthenticationEntryPoint
        implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> commence(
            ServerWebExchange exchange,
            AuthenticationException exception) {

        String requestId = exchange.getRequest()
                .getHeaders()
                .getFirst(
                        RequestIdGlobalFilter.REQUEST_ID_HEADER
                );

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", "Authentication is required");
        body.put(
                "path",
                exchange.getRequest().getPath().value()
        );
        body.put("requestId", requestId);

        byte[] responseBytes;

        try {
            responseBytes =
                    objectMapper.writeValueAsBytes(body);
        } catch (Exception ex) {
            responseBytes = """
                    {
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Authentication is required"
                    }
                    """.getBytes();
        }

        exchange.getResponse()
                .setStatusCode(HttpStatus.UNAUTHORIZED);

        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        exchange.getResponse()
                .getHeaders()
                .setContentLength(responseBytes.length);

        if (requestId != null && !requestId.isBlank()) {
            exchange.getResponse()
                    .getHeaders()
                    .set(
                            RequestIdGlobalFilter.REQUEST_ID_HEADER,
                            requestId
                    );
        }

        return exchange.getResponse()
                .writeWith(
                        Mono.just(
                                exchange.getResponse()
                                        .bufferFactory()
                                        .wrap(responseBytes)
                        )
                );
    }
}