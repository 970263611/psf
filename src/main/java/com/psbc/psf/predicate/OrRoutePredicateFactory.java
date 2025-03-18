package com.psbc.psf.predicate;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.event.PredicateArgsEvent;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Predicate;

/**
 * 2025/3/14 11:06
 * auth: dahua
 * desc:
 */
@Component
@RefreshScope
public class OrRoutePredicateFactory extends AbstractRoutePredicateFactory<OrRoutePredicateFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(OrRoutePredicateFactory.class);
    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    @Lazy
    private AllRoutePredicateFactory allRoutePredicateFactory;

    public OrRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public String name() {
        return "Or";
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("predicates");
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        throw new UnsupportedOperationException("OrPredicateFactory is only async.");
    }

    @Override
    public AsyncPredicate<ServerWebExchange> applyAsync(Config config) {
        return exchange -> {
            List<AsyncPredicate> predicates = new ArrayList<>();
            String routeId = (String) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_PREDICATE_ROUTE_ATTR);
            List<PredicateDefinition> predicateDefinitions = config.getPredicates();
            if (predicateDefinitions == null) {
                return Mono.just(false);
            }
            for (PredicateDefinition predicateDefinition : predicateDefinitions) {
                RoutePredicateFactory factory = allRoutePredicateFactory.getFactory(predicateDefinition.getName());
                if (factory == null) {
                    throw new IllegalArgumentException("Unable to find RoutePredicateFactory with name " + predicateDefinition.getName());
                }
                Map<String, String> args = predicateDefinition.getArgs();
                ConfigurationService.AbstractBuilder builder = this.configurationService.with(factory);
                builder.name(predicateDefinition.getName());
                builder.properties(args);
                LinkedHashMap<String, Object> changeArgs = new LinkedHashMap<>();
                changeArgs.putAll(args);
                PredicateArgsEvent predicateArgsEvent = new PredicateArgsEvent(
                        OrRoutePredicateFactory.this, routeId, changeArgs);
                Object predicateConfig = builder.eventFunction((bound, properties) -> predicateArgsEvent)
                        .bind();
                AsyncPredicate apply;
                if (factory instanceof AbstractPsfRoutePredicateFactory) {
                    apply = factory.applyAsync(predicateConfig);
                } else {
                    apply = AsyncPredicate.from(factory.apply(predicateConfig));
                }
                predicates.add(apply);
            }
            if (!predicates.isEmpty()) {
                AsyncPredicate temp = predicates.remove(0);
                for (AsyncPredicate predicate : predicates) {
                    temp = temp.or(predicate);
                }
                return (Publisher<Boolean>) temp.apply(exchange);
            }
            return Mono.just(false);
        };
    }

    public static class Config {

        private List<PredicateDefinition> predicates;

        public List<PredicateDefinition> getPredicates() {
            return predicates;
        }

        public void setPredicates(List<PredicateDefinition> predicates) {
            this.predicates = predicates;
        }
    }
}
