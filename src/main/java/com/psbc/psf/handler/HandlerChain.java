package com.psbc.psf.handler;

import com.psbc.psf.context.ChainContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;

/**
 * 2025/3/6 16:42
 * auth: dahua
 * desc:
 */
@Component
public class HandlerChain {

    private static final Logger logger = LoggerFactory.getLogger(HandlerChain.class);
    private Set<Handler> handlerCache = new TreeSet<>();

    public void addHandler(Handler handler) {
        handlerCache.add(handler);
    }

    public void handle(ChainContext chainContext) {
        for (Handler handler : handlerCache) {
            try {
                handler.handle(chainContext);
                if (chainContext.hasResponse()) {
                    break;
                }
            } catch (Throwable throwable) {
                chainContext.setThrowable(throwable);
            }
        }
    }
}
