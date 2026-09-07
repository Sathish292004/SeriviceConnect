package com.serviceconnect.gateway.controller;

import com.serviceconnect.gateway.filter.RequestIdGlobalFilter;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {

    @GetMapping(
            value = "/service",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Map<String, Object> serviceFallback(
            ServerWebExchange exchange) {

        String requestId = exchange.getRequest()
                .getHeaders()
                .getFirst(
                        RequestIdGlobalFilter.REQUEST_ID_HEADER
                );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "timestamp",
                Instant.now().toString()
        );

        response.put(
                "status",
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );

        response.put(
                "error",
                "Service Unavailable"
        );

        response.put(
                "message",
                "The requested service is temporarily unavailable"
        );

        response.put(
                "path",
                exchange.getRequest()
                        .getPath()
                        .value()
        );

        response.put(
                "requestId",
                requestId
        );

        return response;
    }
}