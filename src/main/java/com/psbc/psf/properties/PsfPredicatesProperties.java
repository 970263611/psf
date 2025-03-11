package com.psbc.psf.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * 2025/3/11 13:56
 * auth: dahua
 * desc:
 */
@ConfigurationProperties(prefix = PsfPredicatesProperties.PREFIX)
public class PsfPredicatesProperties {

    public static final String PREFIX = "spring.cloud.psf.predicates";
    private List<Map<String,Object>> uri;
    private List<Map<String,Object>> header;
    private List<Map<String,Object>> param;
    private List<Map<String,Object>> exception;
    private Map defaultReturn;

    public List<Map<String,Object>> getHeader() {
        return header;
    }

    public void setHeader(List<Map<String,Object>> header) {
        this.header = header;
    }

    public List<Map<String,Object>> getParam() {
        return param;
    }

    public void setParam(List<Map<String,Object>> param) {
        this.param = param;
    }

    public List<Map<String,Object>> getException() {
        return exception;
    }

    public void setException(List<Map<String,Object>> exception) {
        this.exception = exception;
    }

    public Map getDefaultReturn() {
        return defaultReturn;
    }

    public void setDefaultReturn(Map defaultReturn) {
        this.defaultReturn = defaultReturn;
    }

    public List<Map<String, Object>> getUri() {
        return uri;
    }

    public void setUri(List<Map<String, Object>> uri) {
        this.uri = uri;
    }
}
