package com.psbc.psf.handler;

import com.psbc.psf.context.ChainContext;
import com.psbc.psf.properties.PsfPredicatesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 2025/3/6 16:47
 * auth: dahua
 * desc:
 */
@Component
public class ExceptionHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    @Autowired
    private PsfPredicatesProperties psfPredicatesProperties;

    @Override
    public void doHandler(ChainContext chainContext) {
        if (chainContext.hasThrowable()) {
            Throwable throwable = chainContext.getThrowable();
            logger.error("Psf find exception {}", throwable);
            String name = throwable.getClass().getName();
            List<Map<String, Object>> exceptions = psfPredicatesProperties.getException();
            for (Map<String, Object> exception : exceptions) {
                String exceptionName = (String) exception.get("name");
                if (name.equals(exceptionName)) {
                    buildResponse(chainContext, exception.get("return"));
                    chainContext.hasResponse(true);
                    break;
                }
            }
            if (!chainContext.hasResponse()) {
                Map defaultReturn = psfPredicatesProperties.getDefaultReturn();
                if (defaultReturn != null) {
                    buildResponse(chainContext, defaultReturn);
                } else {
                    buildResponse(chainContext, "Psf find exception: " + throwable.getMessage());
                }
                chainContext.hasResponse(true);
            }
        }
    }

    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }
}
