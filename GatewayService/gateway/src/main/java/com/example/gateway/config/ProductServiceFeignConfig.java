package com.example.gateway.config;

import com.example.gateway.client.ServiceErrorDecoder;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ProductServiceFeignConfig {
    
    @Bean
    public RequestInterceptor productServiceHeaderInterceptor(
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
    public ErrorDecoder productServiceErrorDecoder() {
        return new ServiceErrorDecoder();
    }
}
