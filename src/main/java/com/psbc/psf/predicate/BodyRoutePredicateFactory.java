package com.psbc.psf.predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 2025/3/13 14:00
 * auth: dahua
 * desc:
 */
@Component
@RefreshScope
public class BodyRoutePredicateFactory extends AbstractRoutePredicateFactory<BodyRoutePredicateFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(BodyRoutePredicateFactory.class);
    private static final String TEST_ATTRIBUTE = "read_body_predicate_test_attribute";
    public static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";
    private ObjectMapper objectMapper;
    private Configuration configuration;
    private final List<HttpMessageReader<?>> messageReaders;
    @Value("${spring.cloud.gateway.routeBodyEqualsIgnoreType:true}")
    private boolean routeBodyEqualsIgnoreType;

    public BodyRoutePredicateFactory(ObjectMapper objectMapper) {
        super(Config.class);
        this.objectMapper = objectMapper;
        configuration = Configuration.builder()
                .jsonProvider(new JacksonJsonNodeJsonProvider())
                .build();
        this.messageReaders = HandlerStrategies.withDefaults().messageReaders();
    }

    @Override
    public String name() {
        return "Body";
    }

    @Override
    public AsyncPredicate<ServerWebExchange> applyAsync(Config config) {
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

    @Override
    @SuppressWarnings("unchecked")
    public Predicate<ServerWebExchange> apply(BodyRoutePredicateFactory.Config config) {
        throw new UnsupportedOperationException("ReadBodyPredicateFactory is only async.");
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("key", "value");
    }

    public static class Config {

        private String key;
        private Object value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public boolean check(ObjectMapper objectMapper, Configuration configuration, Object body, boolean routeBodyEqualsIgnoreType) {
            try {
                JsonNode jsonNode = objectMapper.readTree(body.toString());
                JsonNode nameNode = JsonPath.parse(jsonNode, configuration).read(key);
                if (value == null) {
                    return false;
                }
                if (nameNode.isMissingNode()) {
                    return false;
                }
                if (nameNode.isArray()) {
                    ArrayNode arrayNode = (ArrayNode) jsonNode;
                    JsonNode valueNode = objectMapper.readTree(objectMapper.writeValueAsString(value));
                    Set set1 = new HashSet();
                    for (JsonNode node : arrayNode) {
                        if (!routeBodyEqualsIgnoreType) {
                            if (JsonNodeType.STRING != node.getNodeType()) {
                                return false;
                            }
                        }
                        set1.add(node.asText());
                    }
                    Set set2 = new HashSet();
                    for (JsonNode node : valueNode) {
                        set2.add(node.asText());
                    }
                    if (set1.equals(set2)) {
                        return true;
                    }
                } else {
                    if (!routeBodyEqualsIgnoreType) {
                        if (JsonNodeType.STRING != nameNode.getNodeType()) {
                            return false;
                        }
                    } else {
                        String configValue = nameNode.asText();
                        if (value.toString().equals(configValue)) {
                            return true;
                        }
                    }
                }
                return false;
            } catch (Exception e) {
                logger.error("Body parse exception: ", e);
                return false;
            }
        }
    }
}
