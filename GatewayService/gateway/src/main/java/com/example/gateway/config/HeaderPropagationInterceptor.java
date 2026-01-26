package com.example.gateway.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HeaderPropagationInterceptor implements RequestInterceptor {
    
    private final String gatewayHeaderName;
    private final String gatewayHeaderValue;
    
    public HeaderPropagationInterceptor(String gatewayHeaderName, String gatewayHeaderValue) {
        this.gatewayHeaderName = gatewayHeaderName;
        this.gatewayHeaderValue = gatewayHeaderValue;
    }
    
    @Override
    public void apply(RequestTemplate template) {
        template.header(gatewayHeaderName, gatewayHeaderValue);
        
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            return;
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        if (request.getContentType() != null) {
            template.header("Content-Type", request.getContentType());
        }
        
        if (request.getCookies() != null) {
            String cookieHeader = Arrays.stream(request.getCookies())
                .map(c -> c.getName() + "=" + c.getValue())
                .collect(Collectors.joining("; "));
            template.header("Cookie", cookieHeader);
        }
        
        List.of("X-Request-Id", "X-Forwarded-For", "User-Agent", "Accept")
            .forEach(headerName -> {
                String value = request.getHeader(headerName);
                if (value != null) {
                    template.header(headerName, value);
                }
            });
        
        if (template.headers().get("X-Forwarded-For") == null) {
            template.header("X-Forwarded-For", request.getRemoteAddr());
        }
    }
}
