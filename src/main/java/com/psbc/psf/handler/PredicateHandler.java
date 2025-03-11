package com.psbc.psf.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psbc.psf.context.ChainContext;
import com.psbc.psf.properties.PsfPredicatesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 2025/3/11 11:29
 * auth: dahua
 * desc:
 */
@Component
public class PredicateHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(PredicateHandler.class);

    @Autowired
    private PsfPredicatesProperties psfPredicatesProperties;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void doHandler(ChainContext chainContext) {
        if (checkUri(chainContext) || checkHeader(chainContext) || checkPram(chainContext)) {
            chainContext.hasResponse(true);
        }
    }

    private boolean checkUri(ChainContext chainContext) {
        List<Map<String, Object>> uris = psfPredicatesProperties.getUri();
        if (uris != null) {
            String userPath = chainContext.getExchange().getRequest().getURI().getPath();
            for (Map<String, Object> uriMap : uris) {
                String path = (String) uriMap.get("path");
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                Pattern pattern = Pattern.compile(path);
                Matcher matcher = pattern.matcher(userPath);
                if (matcher.matches()) {
                    buildResponse(chainContext, uriMap.get("return"));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkHeader(ChainContext chainContext) {
        List<Map<String, Object>> headers = psfPredicatesProperties.getHeader();
        if (headers != null) {
            HttpHeaders headersTemp = chainContext.getExchange().getRequest().getHeaders();
            for (Map<String, Object> header : headers) {
                boolean match = true;
                for (String key : header.keySet()) {
                    if (key.equals("return")) {
                        continue;
                    }
                    String value = header.get(key).toString().trim();
                    List<String> headerValue = headersTemp.get(key);
                    if (!CollectionUtils.isEmpty(headerValue)) {
                        if (value.startsWith("[") && value.endsWith("]")) {
                            List<String> valueList = new ArrayList<>();
                            try {
                                valueList = objectMapper.readValue(value, new TypeReference<List<String>>() {
                                });
                            } catch (JsonProcessingException e) {
                            }
                            if (!new HashSet<>(headerValue).equals(new HashSet<>(valueList))) {
                                match = false;
                                break;
                            }
                        } else {
                            if (!value.equals(headerValue.get(0).trim())) {
                                match = false;
                                break;
                            }
                        }
                    } else {
                        match = false;
                    }
                }
                if (match) {
                    buildResponse(chainContext, header.get("return"));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkPram(ChainContext chainContext) {
        List<Map<String, Object>> params = psfPredicatesProperties.getParam();
        if (params != null) {
            MultiValueMap<String, String> queryParams = chainContext.getExchange().getRequest().getQueryParams();
            for (Map<String, Object> param : params) {
                boolean match = true;
                for (String key : param.keySet()) {
                    if (key.equals("return")) {
                        continue;
                    }
                    String value = param.get(key).toString().trim();
                    List<String> paramValue = queryParams.get(key);
                    if (!CollectionUtils.isEmpty(paramValue)) {
                        if (value.startsWith("[") && value.endsWith("]")) {
                            List<String> valueList = new ArrayList<>();
                            try {
                                valueList = objectMapper.readValue(value, new TypeReference<List<String>>() {
                                });
                            } catch (JsonProcessingException e) {
                            }
                            if (!new HashSet<>(paramValue).equals(new HashSet<>(valueList))) {
                                match = false;
                                break;
                            }
                        } else {
                            if (!value.equals(paramValue.get(0).trim())) {
                                match = false;
                                break;
                            }
                        }
                    } else {
                        match = false;
                    }
                }
                if (match) {
                    buildResponse(chainContext, param.get("return"));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int order() {
        return 1000;
    }
}
