package com.psbc.psf.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psbc.psf.context.ChainContext;
import com.psbc.psf.exception.PsfException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * 2025/3/7 15:21
 * auth: dahua
 * desc:
 */
public abstract class AbstractHandler implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHandler.class);
    @Autowired
    private HandlerChain handlerChain;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void handle(ChainContext chainContext) throws PsfException {
        if (chainContext.hasThrowable()) {
            if (this instanceof ExceptionHandler) {
                this.doHandler(chainContext);
            }
        } else {
            this.doHandler(chainContext);
        }
    }

    @PostConstruct
    public void init() {
        handlerChain.addHandler(this);
    }

    protected void buildResponse(ChainContext chainContext, Object data) {
        String str = "";
        try {
            str = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
        }
        buildResponse(chainContext, str);
    }

    protected void buildResponse(ChainContext chainContext, String data) {
        chainContext.setResponseData(data);
    }

    abstract void doHandler(ChainContext chainContext) throws PsfException;
}
