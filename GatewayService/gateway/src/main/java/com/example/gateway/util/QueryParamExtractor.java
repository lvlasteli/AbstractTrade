package com.example.gateway.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class QueryParamExtractor {

    private QueryParamExtractor() {
    }

    public static Map<String, String> extract(HttpServletRequest request) {
        Map<String, String> queryParams = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                queryParams.put(key, values[0]);
            } else {
                queryParams.put(key, "");
            }
        });
        return queryParams;
    }
}
