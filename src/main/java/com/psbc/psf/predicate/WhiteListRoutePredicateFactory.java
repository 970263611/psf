package com.psbc.psf.predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * 2025/3/14 10:26
 * auth: dahua
 * desc:
 */
@Component
@RefreshScope
public class WhiteListRoutePredicateFactory extends AbstractPsfRoutePredicateFactory<WhiteListRoutePredicateFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(WhiteListRoutePredicateFactory.class);
    private ObjectMapper objectMapper;
    private Configuration configuration;
    private final List<HttpMessageReader<?>> messageReaders;
    @Value("${spring.cloud.gateway.routeBodyEqualsIgnoreType:true}")
    private boolean routeBodyEqualsIgnoreType;

    public WhiteListRoutePredicateFactory(ObjectMapper objectMapper) {
        super(WhiteListRoutePredicateFactory.Config.class);
        this.objectMapper = objectMapper;
        configuration = Configuration.builder()
                .jsonProvider(new JacksonJsonNodeJsonProvider())
                .build();
        this.messageReaders = HandlerStrategies.withDefaults().messageReaders();
    }

    @Override
    public String name() {
        return "WhiteList";
    }

    @Override
    public AsyncPredicate<ServerWebExchange> applyAsync(WhiteListRoutePredicateFactory.Config config) {
        return applyAsync(config, objectMapper, configuration, messageReaders, routeBodyEqualsIgnoreType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Predicate<ServerWebExchange> apply(WhiteListRoutePredicateFactory.Config config) {
        throw new UnsupportedOperationException("WhiteListPredicateFactory is only async.");
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("key", "value");
    }

    public static class Config implements AbstractPsfRoutePredicateFactory.Config {

        private String key;
        private List<String> value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public List<String> getValue() {
            return value;
        }

        public void setValue(List<String> value) {
            this.value = value;
        }

        @Override
        public boolean check(ObjectMapper objectMapper, Configuration configuration, Object body, boolean routeBodyEqualsIgnoreType) {
            try {
                JsonNode jsonNode = objectMapper.readTree(body.toString());
                DocumentContext parse = JsonPath.parse(jsonNode, configuration);
                JsonNode readValueNode = parse.read(key);
                if (value == null || value.isEmpty()) {
                    return false;
                }
                if (readValueNode.isMissingNode()) {
                    return false;
                }
                if (!readValueNode.isArray()) {
                    if (!routeBodyEqualsIgnoreType && JsonNodeType.STRING != readValueNode.getNodeType()) {
                        return false;
                    }
                    String configValue = readValueNode.asText();
                    if (value.contains(configValue)) {
                        return true;
                    }
                } else {
                    return false;
                }
                return false;
            } catch (Exception e) {
                logger.error("Body parse exception: ", e);
            }
            return false;
        }
    }
}
