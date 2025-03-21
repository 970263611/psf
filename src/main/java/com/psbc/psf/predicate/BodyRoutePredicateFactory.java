package com.psbc.psf.predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.jayway.jsonpath.Configuration;
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
public class BodyRoutePredicateFactory extends AbstractPsfRoutePredicateFactory<BodyRoutePredicateFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(BodyRoutePredicateFactory.class);
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
        return applyAsync(config, objectMapper, configuration, messageReaders, routeBodyEqualsIgnoreType);
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

    public static class Config implements AbstractPsfRoutePredicateFactory.Config {

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
            }
            return false;
        }
    }
}
