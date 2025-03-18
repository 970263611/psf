package com.psbc.psf.predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 2025/3/14 18:21
 * auth: dahua
 * desc:
 */
@Component
public class AllRoutePredicateFactory {

    private static final Logger logger = LoggerFactory.getLogger(AllRoutePredicateFactory.class);
    private Map<String, RoutePredicateFactory> predicateFactories = new LinkedHashMap<>();

    public AllRoutePredicateFactory(List<RoutePredicateFactory> predicateFactoryList) {
        for (RoutePredicateFactory routePredicateFactory : predicateFactoryList) {
            this.predicateFactories.putIfAbsent(routePredicateFactory.name(), routePredicateFactory);
        }
    }

    public RoutePredicateFactory getFactory(String name) {
        return predicateFactories.get(name);
    }
}
