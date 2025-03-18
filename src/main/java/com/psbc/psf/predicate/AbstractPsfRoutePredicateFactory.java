package com.psbc.psf.predicate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 2025/3/18 8:40
 * auth: dahua
 * desc:
 */
public abstract class AbstractPsfRoutePredicateFactory<C> extends AbstractRoutePredicateFactory<C> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPsfRoutePredicateFactory.class);
    public static final String TEST_ATTRIBUTE = "read_body_predicate_test_attribute";
    public static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";

    public AbstractPsfRoutePredicateFactory(Class configClass) {
        super(configClass);
    }

    public AsyncPredicate<ServerWebExchange> applyAsync(AbstractPsfRoutePredicateFactory.Config config, ObjectMapper objectMapper, Configuration configuration, List<HttpMessageReader<?>> messageReaders, boolean routeBodyEqualsIgnoreType) {
        return new AsyncPredicate<ServerWebExchange>() {
            @Override
            public Publisher<Boolean> apply(ServerWebExchange exchange) {
                Class inClass = String.class;
                Object cachedBody = exchange.getAttribute(CACHE_REQUEST_BODY_OBJECT_KEY);
                if (cachedBody != null) {
                    try {
                        boolean test = config.check(objectMapper, configuration, cachedBody, routeBodyEqualsIgnoreType);
                        exchange.getAttributes().put(TEST_ATTRIBUTE, test);
                        return Mono.just(test);
                    } catch (ClassCastException e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Predicate test failed because class in predicate "
                                    + "does not match the cached body object", e);
                        }
                    }
                    return Mono.just(false);
                } else {
                    return ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange,
                            (serverHttpRequest) -> ServerRequest
                                    .create(exchange.mutate().request(serverHttpRequest).build(), messageReaders)
                                    .bodyToMono(inClass).doOnNext(objectValue -> exchange.getAttributes()
                                            .put(CACHE_REQUEST_BODY_OBJECT_KEY, objectValue))
                                    .map(objectValue -> config.check(objectMapper, configuration, objectValue, routeBodyEqualsIgnoreType)));
                }
            }

            @Override
            public Object getConfig() {
                return config;
            }
        };
    }

    interface Config {
        boolean check(ObjectMapper objectMapper, Configuration configuration, Object body, boolean routeBodyEqualsIgnoreType);
    }
}
