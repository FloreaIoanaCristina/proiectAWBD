package com.unibuc.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Cross-cutting request/response filter: injects/propagates a correlation id and
 * logs method, path, status and latency for every request through the gateway.
 */
@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdGlobalFilter.class);
    private static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        final String cid = correlationId;

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(CORRELATION_ID, cid)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        long start = System.currentTimeMillis();
        log.info("[{}] --> {} {}", cid,
                mutatedRequest.getMethod(), mutatedRequest.getURI().getRawPath());

        mutatedExchange.getResponse().getHeaders().add(CORRELATION_ID, cid);

        return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
            long took = System.currentTimeMillis() - start;
            log.info("[{}] <-- {} {} ({} ms)", cid,
                    mutatedExchange.getResponse().getStatusCode(),
                    mutatedRequest.getURI().getRawPath(), took);
        }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
