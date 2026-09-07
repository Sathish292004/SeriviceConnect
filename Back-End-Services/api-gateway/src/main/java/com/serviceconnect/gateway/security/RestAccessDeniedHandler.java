package com.serviceconnect.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceconnect.gateway.filter.RequestIdGlobalFilter;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RestAccessDeniedHandler
        implements ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            AccessDeniedException exception) {

        String requestId = exchange.getRequest()
                .getHeaders()
                .getFirst(
                        RequestIdGlobalFilter.REQUEST_ID_HEADER
                );

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put(
                "message",
                "You do not have permission to access this resource"
        );
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
                      "status": 403,
                      "error": "Forbidden",
                      "message": "You do not have permission to access this resource"
                    }
                    """.getBytes();
        }

        exchange.getResponse()
                .setStatusCode(HttpStatus.FORBIDDEN);

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