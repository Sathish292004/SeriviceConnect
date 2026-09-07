package com.serviceconnect.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestIdGlobalFilter implements GlobalFilter, Ordered {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String incomingRequestId = exchange.getRequest()
                .getHeaders()
                .getFirst(REQUEST_ID_HEADER);

        final String requestId =
                (incomingRequestId == null
                        || incomingRequestId.isBlank())
                        ? UUID.randomUUID().toString()
                        : incomingRequestId;

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(request -> request.headers(headers ->
                        headers.set(
                                REQUEST_ID_HEADER,
                                requestId
                        )))
                .build();

        mutatedExchange.getResponse()
                .getHeaders()
                .set(
                        REQUEST_ID_HEADER,
                        requestId
                );

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}