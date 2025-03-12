package com.psbc.psf.handler;

import com.psbc.psf.context.ChainContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Flux;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 2025/3/6 16:45
 * auth: dahua
 * desc:
 */
//@Component
public class MonitorHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(MonitorHandler.class);
    private Queue<Map<String, String>> monitorQueue = new LinkedBlockingQueue<>();

    @Override
    public void doHandler(ChainContext chainContext) {
        ServerHttpRequest request = chainContext.getExchange().getRequest();
        HttpMethod method = request.getMethod();
        Map<String, String> message = new LinkedHashMap() {{
            put("routerId", chainContext.getRouterId());
            put("url", request.getURI().toString());
            put("method", method.toString());
            put("header", request.getHeaders().toString());
            put("param", request.getQueryParams().toString());
        }};
        if (HttpMethod.POST == method
                || HttpMethod.PUT == method
                || HttpMethod.PATCH == method
                || HttpMethod.DELETE == method
                || HttpMethod.OPTIONS == method) {
            Flux<DataBuffer> body = request.getBody();
            AtomicReference<String> bodyRef = new AtomicReference<>();
            body.subscribe(buffer -> {
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
                DataBufferUtils.release(buffer);
                bodyRef.set(charBuffer.toString());
            });
            message.put("body", bodyRef.get());
        }
        boolean offer = monitorQueue.offer(message);
        if (offer) {
            logger.debug("Monitor request {}", message);
        }
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }
}
