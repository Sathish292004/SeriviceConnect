package com.serviceconnect.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceconnect.gateway.filter.RequestIdGlobalFilter;

import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GatewayGlobalExceptionHandler
        implements WebExceptionHandler, Ordered {

    private final ObjectMapper objectMapper;

    public GatewayGlobalExceptionHandler(
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            Throwable exception) {

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(exception);
        }

        HttpStatus status = resolveStatus(exception);

        String requestId = exchange.getRequest()
                .getHeaders()
                .getFirst(
                        RequestIdGlobalFilter.REQUEST_ID_HEADER
                );

        Map<String, Object> body = new LinkedHashMap<>();

        body.put(
                "timestamp",
                Instant.now().toString()
        );

        body.put(
                "status",
                status.value()
        );

        body.put(
                "error",
                status.getReasonPhrase()
        );

        body.put(
                "message",
                resolveMessage(status)
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

        } catch (Exception serializationException) {

            responseBytes = (
                    """
                    {
                      "status": %d,
                      "error": "%s",
                      "message": "%s",
                      "path": "%s",
                      "requestId": "%s"
                    }
                    """
                            .formatted(
                                    status.value(),
                                    status.getReasonPhrase(),
                                    resolveMessage(status),
                                    exchange.getRequest()
                                            .getPath()
                                            .value(),
                                    requestId == null
                                            ? ""
                                            : requestId
                            )
            ).getBytes(StandardCharsets.UTF_8);
        }

        exchange.getResponse()
                .setStatusCode(status);

        exchange.getResponse()
                .getHeaders()
                .setContentType(
                        MediaType.APPLICATION_JSON
                );

        if (requestId != null
                && !requestId.isBlank()) {

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

    private HttpStatus resolveStatus(Throwable exception) {

        Throwable current = exception;

        while (current != null) {

            if (current instanceof
                    org.springframework.web.server.ResponseStatusException responseStatusException) {

                int statusCode =
                        responseStatusException
                                .getStatusCode()
                                .value();

                HttpStatus resolved =
                        HttpStatus.resolve(statusCode);

                if (resolved != null) {
                    return resolved;
                }
            }

            current = current.getCause();
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(HttpStatus status) {

        return switch (status) {

            case NOT_FOUND ->
                    "The requested resource was not found";

            case BAD_GATEWAY ->
                    "The downstream service returned an invalid response";

            case SERVICE_UNAVAILABLE ->
                    "The requested service is temporarily unavailable";

            case GATEWAY_TIMEOUT ->
                    "The downstream service did not respond in time";

            case INTERNAL_SERVER_ERROR ->
                    "An unexpected error occurred";

            default ->
                    status.getReasonPhrase();
        };
    }

    @Override
    public int getOrder() {
        return -2;
    }
}