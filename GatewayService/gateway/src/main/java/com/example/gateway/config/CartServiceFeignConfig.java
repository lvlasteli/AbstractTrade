package com.example.gateway.config;

import com.example.gateway.client.ServiceErrorDecoder;
import com.example.gateway.service.CartIdentityService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Configuration
@Slf4j
public class CartServiceFeignConfig {
    
    @Bean
    public RequestInterceptor cartServiceHeaderInterceptor(
        @Value("${gateway.service.secret.header-name:X-Gateway-Request}") String headerName,
        @Value("${gateway.service.secret.value}") String headerValue) {
        
        if (!StringUtils.isNotEmpty(headerName)) {
            log.warn("Header name is not set");
        }

        if (!StringUtils.isNotEmpty(headerValue)) {
            log.warn("GATEWAY_SERVICE_SECRET is not set");
        }
        
        return new HeaderPropagationInterceptor(headerName, headerValue);
    }
    
    @Bean
    public ErrorDecoder cartServiceErrorDecoder() {
        return new ServiceErrorDecoder();
    }

    @Bean
    public RequestInterceptor cartUserIdInterceptor() {
        return (RequestTemplate template) -> {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;
            Object userId = attrs.getAttribute(CartIdentityService.ATTR_USER_ID, RequestAttributes.SCOPE_REQUEST);
            if (userId != null) {
                String value = String.valueOf(userId);
                if (!value.isBlank()) {
                    template.header("X-User-Id", value);
                }
            }
        };
    }
}
