package com.psbc.psf.filter;

import com.psbc.psf.predicate.BodyRoutePredicateFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static reactor.core.publisher.Flux.just;

/**
 * 2025/3/9 15:43
 * auth: dahua
 * desc:
 */
@Order(Integer.MIN_VALUE)
@Component
public class CacheBodyFilter implements GlobalFilter {

    private static final DataBufferFactory factory = new DefaultDataBufferFactory();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();
        if (HttpMethod.POST == method
                || HttpMethod.PUT == method
                || HttpMethod.PATCH == method
                || HttpMethod.DELETE == method
                || HttpMethod.OPTIONS == method) {
            Flux<DataBuffer> body;
            Object cacheBody = exchange.getAttributes().get(BodyRoutePredicateFactory.CACHE_REQUEST_BODY_OBJECT_KEY);
            if (cacheBody != null) {
                DataBuffer buffer = factory.wrap(cacheBody.toString().getBytes(StandardCharsets.UTF_8));
                body = Flux.just(buffer);
            } else {
                body = request.getBody();
            }
            return DataBufferUtils.join(body)
                    .flatMap(dataBuffer -> {
                        DataBufferUtils.retain(dataBuffer);
                        Flux<DataBuffer> cachedFlux = Flux
                                .defer(() -> just(dataBuffer.slice(0, dataBuffer.readableByteCount())));
                        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(
                                exchange.getRequest()) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                return cachedFlux;
                            }
                        };
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    });
        } else {
            return chain.filter(exchange);
        }
    }
}