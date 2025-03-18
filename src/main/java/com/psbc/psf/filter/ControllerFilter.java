package com.psbc.psf.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psbc.psf.context.ChainContext;
import com.psbc.psf.handler.HandlerChain;
import com.psbc.psf.properties.PsfPredicatesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 2025/3/6 17:36
 * auth: dahua
 * desc:
 */
@Component
public class ControllerFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(ControllerFilter.class);
    @Autowired
    private HandlerChain handlerChain;
    @Autowired
    private PsfPredicatesProperties psfPredicatesProperties;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String routerId = ((Route) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR)).getId();
        ChainContext chainContext = new ChainContext(routerId, exchange);
        handlerChain.handle(chainContext);
        ServerHttpResponse response = exchange.getResponse();
        if (chainContext.hasResponse()) {
            return buildResponse(response, chainContext);
        }
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            if (response.getStatusCode() != HttpStatus.OK) {
                buildResponse(response, chainContext);
            }
        }));
    }

    private Mono<Void> buildResponse(ServerHttpResponse response, ChainContext chainContext) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String responseData = chainContext.getResponseData();
        if (responseData == null) {
            Map defaultReturn = psfPredicatesProperties.getDefaultReturn();
            try {
                responseData = objectMapper.writeValueAsString(defaultReturn);
            } catch (Exception e) {
                responseData = "Psf find exception: " + e.getMessage();
            }
        }
        byte[] bytes = responseData.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
