package com.psbc.psf.predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * 2025/3/12 17:12
 * auth: dahua
 * desc:
 */
@Component
public class BodyPredicateFactory extends AbstractRoutePredicateFactory<BodyPredicateProperties> {

    private static final Logger logger = LoggerFactory.getLogger(BodyPredicateFactory.class);

    @Autowired
    private ObjectMapper objectMapper;
    private ParseContext parseContext;

    @PostConstruct
    public void init() {
        Configuration configuration = Configuration.builder()
                .jsonProvider(new JacksonJsonProvider(objectMapper))
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .build();
        parseContext = JsonPath.using(configuration);
    }

    public BodyPredicateFactory() {
        super(BodyPredicateProperties.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("key", "value");
    }

    @Override
    public Predicate<ServerWebExchange> apply(BodyPredicateProperties config) {
        return exchange -> {
            String cachedBody = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
            if (cachedBody == null) return false;
            try {
                JsonNode jsonNode = objectMapper.readTree(cachedBody);
                List<BodyPredicateProperties.KV> keysAndValues = config.getKeysAndValues();
                for (BodyPredicateProperties.KV keysAndValue : keysAndValues) {
                    String key = keysAndValue.getKey();
                    String value = keysAndValue.getValue();
                    DocumentContext ctx = parseContext.parse(jsonNode);
                    String configValue = ctx.read("$." + key, String.class);
                    if (value == null) {
                        return false;
                    }
                    if (value.equalsIgnoreCase(configValue)) {
                        return true;
                    }
                    System.out.println(configValue);
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        };
    }

    @Override
    public String name() {
        return "Body";
    }
}
