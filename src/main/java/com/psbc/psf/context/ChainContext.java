package com.psbc.psf.context;

import com.psbc.psf.handler.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;

/**
 * 2025/3/8 23:16
 * auth: dahua
 * desc:
 */
public class ChainContext {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    private String routerId;
    private ServerWebExchange exchange;
    private Throwable throwable;
    private boolean response;
    private String responseData;

    public ChainContext(String routerId, ServerWebExchange exchange) {
        this.routerId = routerId;
        this.exchange = exchange;
    }

    public String getRouterId() {
        return routerId;
    }

    public ServerWebExchange getExchange() {
        return exchange;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public boolean hasThrowable() {
        return this.throwable != null;
    }

    public void hasResponse(boolean response) {
        this.response = response;
    }

    public boolean hasResponse() {
        return response;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }
}
