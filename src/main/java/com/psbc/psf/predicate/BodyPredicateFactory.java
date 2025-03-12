package com.psbc.psf.predicate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
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
public class BodyPredicateFactory extends AbstractRoutePredicateFactory<BodyPredicateFactory.Config> {

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
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("key", "value");
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {


//            Flux<DataBuffer> body = exchange.getRequest().getBody();
//            AtomicReference<String> bodyRef = new AtomicReference<>();
//            body.subscribe(buffer -> {
//                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
//                DataBufferUtils.release(buffer);
//                bodyRef.set(charBuffer.toString());
//            });
//            String cachedBody = bodyRef.get();
//            if (cachedBody == null) return false;
//            try {
//                JsonNode jsonNode = objectMapper.readTree(cachedBody);
//                String key = config.getKey();
//                String value = config.getValue();
//                DocumentContext ctx = parseContext.parse(jsonNode);
//                String configValue = ctx.read("$." + key, String.class);
//                if (value == null) {
//                    return false;
//                }
//                if (value.equalsIgnoreCase(configValue)) {
//                    return true;
//                }
//                return false;
//            } catch (Exception e) {
//                return false;
//            }
            return true;
        };
    }

    @Override
    public String name() {
        return "Body";
    }

    public static class Config {

        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
